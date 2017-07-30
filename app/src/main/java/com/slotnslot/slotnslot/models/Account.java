package com.slotnslot.slotnslot.models;

import java.io.Serializable;
import java.math.BigInteger;

public class Account implements Serializable {
    private org.ethereum.geth.Account account;
    private BigInteger balance;

    public Account(org.ethereum.geth.Account account) {
        this.account = account;
    }

    public String getAddressHex() {
        return account.getAddress().getHex();
    }

    public org.ethereum.geth.Account getAccount() {
        return account;
    }

    public void setAccount(org.ethereum.geth.Account account) {
        this.account = account;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }
}
