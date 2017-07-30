package com.slotnslot.slotnslot.models;

import java.math.BigInteger;

import io.reactivex.Observable;

public class AccountViewModel {
    public Observable<Account> account;
    public Observable<BigInteger> balance;
    public Observable<String> addressHex;

    public AccountViewModel(Observable<Account> account) {
        this.account = account.distinctUntilChanged();
        balance = account.map(Account::getBalance).distinctUntilChanged();
        addressHex = account.map(Account::getAddressHex).distinctUntilChanged();
    }
}
