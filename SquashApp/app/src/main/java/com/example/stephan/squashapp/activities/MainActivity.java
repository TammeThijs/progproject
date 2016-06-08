package com.example.stephan.squashapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.stephan.squashapp.adapters.UserTrainingAdapter;
import com.example.stephan.squashapp.helpers.FirebaseConnector;
import com.example.stephan.squashapp.models.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements FirebaseConnector.AsyncResponse {

    ProgressDialog progressDialog;              // Wait for data
    UserTrainingAdapter adapter;                // show trainings
    FirebaseConnector firebase =
            new FirebaseConnector(FirebaseDatabase.getInstance().getReference());
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("startuplog", "hoi");

        // set listview
        ListView listview = (ListView) findViewById(R.id.ListViewTraining);
        adapter = new UserTrainingAdapter(this, new ArrayList<Training>());
        listview.setAdapter(adapter);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                if (user != null) {
                    // User is signed in
                    Log.d("user", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("user", "onAuthStateChanged:signed_out");
                }

            }
        };

    }

    public void signIn(){
        Log.d("signing","in");
        String email = "stephan_handbal@hotmail.com";
        String password = "!Mjooj33";
        mAuth.signInAnonymously();
//        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                Log.d("logging", "signInWithEmail:onComplete:" + task.isSuccessful());
//
//                // If sign in fails, display a message to the user. If sign in succeeds
//                // the auth state listener will be notified and logic to handle the
//                // signed in user can be handled in the listener.
//                if (!task.isSuccessful()) {
//                    Log.w("logging2", "signInWithEmail", task.getException());
//                }
//
//                // ...
//            }
//        });
    }

    /**
     * When resume get data again from firebase
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
     * create menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.actionbar_main, menu);
        return true;
    }

    /**
     * Set listener to menu items
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_reload:
                updateDatabase();
                break;
            case R.id.menu_admin:
                adminMenu();
                break;
            case R.id.menu_logo:
                Toast.makeText(MainActivity.this, "Logo", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_contact:
                Toast.makeText(MainActivity.this, "Contact", Toast.LENGTH_SHORT).show();
                Intent newContactWindow = new Intent(this, ContactActivity.class);
                startActivity(newContactWindow);
                break;
            case R.id.sign_in:
                signIn();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Create login for admin activity
     */
    public void adminMenu(){
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
                            if(username.getText().toString().compareTo("admin") == 0 &&
                                    password.getText().toString().compareTo("admin") == 0){
                                Toast.makeText(
                                        MainActivity.this, "Succes", Toast.LENGTH_SHORT).show();
                                Intent adminScreen =
                                        new Intent(MainActivity.this, AdminActivity.class);
                                startActivity(adminScreen);
                            }
                            else{
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
    }
}