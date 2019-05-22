package com.example.netbank.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;


public class BillsFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    final static String TAG = "BillsFragment";

    TextView billText, accountBalance;
    Spinner billsSpinner, accountSpinner;
    Button payBill;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        billText = view.findViewById(R.id.billText);
        accountBalance = view.findViewById(R.id.accountBalance);

        payBill = view.findViewById(R.id.payBill);
        payBill.setOnClickListener(this);

        billsSpinner = view.findViewById(R.id.billsSpinner);
        accountSpinner = view.findViewById(R.id.accountSpinner);

        billsSpinnerSetup();
        accountsSpinnerSetup();

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Has been called");
        int i = v.getId();

        if (i == R.id.payBill){
            String acc = accountSpinner.getSelectedItem().toString();
            String balance = acc.substring(acc.lastIndexOf(" ")+1);

            String bill = billText.getText().toString();
            String amount = bill.substring(bill.lastIndexOf(" ")+1);
            if (Integer.valueOf(balance) >= Integer.valueOf(amount)){
                payBill();
                Toast.makeText(getActivity(), "Payment successful",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Insufficient funds on account",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    /**
     This method sets up the Bills Spinner on the BillsFragment, when bill is chosen on the navigation drawer,
     and shows the bills, associated with the user currently online, that has isPaid set to false
     */
    private void billsSpinnerSetup() {

        db.collection("companies").document("boligforeningen")
                .collection("customer").document(user.getEmail()).collection("bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    List<String> billsList = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (!document.getBoolean("isPaid")) {
                                    billsList.add(document.getId());
                                }


                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, billsList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            billsSpinner.setAdapter(dataAdapter);
                            billsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = adapterView.getItemAtPosition(i).toString();

                                    final DocumentReference docRef = db.collection("companies").document("boligforeningen")
                                            .collection("customer").document(user.getEmail()).collection("bills").document(item);
                                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {


                                            billText.setText(getString(R.string.placeholder_bill_display, "Boligforeningen", documentSnapshot.getString("info"),
                                                    documentSnapshot.getLong("amount").intValue()));

                                        }
                                    });

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                        }
                    }
                });
    }

    /**
     This method sets up the Account Spinner on the BillsFragment, when bill is chosen on the navigation drawer,
     and shows the accounts of the current user online, that has accountActive to true.
     */
    private void accountsSpinnerSetup(){
        Log.d(TAG, "accountsSpinnerSetup: Has been called");
        db.collection("users").document(user.getEmail()).collection("accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> accountList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Account account = document.toObject(Account.class);

                                if (account.isAccountActive()) {
                                    accountList.add(account.getAccountType() + " - " + account.getBalance());
                                }

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accountList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            accountSpinner.setAdapter(dataAdapter);
                            accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = adapterView.getItemAtPosition(i).toString();



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
    /**
     This method is associated with the onClick for the button payBill. This method has 3 Document references to the firestore database:
     Sender: which is the account of the current user online, that has been selected on the account spinner.
     the field in the senderDocRef gets it's 'balance' field reduced by the amount of the payment.
     Bill: which is the bill that the user has chosen on the bill spinner, which is the one to be paid
     the field in the billDocRef gets it's 'amount' field reduced by the amount of the payment.
     Reciever: which the company that has sent the bill to the user, and asked for payment.
     the field in the recieverDocRef gets it's field 'amount' increased by the amount of the payment.
     */
    private void payBill(){
        Log.d(TAG, "payBill: Has been called");
        final DocumentReference senderDocRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(getFirstWord(accountSpinner.getSelectedItem().toString()));

        final DocumentReference billDocRef = db.collection("companies").document("boligforeningen")
                .collection("customer").document(user.getEmail()).collection("bills")
                .document(billsSpinner.getSelectedItem().toString());

        final DocumentReference recieverDocRef = db.collection("companies").document("boligforeningen");

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot sender = transaction.get(senderDocRef);
                DocumentSnapshot billAmount = transaction.get(billDocRef);
                DocumentSnapshot reciever = transaction.get(recieverDocRef);


                int senderBalance = sender.getLong("balance").intValue();
                int billAmountBalance = billAmount.getLong("amount").intValue();
                int recieverBalance = reciever.getLong("amount").intValue();


                transaction.update(senderDocRef, "balance", senderBalance - billAmountBalance);
                transaction.update(billDocRef, "amount", 0);
                transaction.update(recieverDocRef, "amount", recieverBalance + billAmountBalance);
                transaction.update(billDocRef, "isPaid", true);


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

    private String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) { // Check if there is more than one word.
            return text.substring(0, index); // Extract first word.
        } else {
            return text; // Text is the first word itself.
        }
    }


}
