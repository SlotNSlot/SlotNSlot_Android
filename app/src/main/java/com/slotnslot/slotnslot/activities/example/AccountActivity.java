package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.GethManager;
import com.slotnslot.slotnslot.geth.TransactionManager;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.ethereum.geth.Account;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Node;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AccountActivity extends RxAppCompatActivity {
    private static final String TAG = AccountActivity.class.getSimpleName();

    @BindView(R.id.acc_async_txt)
    TextView accAsyncTxt;
    @BindView(R.id.acc_list_txt)
    TextView accListTxt;
    @BindView(R.id.acc_bal_txt)
    TextView accBalTxt;
    @BindView(R.id.acc_password)
    EditText accPassword;
    @BindView(R.id.del_acc_num)
    EditText delAccNum;
    @BindView(R.id.acc_check_acc_btn)
    Button accCheckAccBtn;
    @BindView(R.id.acc_create_btn)
    Button accCreateBtn;
    @BindView(R.id.del_btn)
    Button delBtn;

    private GethManager manager;
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);

        Toast.makeText(getApplicationContext(), "Welcome to Account Test Activity...", Toast.LENGTH_SHORT).show();

        manager = GethManager.getInstance();
        node = manager.getNode();

        createAccount();
        deleteAccount();
        checkBalance();

        asyncCheck();
    }

    private void asyncCheck() {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> accAsyncTxt.setText("" + aLong));
    }

    @OnClick(R.id.acc_list_btn)
    void getAccountList() {
        try {
            List<Account> accounts = CredentialManager.getAccounts();
            accListTxt.setText("account list...\n");
            for (int i = 0; i < accounts.size(); i++) {
                Account account = accounts.get(i);
                accListTxt.append("#" + i + " address : " + account.getAddress().getHex() + "\n");
                Log.i(TAG, account.getAddress().getHex());
                Log.i(TAG, account.getURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Fail to get account list...", Toast.LENGTH_SHORT).show();
        }
    }

    void createAccount() {
        RxView
                .clicks(accCreateBtn)
                .compose(bindToLifecycle())
                .subscribe(event -> {
                    String password = accPassword.getText().toString();

                    KeyStore keyStore = CredentialManager.getKeyStore();
                    Account account = keyStore.newAccount(password);

                    Toast.makeText(getApplicationContext(), "Create new account... address : " + account.getAddress().getHex(), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, account.getAddress().getHex());
                    Log.i(TAG, account.getURL());

                    getAccountList();
                }, err -> Log.e(TAG, err.getLocalizedMessage()));
    }

    void deleteAccount() {
        RxView
                .clicks(delBtn)
                .compose(bindToLifecycle())
                .subscribe(event -> {
                    String password = accPassword.getText().toString();
                    int accNum = Integer.parseInt(delAccNum.getText().toString());

                    KeyStore keyStore = CredentialManager.getKeyStore();
                    Account account = keyStore.getAccounts().get(accNum);

                    Toast.makeText(getApplicationContext(), "Delete account... address : " + account.getAddress(), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, account.getAddress().getHex());
                    Log.i(TAG, account.getURL());

                    keyStore.deleteAccount(account, password);

                    getAccountList();
                }, err -> Log.e(TAG, err.getLocalizedMessage()));
    }

    void checkBalance() {
        RxView
                .clicks(accCheckAccBtn)
                .compose(bindToLifecycle())
                .flatMap(o -> {
                    int accNum = Integer.parseInt(delAccNum.getText().toString());

                    KeyStore keyStore = CredentialManager.getKeyStore();
                    Account account = keyStore.getAccounts().get(accNum);

                    return TransactionManager.getBalanceAt(account.getAddress());
                })
                .subscribe(balanceAt -> {
                    accBalTxt.setText("Balance of account is " + balanceAt);
                    Toast.makeText(getApplicationContext(), "Balance of account is " + balanceAt, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "balanceAt : " + balanceAt);

                    Log.i(TAG, "sub: run on thread " + Thread.currentThread().getName());
                }, err -> Log.e(TAG, err.getLocalizedMessage()));
    }
}
