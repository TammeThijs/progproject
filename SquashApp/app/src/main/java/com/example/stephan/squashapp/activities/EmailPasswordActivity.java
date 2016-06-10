package com.example.stephan.squashapp.activities;

/**
 * Created by Stephan on 9-6-2016.
 *
 */

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordActivity extends MainActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText mEmailField;
    private Dialog dialog;
    private EditText mPasswordField;
    private Button signOut;
    private Button signIn;
    private Button register;
    private Integer resultCode;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        signOut = (Button) findViewById(R.id.sign_out_button);
        signIn = (Button) findViewById(R.id.email_sign_in_button);
        register = (Button) findViewById(R.id.email_create_account_button);

        signOut.setOnClickListener(this);
        signIn.setOnClickListener(this);
        register.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // add back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(EmailPasswordActivity.this, "singed in", Toast.LENGTH_SHORT).show();

                    signIn.setVisibility(View.GONE);
                    signOut.setVisibility(View.VISIBLE);
                    register.setVisibility(View.GONE);
                    mEmailField.setVisibility(View.GONE);
                    mPasswordField.setVisibility(View.GONE);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Toast.makeText(EmailPasswordActivity.this, "singed out", Toast.LENGTH_SHORT).show();
                    signIn.setVisibility(View.VISIBLE);
                    signOut.setVisibility(View.GONE);
                    register.setVisibility(View.VISIBLE);
                    mEmailField.setVisibility(View.VISIBLE);
                    mPasswordField.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // make sure users has correct google play service
        resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext());
    }

    @Override
    public void onResume(){
        super.onResume();
        if (resultCode == ConnectionResult.SUCCESS) {
            Toast.makeText(EmailPasswordActivity.this, "Update succesfull" +
                    " you can login now.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("result", resultCode.toString());
            Toast.makeText(EmailPasswordActivity.this, "Failed please update google play service",
                    Toast.LENGTH_SHORT).show();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                //This dialog will help the user update to the latest GooglePlayServices
                dialog.show();
            }
        }
    }


    /**
     * Set back button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // check witch item is pressed.
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private void signIn(String email, String password) {
        // check if correct form
        if (!validateForm()) {
            return;
        }

        // make dialog
        showProgressDialog();

        // sign in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // done
                        hideProgressDialog();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else if(!email.contains("@")) {
            mEmailField.setError("It must be a valid email adress.");
            valid = false;
        }
        else{
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        }
        else if(password.length() < 6){
            mPasswordField.setError("Minimal 6 characters.");
            valid = false;
        }
        else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    /**
     * Show progress.
     */
    private void showProgressDialog(){
        dialog = new Dialog(this);
        dialog.setTitle("Connecting...");
        dialog.show();
    }

    /**
     * Hide progress.
     */
    private void hideProgressDialog(){
        dialog.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email_create_account_button:
                Intent register = new Intent(EmailPasswordActivity.this, RegisterNewUser.class);
                startActivity(register);
                finish();
                break;
            case R.id.email_sign_in_button:
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}

// confirm password