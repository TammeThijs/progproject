package com.example.stephan.squashapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements HttpRequestHelperGET.AsyncResponse{

    HttpRequestHelperGET request;               // Connect to internet
    ProgressDialog progressDialog;              // Wait for data
    UserTrainingAdapter adapter;                // show trainings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listview = (ListView) findViewById(R.id.ListViewTraining);
        adapter = new UserTrainingAdapter(this, new ArrayList<Training>());
        listview.setAdapter(adapter);
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
                getData();
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



    /**
     * Update data.
     * Connect to internet.
     */
    public void getData(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Connecting to server...");
            progressDialog.show();
        }
        request = new HttpRequestHelperGET(this);
        request.execute();

    }

    /**
     * Callback from asynctask
     */
    public void processFinish(ArrayList<Training> trainingsList){
        // update trainings
        adapter.clear();
        for(int i = 0; i < trainingsList.size(); i++){
            adapter.add(trainingsList.get(i));
        }
        adapter.notifyDataSetChanged();

        // cancel dialog
        if (progressDialog != null){
            progressDialog.cancel();
        }


    }
}