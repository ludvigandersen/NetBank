package com.example.netbank.Repository;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.netbank.Model.Customer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.ContentValues.TAG;

public class DatabaseHandler {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createCustomer(String firstName, String lastName, String age, String address, String email){


        Customer customer = new Customer(firstName, lastName, age, address);

        db.collection("users").document(email)
                .set(customer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }
}
