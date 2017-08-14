package com.slotnslot.slotnslot.geth;

import android.util.Log;

import com.slotnslot.slotnslot.utils.Convert;

import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.CallMsg;
import org.ethereum.geth.Context;
import org.ethereum.geth.Hash;

import java.math.BigInteger;

public class Transaction {
    public static final String TAG = Transaction.class.getSimpleName();

    private long nonce;
    private Account from;
    private Address to;
    private BigInteger value;
    private BigInteger gas;
    private BigInteger gasPrice;
    private byte[] data;

    private Context txContext;

    private Transaction(long nonce, Account from, Address to, BigInteger value, BigInteger gas, BigInteger gasPrice, String data, Context txContext) {
        this.txContext = txContext;

        this.nonce = nonce;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.data = Utils.hexToByte(data);
    }

    Transaction() throws Exception {
        this(0, null, null, null, null, null, null, null);
        txContext = GethManager.getMainContext();

        from = CredentialManager.getDefault().getAccount();
        if (from != null) {
            to = from.getAddress();
        }
    }

    public Transaction nonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public Transaction to(Address account) {
        if (to == null) throw new GethException();
        this.to = account;
        return this;
    }

    public Transaction to(String address) throws Exception {
        return to(new Address(address));
    }

    public Transaction from(Account account) {
        if (account == null) throw new GethException();
        this.from = account;
        return this;
    }

    public Transaction value(BigInteger value) {
        this.value = value;
        return this;
    }

    public Transaction gasAmount(BigInteger gas) {
        this.gas = gas;
        return this;
    }

    public Transaction gasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public Transaction data(byte[] data) {
        this.data = data;
        return this;
    }

    public Transaction data(String data) {
        return data(Utils.hexToByte(data));
    }

    public Transaction context(Context context) {
        this.txContext = context;
        return this;
    }

    private boolean checkValidity() throws Exception {
        if (to == null) throw new GethException();
        if (from == null) throw new GethException();
        return true;
    }

    private org.ethereum.geth.Transaction getRawTransaction() throws Exception {
        if (to == null) throw new GethException();
        if (nonce == 0 && from != null) {
//            nonce = GethManager.getClient().getPendingNonceAt(txContext, from.getAddress());
            nonce = CredentialManager.getDefaultNonce();
        }
        if (gas == null) {
            gas = GethConstants.DEFAULT_GAS_LIMIT;
        }
        if (gasPrice == null) {
//            gasPrice = BigInteger.valueOf(GethManager.getClient().suggestGasPrice(txContext).getInt64());
            gasPrice = GethConstants.DEFAULT_GAS_PRICE; // for testnet only
        }
        return new org.ethereum.geth.Transaction(
                nonce, // nonce
                to, // receiver address
                Convert.toBigInt(value), // wei to send
                Convert.toBigInt(gas), // gas limit
                Convert.toBigInt(gasPrice), // gas price
                data // data to send
        );
    }

    private CallMsg toCallMessage() throws Exception {
        checkValidity();

        CallMsg ret = new CallMsg();
        ret.setFrom(from.getAddress());
        ret.setTo(to);
        ret.setValue(Convert.toBigInt(value));
        ret.setGas(gas == null ? 0 : gas.longValue());
        ret.setGasPrice(Convert.toBigInt(gasPrice));
        ret.setData(data);

        return ret;
    }

    String call() throws Exception {
        byte[] hexadecimalResult = GethManager.getClient().pendingCallContract(txContext, toCallMessage());
        Log.d(TAG, "======= FIRST CALL DATA : " + Utils.byteToHex(hexadecimalResult));

        // this because first contract call returns null
        if (hexadecimalResult == null) {
            hexadecimalResult = GethManager.getClient().pendingCallContract(txContext, toCallMessage());
            Log.d(TAG, "======= SECOND CALL DATA : " + Utils.byteToHex(hexadecimalResult));
        }

        if (hexadecimalResult == null) {
            return "0x";
        }
        return Utils.byteToHex(hexadecimalResult);
    }

    Hash send() throws Exception {
        org.ethereum.geth.Transaction raw = getRawTransaction();
        org.ethereum.geth.Transaction signed = CredentialManager.getDefault().sign(raw);

        // transaction.getHash().getHex() -> ???
        // transaction.getSigHash().getHex() == signed.getSigHash().getHex()
        // signed.getHash().getHex() -> fullhash

        Log.d(TAG, "======= SENDING TX : nonce - " + signed.getNonce() + ", data - " + Utils.byteToHex(signed.getData()));
        GethManager.getClient().sendTransaction(GethManager.getMainContext(), signed);
        CredentialManager.updateDefaultNonce();
        return signed.getHash();
    }
}
