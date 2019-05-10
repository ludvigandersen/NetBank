package com.example.netbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;



public class AccountsFragment extends Fragment implements View.OnClickListener {

    Button signOut;
    TextView tvAccounts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signOut = view.findViewById(R.id.acc_signOut);
        tvAccounts = view.findViewById(R.id.tvAccounts);

        signOut.setOnClickListener(this);

    }



    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.acc_signOut){

        }
    }
}
