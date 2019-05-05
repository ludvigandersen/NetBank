package com.example.netbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;

    EditText inputEmail, inputPassword, repeatPassword;
    Button signIn, register, finishRegister, resetPassword, backLogin, finishReset, backReset;
    ImageButton hidePassword, showPassword;
    TextView passwordText, repeatPassText, emailText, resetInfo;

    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signOut();
        init();

        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount: has been called");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed: Email and atleast 6 character password required",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn: has been called");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void resetPassword(String emailAddress) {
        Log.d(TAG, "resetPassword: Has been called");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        Log.d(TAG, "updateUI: Has been called");

        Intent i = new Intent(this, AccountsActivity.class);
        if (currentUser != null) {
            startActivity(i);
        }
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public void onClick(View v) {
        Log.d(TAG, "onClick: Has been called");
        int i = v.getId();
        if (i == R.id.signIn) {
            signIn(inputEmail.getText().toString(), inputPassword.getText().toString());

        } else if (i == R.id.register) {
            register.setVisibility(View.GONE);
            signIn.setVisibility(View.GONE);
            resetPassword.setVisibility(View.GONE);
            finishRegister.setVisibility(View.VISIBLE);
            repeatPassword.setVisibility(View.VISIBLE);
            backLogin.setVisibility(View.VISIBLE);

        } else if (i == R.id.finishRegister) {
            if (inputPassword.getText().toString().equalsIgnoreCase(repeatPassword.getText().toString())) {
                createAccount(inputEmail.getText().toString(), inputPassword.getText().toString());
            } else {
                repeatPassword.setError(getString(R.string.repeat_pass_error));
            }

        } else if (i == R.id.finishReset) {
                resetPassword(inputEmail.getText().toString());

        } else if (i == R.id.hidePassword) {
            inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            showPassword.setVisibility(View.VISIBLE);
            hidePassword.setVisibility(View.GONE);

        } else if (i == R.id.showPassword) {
            inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            hidePassword.setVisibility(View.VISIBLE);
            showPassword.setVisibility(View.GONE);

        } else if (i == R.id.backLogin) {
            register.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.VISIBLE);
            resetPassword.setVisibility(View.VISIBLE);
            finishRegister.setVisibility(View.GONE);
            repeatPassword.setVisibility(View.GONE);
            backLogin.setVisibility(View.GONE);

        } else if (i == R.id.resetPassword) {
            inputPassword.setVisibility(View.GONE);
            signIn.setVisibility(View.GONE);
            register.setVisibility(View.GONE);
            resetPassword.setVisibility(View.GONE);
            hidePassword.setVisibility(View.GONE);
            showPassword.setVisibility(View.GONE);
            resetInfo.setVisibility(View.VISIBLE);
            finishReset.setVisibility(View.VISIBLE);
            backReset.setVisibility(View.VISIBLE);
        } else if (i == R.id.backReset){
            inputPassword.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.VISIBLE);
            register.setVisibility(View.VISIBLE);
            resetPassword.setVisibility(View.VISIBLE);
            hidePassword.setVisibility(View.VISIBLE);
            showPassword.setVisibility(View.VISIBLE);
            resetInfo.setVisibility(View.GONE);
            finishReset.setVisibility(View.GONE);
            backReset.setVisibility(View.GONE);
        }
    }


    private void init() {
        Log.d(TAG, "init: Has been called");

        //EditTexts
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        repeatPassword = findViewById(R.id.repeatPassword);

        //Buttons
        signIn = findViewById(R.id.signIn);
        register = findViewById(R.id.register);
        finishRegister = findViewById(R.id.finishRegister);
        resetPassword = findViewById(R.id.resetPassword);
        finishReset = findViewById(R.id.finishReset);
        backReset = findViewById(R.id.backReset);

        //ImageButtons
        hidePassword = findViewById(R.id.hidePassword);
        showPassword = findViewById(R.id.showPassword);
        backLogin = findViewById(R.id.backLogin);

        //TextViews
        resetInfo = findViewById(R.id.resetInfo);

    }

    @Override
    public void onBackPressed() {

    }
}
