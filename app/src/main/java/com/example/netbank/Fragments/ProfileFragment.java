package com.example.netbank.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.netbank.Model.Customer;
import com.example.netbank.R;
import com.example.netbank.Repository.DatabaseHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseHandler dbHandler = new DatabaseHandler();


    EditText firstName, lastName;
    TextView mDisplayDate;
    Button updateProfile;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     Instantiates my view elements and sets onClickListeners after onCreateView() has been called
     * */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);

        mDisplayDate = view.findViewById(R.id.tvDate);
        mDisplayDate.setOnClickListener(this);

        updateProfile = view.findViewById(R.id.updateProfile);
        updateProfile.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: Has been called");
        int i = view.getId();

        if (i == R.id.tvDate) {
            showDatePicker();
        } else if (i == R.id.updateProfile) {
            dbHandler.createCustomer(firstName.getText().toString(), lastName.getText().toString(), mDisplayDate.getText().toString(), "test", user.getEmail());
        }


    }

    private void showDatePicker() {
        Log.d(TAG, "showDatePicker: Has been called");
        DatePickerFragment date = new DatePickerFragment();
        /**
         * Set Up Current Date Into dialog
         */
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            mDisplayDate.setText(getAge(year, monthOfYear, dayOfMonth));

        }

    };

    /**
     gets the age of a person based on input given from the datepicker
     * */
    private String getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }


}

