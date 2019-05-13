package com.example.netbank;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.netbank.Model.Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;


public class AccountsFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    TextView savingsView, budgetView, pensionView, defaultView, businessView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        savingsView = view.findViewById(R.id.savingsView);
        budgetView = view.findViewById(R.id.budgetView);
        pensionView = view.findViewById(R.id.pensionView);
        defaultView = view.findViewById(R.id.defaultView);
        businessView = view.findViewById(R.id.businessView);

        getAccounts();


    }

    @Override
    public void onClick(View view) {
        int i = view.getId();


    }

    private void init() {

        getAccounts();

    }

    private void getAccounts() {

        db.collection("users").document(user.getEmail()).collection("accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {


                                Account account = document.toObject(Account.class);


                                if (account.getAccountType().equalsIgnoreCase("Budget") && account.isAccountActive()) {
                                    budgetView.setVisibility(View.VISIBLE);
                                    budgetView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                } else if (account.getAccountType().equalsIgnoreCase("Business") && account.isAccountActive()) {
                                    businessView.setVisibility(View.VISIBLE);
                                    businessView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                } else if (account.getAccountType().equalsIgnoreCase("Default") && account.isAccountActive()) {
                                    defaultView.setVisibility(View.VISIBLE);
                                    defaultView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                } else if (account.getAccountType().equalsIgnoreCase("Pension") && account.isAccountActive()) {
                                    pensionView.setVisibility(View.VISIBLE);
                                    pensionView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                } else if (account.getAccountType().equalsIgnoreCase("Savings") && account.isAccountActive()) {
                                    savingsView.setVisibility(View.VISIBLE);
                                    savingsView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                }

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


}
