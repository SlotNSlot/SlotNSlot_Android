package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Account;
import org.ethereum.geth.BigInt;

public class Credential {
    private final Account account;
    private final String passphrase;
    private long nonce;

    private Credential(Account account, String passphrase) {
        this.account = account;
        this.passphrase = passphrase;
    }

    public static Credential create(Account account, String passphrase) {
        Credential credential = new Credential(account, passphrase);
        credential.syncNonce();
        return credential;
    }

    public Account getAccount() {
        return account;
    }

    public long getNonce() {
        return nonce;
    }

    public long syncNonce() {
        try {
            nonce = GethManager.getClient().getPendingNonceAt(GethManager.getMainContext(), account.getAddress());
            return nonce;
        } catch (Exception e) {
            throw new GethException("fail to sync nonce.", e);
        }
    }

    public long updateNonce() {
        return ++nonce;
    }

    public org.ethereum.geth.Transaction sign(org.ethereum.geth.Transaction transaction) throws Exception {
        long chainId = GethManager.getNetworkConfig().getNetworkID();

        return CredentialManager.getKeyStore().signTxPassphrase(
                account, // sender
                passphrase, // passphrase
                transaction,  // transaction
                new BigInt(chainId) // chain id
        );
    }
}
