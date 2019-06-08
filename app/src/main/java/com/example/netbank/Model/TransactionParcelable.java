package com.example.netbank.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionParcelable implements Parcelable {

    private String confirm;

    public TransactionParcelable() {
    }

    public TransactionParcelable(String confirm) {
        this.confirm = confirm;
    }

    protected TransactionParcelable(Parcel in) {
        confirm = in.readString();
    }

    public static final Creator<TransactionParcelable> CREATOR = new Creator<TransactionParcelable>() {
        @Override
        public TransactionParcelable createFromParcel(Parcel in) {
            return new TransactionParcelable(in);
        }

        @Override
        public TransactionParcelable[] newArray(int size) {
            return new TransactionParcelable[size];
        }
    };

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(confirm);
    }


}
