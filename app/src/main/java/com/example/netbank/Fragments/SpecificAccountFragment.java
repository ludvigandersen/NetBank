package com.example.netbank.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netbank.Model.Account;
import com.example.netbank.Model.TransactionParcelable;
import com.example.netbank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;


import static android.support.constraint.Constraints.TAG;

public class SpecificAccountFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    final static String TAG = "SpecificAccountFragment";

    TextView tvAccountName, tvAccountBalance;
    EditText transactionAmount, customRecipient;
    Spinner accountsSpinner;
    Button makeTransaction, customTransaction;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.specific_account_fragment, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAccountName = view.findViewById(R.id.tvAccountName);
        String accountName = getArguments().getString("accountName");
        tvAccountName.setText(getString(R.string.placeholder_account_display, accountName));

        tvAccountBalance = view.findViewById(R.id.tvAccountBalance);


        transactionAmount = view.findViewById(R.id.transactionAmount);
        customRecipient = view.findViewById(R.id.customRecipient);

        makeTransaction = view.findViewById(R.id.makeTransaction);
        makeTransaction.setOnClickListener(this);
        customTransaction = view.findViewById(R.id.customTransaction);
        customTransaction.setOnClickListener(this);

        accountsSpinner = view.findViewById(R.id.accountsSpinner);


        getAccount();
        spinnerSetup();


    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.makeTransaction) {
            if (!transactionAmount.getText().toString().equalsIgnoreCase("")) {
                if (Integer.valueOf(tvAccountBalance.getText().toString()) >= Integer.valueOf(transactionAmount.getText().toString())
                        && !accountsSpinner.getSelectedItem().toString().equalsIgnoreCase("Pension")) {
                    makeTransaction();
                } else if (accountsSpinner.getSelectedItem().toString().equalsIgnoreCase("pension")) {
                    verificationDialog("pension");
                } else {
                    Toast.makeText(getActivity(), "Transaction amount exceeds balance available",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "No transaction amount given",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.customTransaction) {
            if (!transactionAmount.getText().toString().equalsIgnoreCase("") && !customRecipient.getText().toString().equalsIgnoreCase("")) {
                if (Integer.valueOf(tvAccountBalance.getText().toString()) >= Integer.valueOf(transactionAmount.getText().toString())) {

                    verificationDialog("custom");

                } else {
                    Toast.makeText(getActivity(), "Transaction amount exceeds balance available",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "No transaction amount or recipient given",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getAccount() {
        Log.d(TAG, "getAccount: Has been called");
        DocumentReference docRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(getFirstWord(tvAccountName.getText().toString()));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Account account = documentSnapshot.toObject(Account.class);

                tvAccountBalance.setText(getString(R.string.placeholder_balance_display, account.getBalance()));

            }
        });
    }

    private void spinnerSetup() {

        db.collection("users").document(user.getEmail()).collection("accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> accountList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Account account = document.toObject(Account.class);


                                if (account.isAccountActive() != account.getAccountType().equalsIgnoreCase(getFirstWord(tvAccountName.getText().toString()))) {
                                    accountList.add(account.getAccountType());
                                } else if (account.getAccountType().equalsIgnoreCase("Pension")) {
                                    accountList.add(account.getAccountType());
                                }


                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            accountList.add("Custom recipient");
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accountList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            accountsSpinner.setAdapter(dataAdapter);
                            accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = adapterView.getItemAtPosition(i).toString();
                                    if (item.equalsIgnoreCase("Custom recipient")) {
                                        customRecipient.setVisibility(View.VISIBLE);
                                        customTransaction.setVisibility(View.VISIBLE);
                                        makeTransaction.setVisibility(View.GONE);
                                    } else {
                                        customRecipient.setVisibility(View.GONE);
                                        customTransaction.setVisibility(View.GONE);
                                        makeTransaction.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void makeTransaction() {
        Log.d(TAG, "makeTransaction: Has been called");
        final DocumentReference senderDocRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(getFirstWord(tvAccountName.getText().toString()));

        final DocumentReference recieverDocRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(accountsSpinner.getSelectedItem().toString());

        db.runTransaction(new Transaction.Function<Void>() {
            TransactionParcelable tp = new TransactionParcelable();

            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot sender = transaction.get(senderDocRef);
                DocumentSnapshot reciever = transaction.get(recieverDocRef);


                int senderBalance = sender.getLong("balance").intValue();
                int recieverBalance = reciever.getLong("balance").intValue();
                int transactionBalance = Math.abs(Integer.valueOf(transactionAmount.getText().toString()));

                transaction.update(senderDocRef, "balance", senderBalance - transactionBalance);
                transaction.update(recieverDocRef, "balance", recieverBalance + transactionBalance);


                AccountsFragment fragment = new AccountsFragment();
                Bundle bundle = new Bundle();
                tp.setConfirm("transaction");
                bundle.putParcelable("transaction", tp);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();


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

    private void makeCustomTransaction() {
        Log.d(TAG, "makeCustomTransaction: Has been called");
        final DocumentReference senderDocRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(getFirstWord(tvAccountName.getText().toString()));

        final DocumentReference recieverDocRef = db.collection("users").document(customRecipient.getText().toString())
                .collection("accounts").document("Default");

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot sender = transaction.get(senderDocRef);
                DocumentSnapshot reciever = transaction.get(recieverDocRef);


                int senderBalance = sender.getLong("balance").intValue();
                //TODO: Path virker ikke når autofill af recieve email bruges
                int recieverBalance = reciever.getLong("balance").intValue();
                int transactionBalance = Integer.valueOf(transactionAmount.getText().toString());

                transaction.update(senderDocRef, "balance", senderBalance - transactionBalance);
                transaction.update(recieverDocRef, "balance", recieverBalance + transactionBalance);


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

    public void verificationDialog(final String transactionType) {
        Log.d(TAG, "verificationDialog: Has been called");
        Log.d(TAG, "verificationDialog: Has been called");
        final EditText passVerification = new EditText(getActivity());


        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        alert.setTitle("Verification");
        alert.setView(passVerification);


        alert.setMessage(R.string.verification_text).setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (passVerification.getText().toString().equalsIgnoreCase("")) {
                    passVerification.setText(" ");
                }
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), passVerification.getText().toString());

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (transactionType.equalsIgnoreCase("custom")) {
                                        makeCustomTransaction();
                                    } else if (transactionType.equalsIgnoreCase("pension")) {
                                        makeTransaction();
                                    }
                                    Log.d(TAG, "User re-authenticated.");
                                } else {
                                    Toast.makeText(getActivity(), "Incorrect password, try again",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onComplete: failed transaction");
                                }

                            }
                        });

                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alert.create();

        alertDialog.show();

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
