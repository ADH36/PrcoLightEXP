// WalletManager.java
package com.example.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WalletManager {
    private List<ProcyonCoinWallet> wallets;

    public WalletManager() {
        this.wallets = new ArrayList<>();
    }

    public ProcyonCoinWallet createWallet(double initialBalance) {
        ProcyonCoinWallet wallet = new ProcyonCoinWallet(initialBalance);
        wallets.add(wallet);
        return wallet;
    }

    public void saveWallet(ProcyonCoinWallet wallet, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(wallet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProcyonCoinWallet loadWallet(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (ProcyonCoinWallet) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
