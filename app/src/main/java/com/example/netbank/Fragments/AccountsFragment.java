package com.example.netbank.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netbank.Model.Account;
import com.example.netbank.Model.TransactionParcelable;
import com.example.netbank.R;
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

    final static String TAG = "AccountsFragment";

    TextView savingsView, budgetView, pensionView, defaultView, businessView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null){
            TransactionParcelable check = bundle.getParcelable("transaction");
            if (check.getConfirm().equalsIgnoreCase("transaction")){
                Toast.makeText(getActivity(), "Transaction succesfull",
                        Toast.LENGTH_SHORT).show();
            }
        }

        savingsView = view.findViewById(R.id.savingsView);
        savingsView.setOnClickListener(this);
        budgetView = view.findViewById(R.id.budgetView);
        budgetView.setOnClickListener(this);
        pensionView = view.findViewById(R.id.pensionView);
        pensionView.setOnClickListener(this);
        defaultView = view.findViewById(R.id.defaultView);
        defaultView.setOnClickListener(this);
        businessView = view.findViewById(R.id.businessView);
        businessView.setOnClickListener(this);

        getAccounts();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        SpecificAccountFragment fragment = new SpecificAccountFragment();
        Bundle bundle = new Bundle();

        if (i == R.id.savingsView) {
            bundle.putString("accountName", getFirstWord(savingsView.getText().toString()));
        } else if (i == R.id.budgetView) {
            bundle.putString("accountName", getFirstWord(budgetView.getText().toString()));
        } else if (i == R.id.pensionView) {
            bundle.putString("accountName", getFirstWord(pensionView.getText().toString()));
        }else if (i == R.id.defaultView) {
            bundle.putString("accountName", getFirstWord(defaultView.getText().toString()));
        }else if (i == R.id.businessView) {
            bundle.putString("accountName", getFirstWord(businessView.getText().toString()));
        }


        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    private void init() {

    }

    private void getAccounts() {
        Log.d(TAG, "getAccounts: Has been called");
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
                                    budgetView.setTextSize(20);
                                } else if (account.getAccountType().equalsIgnoreCase("Business") && account.isAccountActive()) {
                                    businessView.setVisibility(View.VISIBLE);
                                    businessView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                    businessView.setTextSize(20);
                                } else if (account.getAccountType().equalsIgnoreCase("Default") && account.isAccountActive()) {
                                    defaultView.setVisibility(View.VISIBLE);
                                    defaultView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                    defaultView.setTextSize(20);
                                } else if (account.getAccountType().equalsIgnoreCase("Pension") && account.isAccountActive()) {
                                    pensionView.setVisibility(View.VISIBLE);
                                    pensionView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                    pensionView.setTextSize(20);
                                } else if (account.getAccountType().equalsIgnoreCase("Savings") && account.isAccountActive()) {
                                    savingsView.setVisibility(View.VISIBLE);
                                    savingsView.setText(getString(R.string.account_display, account.getAccountType(), account.getBalance()));
                                    savingsView.setTextSize(20);
                                }

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) { // Check if there is more than one word.
            return text.substring(0, index); // Extract first word.
        } else {
            return text; // Text is the first word itself.
        }
    }


}
