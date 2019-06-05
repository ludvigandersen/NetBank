package com.example.netbank.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.netbank.Fragments.AccountsFragment;
import com.example.netbank.Fragments.BillsFragment;
import com.example.netbank.Fragments.ProfileFragment;
import com.example.netbank.Model.Account;
import com.example.netbank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AccountsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private final static String TAG = "AccountsActivity";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.userEmail);
        payAutomaticBills();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AccountsFragment()).commit();
        }


        if (user != null) {
            userEmail.setText(user.getEmail() + "");
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.accounts, menu);
        return true;
    }

    //Skal umiddelbart ikke bruges
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_accounts) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AccountsFragment()).commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ProfileFragment()).commit();
        } else if (id == R.id.nav_bills) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new BillsFragment()).commit();
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void signOut() {
        Intent i = new Intent(this, LoginActivity.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(i);
    }

    //TODO: Create method that automatically pays bills of current user, where automatic = true, maybe set bill to recurring at creation?
    private void payAutomaticBills() {

        Date date = new Date();

        db.collection("companies").document("boligforeningen").collection("customer")
                .document(user.getEmail()).collection("bills").whereEqualTo("recurring", new SimpleDateFormat("dd-MM-yyyy").format(date))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.getBoolean("automatic")) {
                                    automaticPayment(document.getString("fromAccount"),
                                            "boligforeningen", document.getId(), document.getLong("amount").intValue(), document.getString("recurring"));
                                }


                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    private void automaticPayment(String accountName, String reciever, String billName, final int amount, String date) {
        Log.d(TAG, "makeTransaction: Has been called");
        final DocumentReference senderDocRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(accountName);

        final DocumentReference recieverDocRef = db.collection("companies").document(reciever);

        final DocumentReference billDocRef = db.collection("companies").document(reciever).collection("customer")
                .document(user.getEmail()).collection("bills").document(billName);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot sender = transaction.get(senderDocRef);
                DocumentSnapshot reciever = transaction.get(recieverDocRef);


                int senderBalance = sender.getLong("balance").intValue();
                int recieverBalance = reciever.getLong("amount").intValue();
                int transactionBalance = Math.abs(amount);

                transaction.update(senderDocRef, "balance", senderBalance - transactionBalance);
                transaction.update(recieverDocRef, "amount", recieverBalance + transactionBalance);
                transaction.update(billDocRef, "isPaid", true);


                SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
                Date newDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(newDate);
                cal.add(Calendar.MONTH, 1);
                newDate = cal.getTime();
                transaction.update(billDocRef, "recurring", dateformat.format(newDate));


                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });

    }

}
