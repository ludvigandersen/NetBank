package com.example.netbank;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }



    public void signOut(){
        Intent i = new Intent(this, LoginActivity.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(i);
    }

}
