// ProcyonCoinWallet.java
package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class ProcyonCoinWallet {
    private double balance;
    private List<Transaction> transactionHistory;
    private WalletAddress walletAddress;

    public ProcyonCoinWallet(double initialBalance) {
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        this.walletAddress = new WalletAddress();
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public WalletAddress getWalletAddress() {
        return walletAddress;
    }

    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        balance += transaction.getAmount();
    }
}
