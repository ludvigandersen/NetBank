package com.example.netbank.Model;

public class Account {

    private String accountType;
    private int balance;
    private boolean accountActive;


    public Account() {
    }

    public Account(String accountType, int balance, boolean accountActive) {
        this.accountType = accountType;
        this.balance = balance;
        this.accountActive = accountActive;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }
}
