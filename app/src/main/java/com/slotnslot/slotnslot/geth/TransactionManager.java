package com.slotnslot.slotnslot.geth;

import com.slotnslot.slotnslot.activities.example.TransactionActivity;

import org.ethereum.geth.Address;
import org.ethereum.geth.Hash;
import org.ethereum.geth.Receipt;

import java.math.BigInteger;

import io.reactivex.Observable;

public class TransactionManager {
    private static final String TAG = TransactionActivity.class.getSimpleName();

    private TransactionManager() {
    }

    public static Observable<String> executeCall(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) {
        return Utils.waitResponse(() -> new Transaction()
                .from(CredentialManager.getDefault().getAccount())
                .to(to)
                .value(value)
                .gasPrice(gasPrice)
                .gasAmount(gasLimit)
                .data(data)
                .call());
    }

    public static Observable<Receipt> executeTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) {
        return sendTransaction(gasPrice, gasLimit, to, data, value)
                .flatMap(TransactionManager::processResponse);
    }

    private static Observable<Hash> sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) {
        return Utils.waitResponse(() -> new Transaction()
                .from(CredentialManager.getDefault().getAccount())
                .to(to)
                .value(value)
                .gasPrice(gasPrice)
                .gasAmount(gasLimit)
                .data(data)
                .send());
    }

    public static Observable<Receipt> processResponse(Hash hash) {
        return Utils.waitResponse(() -> {
            if (hash == null) throw new GethException("getTransactionReceipt: hash is null.");
            return GethManager.getClient().getTransactionReceipt(GethManager.getMainContext(), new Hash(hash.getHex()));
        });
    }

    public static Observable<BigInteger> getBalanceAt() {
        return getBalanceAt(CredentialManager.getDefault().getAccount().getAddress());
    }

    public static Observable<BigInteger> getBalanceAt(Address address) {
        return Utils.waitResponse(() -> new BigInteger(
                GethManager
                        .getClient()
                        .getPendingBalanceAt(GethManager.getMainContext(), address)
                        .getString(10)));
    }

    public static Observable<Hash> sendFunds(String to, BigInteger value) {
        return Utils.waitResponse(() -> new Transaction()
                .to(to)
                .gasAmount(GethConstants.FUND_GAS_LIMIT)
                .value(value)
                .send());
    }

}
