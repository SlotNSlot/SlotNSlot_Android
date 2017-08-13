package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;

import java.util.ArrayList;
import java.util.List;

public class CredentialManager {
    private static final String TAG = CredentialManager.class.getSimpleName();
    private static final String defaultKeyStoreDir = "/keystore";

    private static KeyStore keyStore;
    private static Credential defaultCredential;

    private CredentialManager() {
    }

    public static boolean setDefault(Account account, String passphrase) {
        try {
            keyStore.unlock(account, passphrase);
            keyStore.lock(account.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast("passphrase is incorrect.");
            return false;
        }

        setDefault(Credential.create(account, passphrase));
        return true;
    }

    public static boolean setDefault(int index, String passphrase) {
        Account account;
        try {
            account = keyStore.getAccounts().get(index);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast("cannot get account : " + e.getMessage());
            return false;
        }
        return setDefault(account, passphrase);
    }

    public static Credential getDefault() {
        if (defaultCredential == null)
            throw new GethException("default credential is not set yet.");
        return defaultCredential;
    }

    public static void setDefault(Credential credential) {
        defaultCredential = credential;
    }

    public static KeyStore getKeyStore() {
        if (keyStore == null) throw new GethException("keystore is not set yet.");
        return keyStore;
    }

    public static void setKeyStore() {
        keyStore = new KeyStore(Utils.getDataDir() + defaultKeyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
    }

    public static List<Account> getAccounts() {
        ArrayList<Account> list = new ArrayList<>();

        Accounts accounts = keyStore.getAccounts();
        if (accounts == null) {
            return list;
        }

        for (int i = 0; i < accounts.size(); i++) {
            try {
                list.add(accounts.get(i));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        return list;
    }

    public static String getDefaultAccountHex() {
        return defaultCredential.getAccount().getAddress().getHex();
    }

    public static long getDefaultNonce() {
        return defaultCredential.getNonce();
    }

    public static void updateDefaultNonce() {
        defaultCredential.updateNonce();
    }
}
