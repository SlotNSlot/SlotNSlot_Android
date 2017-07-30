package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.TransactionManager;
import com.slotnslot.slotnslot.utils.Convert;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.ethereum.geth.Account;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionActivity extends RxAppCompatActivity {
    private static final String TAG = TransactionActivity.class.getSimpleName();

    @BindView(R.id.tx_password)
    EditText txPassword;
    @BindView(R.id.tx_sender_num)
    EditText txSenderNum;
    @BindView(R.id.tx_receiver_num)
    EditText txReceiverNum;
    @BindView(R.id.send_ether)
    EditText sendEther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ButterKnife.bind(this);

        Toast.makeText(getApplicationContext(), "Welcome to Transaction Test Activity...", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tx_send_btn)
    void sendTransaction() {
        String password = txPassword.getText().toString();
        int senderNum = Integer.parseInt((txSenderNum).getText().toString());
        int receiverNum = Integer.parseInt((txReceiverNum).getText().toString());
        double ether = Double.parseDouble((sendEther).getText().toString());

        try {
            List<Account> accounts = CredentialManager.getAccounts();
            Account sender = accounts.get(senderNum);
            Account receiver = accounts.get(receiverNum);

            CredentialManager.setDefault(sender, password);
            TransactionManager.sendFunds(receiver.getAddress().getHex(), Convert.toWei(ether, Convert.Unit.ETHER))
                    .subscribe(hash -> {
                        Toast.makeText(getApplicationContext(), "Transaction sent... hash : " + hash.getHex(), Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "transaction hash : " + hash.getHex());
                    });

            CredentialManager.setDefault(0, "asdf");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Fail to send transaction...", Toast.LENGTH_SHORT).show();
        }
    }
}
