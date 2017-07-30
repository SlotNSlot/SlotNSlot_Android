package com.slotnslot.slotnslot.provider;

import android.text.TextUtils;

import com.slotnslot.slotnslot.geth.TransactionManager;
import com.slotnslot.slotnslot.models.Account;

import java.math.BigInteger;

import io.reactivex.subjects.BehaviorSubject;

public class AccountProvider {
    public static BehaviorSubject<Account> accountSubject = BehaviorSubject.create();
    public static Account account;

    private AccountProvider() {
    }

    public static void setAccount(Account account) {
        AccountProvider.account = account;
    }

    public static Account getAccount() {
        return account;
    }

    public static void updateBalance(BigInteger balance) {
        if (account == null || balance == null) return;
        account.setBalance(balance);
        accountSubject.onNext(account);
    }

    public static void getBalance() {
        TransactionManager
                .getBalanceAt()
                .subscribe(
                        AccountProvider::updateBalance,
                        Throwable::printStackTrace
                );
    }

    public static boolean identical(String address) {
        if (account == null || TextUtils.isEmpty(address)) {
            return false;
        }
        return address.equals(account.getAddressHex());
    }
}
