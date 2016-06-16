package com.example.stephan.squashapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stephan.squashapp.adapters.UserTrainingAdapter;
import com.example.stephan.squashapp.helpers.FirebaseConnector;
import com.example.stephan.squashapp.models.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


/**
 * Here everyone can see the upcomming trainings. If you login other features will be enabled.
 *
 * Logged in Features:
 * Register, cancel registration, MegaChat, user account setting and if you have to password the
 * admin page.
 */
public class MainActivity extends AppCompatActivity implements FirebaseConnector.AsyncResponse {

    private ProgressDialog progressDialog;              // Wait for data
    private UserTrainingAdapter adapter;                // show trainings
    private TextView mainInfo;
    private FirebaseConnector firebase = new FirebaseConnector();
    private FirebaseUser user;
    private TextView signInStatus;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get views.
        ListView listview = (ListView) findViewById(R.id.ListViewTraining);
        mainInfo = (TextView) findViewById(R.id.mainInfo);
        signInStatus = (TextView) findViewById(R.id.signinstatus);

        // Set adapter to listview.
        adapter = new UserTrainingAdapter(this, new ArrayList<Training>());
        listview.setAdapter(adapter);


        // Update users and set an on change listener.
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        });

        // Register and deregister.
        listview.setOnItemClickListener(onItemClickListener());
    }

    /**
     * OnItemClickListener that wil send you to login page, register alert or cancel
     * registration alert.
     */
    private AdapterView.OnItemClickListener onItemClickListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // Check if user is logged in.
                if(user != null){
                    // Get current Training.
                    Training training = adapter.getItem(position);
                    // Check if user is registered.
                    if(training.getRegisteredPlayers().containsKey(user.getUid())){
                        // Already registered, give option to cancel.
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Cancel Registration").setMessage("Are you sure you want" +
                                        " to deregister?")
                                .setPositiveButton("deregister", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        adapter.getItem(position).deletePlayer(user.getUid());
                                        firebase.updateRegisteredPlayers(adapter.getItem(position),
                                                position);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        dialog.create().show();
                    }
                    else {
                        // Not registered, give option to register.
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Register").setMessage("Do you want to register for " +
                                        adapter.getItem(position).getFormattedDate())
                                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        adapter.getItem(position).registerPlayer(user.getUid(), user.getDisplayName());
                                        firebase.updateRegisteredPlayers(adapter.getItem(position),
                                                position);
                                        adapter.notifyDataSetChanged();
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        dialog.create().show();
                    }
                }
                else{
                    // Not logged in, send to login page.
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Not signed in").setMessage("Please sign in")
                            .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent signIn = new Intent(MainActivity.this,
                                            LoginActivity.class);
                                    startActivity(signIn);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    dialog.create().show();
                }
            }
        };
    }

    /**
     * When resume get data again from firebase
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateDatabase();

        if(menu != null) {
            menu.clear();
            if (user != null) {
                getMenuInflater().inflate(R.menu.actionbar_menu_logged_in, menu);
            } else {
                getMenuInflater().inflate(R.menu.actionbar_main, menu);
            }
        }


        if(user != null && user.getDisplayName() != null ){
            signInStatus.setText("Welcome " + user.getDisplayName());
            signInStatus.setVisibility(View.VISIBLE);
            mainInfo.setVisibility(View.GONE);
        }
        else{
            signInStatus.setVisibility(View.GONE);
            mainInfo.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Call class that will call firebase to get data
     */
    private void updateDatabase(){
        adapter.clear();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.show();
        firebase.getTraingen(this);
    }

    /**
     * When data is loaded, this function will be called.
     *
     * Set new trainingslist to adapter
     */
    public void processFinish(ArrayList<Training> output){
        adapter.setTrainingList(output);
        Log.d("done", "done");
        adapter.notifyDataSetChanged();
        progressDialog.cancel();
    }

    /**
     * Create menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Check if logged in.
        if(user != null){
            // Set logged in menu.
            getMenuInflater().inflate(R.menu.actionbar_menu_logged_in, menu);
        }
        else{
            // set logged out menu.
            getMenuInflater().inflate(R.menu.actionbar_main, menu);
        }
        return true;
    }

    private void signOutMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_main, menu);
        signInStatus.setVisibility(View.GONE);
        mainInfo.setVisibility(View.VISIBLE);

    }

    /**
     * Set listener to menu items.
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_reload:
                // Reload trainings.
                updateDatabase();
                break;
            case R.id.menu_admin:
                // Open admin page.
                adminMenu();
                break;
            case R.id.menu_contact:
                // Open contact page.
                Toast.makeText(MainActivity.this, "Contact", Toast.LENGTH_SHORT).show();
                Intent newContactWindow = new Intent(this, ContactActivity.class);
                startActivity(newContactWindow);
                break;
            case R.id.menu_sign_in:
                // Open sign in page.
                Intent signIn = new Intent(this, LoginActivity.class);
                startActivity(signIn);
                break;
            case R.id.menu_account:
                // Open user account page.
                Intent accountActivity = new Intent(MainActivity.this, UserAccountActivity.class);
                startActivity(accountActivity);
                break;
            case R.id.menu_sign_out:
                // Sign user out.
                FirebaseAuth.getInstance().signOut();
                signOutMenu(menu);
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
            case R.id.menu_mega_chat:
                // Open mega chat page.
                Intent megaChat = new Intent(MainActivity.this, MegaChatActivity.class);
                startActivity(megaChat);

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Create login for admin activity.
     */
    public void adminMenu() {
        // Check if user really is logged in.
        if (user != null) {
            // make layout
            LayoutInflater li = LayoutInflater.from(this);
            View layout = li.inflate(R.layout.alertdialog_open_admin, null);
            final EditText username = (EditText) layout.findViewById(R.id.username);
            final EditText password = (EditText) layout.findViewById(R.id.password);

            // create alertdialog
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Admin")
                    .setMessage("Please login.")
                    .setCancelable(true)
                    .setView(layout)
                    .setPositiveButton(
                            "Login",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (username.getText().toString().compareTo("admin") == 0 &&
                                            password.getText().toString().compareTo("admin") == 0) {
                                        Toast.makeText(
                                                MainActivity.this, "Succes", Toast.LENGTH_SHORT).show();
                                        Intent adminScreen =
                                                new Intent(MainActivity.this, AdminActivity.class);
                                        startActivity(adminScreen);
                                    } else {
                                        Toast.makeText(
                                                MainActivity.this, "Invalid login!", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .create().show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Not signed in").setMessage("Please sign in")
                    .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent signIn = new Intent(MainActivity.this,
                                    LoginActivity.class);
                            startActivity(signIn);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            dialog.create().show();
        }
    }
}