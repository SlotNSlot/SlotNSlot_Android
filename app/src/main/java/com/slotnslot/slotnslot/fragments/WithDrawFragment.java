package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.MyPageActivity;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.TransactionManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.AccountViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.utils.Convert;

import org.ethereum.geth.Account;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;

public class WithDrawFragment extends SlotRootFragment {
    private static final String TAG = WithDrawFragment.class.getSimpleName();

    @BindView(R.id.withdraw_address_edittext)
    EditText withdrawAddress;
    @BindView(R.id.withdraw_address_border_view)
    View addressBorder;
    @BindView(R.id.withdraw_amount_edittext)
    EditText withdrawAmount;
    @BindView(R.id.withdraw_amount_border_view)
    View amountBorder;
    @BindView(R.id.withdraw_current_balance_textview)
    TextView currentBalance;

    private AccountViewModel accountViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_withdraw, container, false);
        ButterKnife.bind(this, view);

        withdrawAddress.setOnFocusChangeListener((v, isFocus) -> addressBorder.setBackgroundColor(getBorderBackgroundColor(isFocus)));
        withdrawAmount.setOnFocusChangeListener((v, isFocus) -> amountBorder.setBackgroundColor(getBorderBackgroundColor(isFocus)));

        accountViewModel = new AccountViewModel(AccountProvider.accountSubject);

        updateBalance();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MyPageActivity)getActivity()).setTitle("Withdraw ETH");
    }

    private void updateBalance() {
        accountViewModel.balance
                .subscribe(bigInteger -> {
                    BigDecimal ether = Convert.fromWei(bigInteger, Convert.Unit.ETHER);
                    currentBalance.setText("Current Balance : " + ether);
                });
    }

    private int getBorderBackgroundColor(boolean isFocus) {
        return ContextCompat.getColor(getContext(), isFocus ? R.color.pink1 : R.color.default_border_color);
    }


    @OnClick(R.id.withdraw_btn)
    void withdraw() {
        String toAddress = withdrawAddress.getText().toString();
        double etherAmount = Double.parseDouble(withdrawAmount.getText().toString());

        TransactionManager.sendFunds(toAddress, Convert.toWei(etherAmount, Convert.Unit.ETHER))
                .subscribe(
                        hash -> {
                            Log.i(TAG, "ether sent, hash : " + hash.getHex());
                            withdrawAmount.setText("");
                            Utils.showToast("sent [" + etherAmount + "] ETH to address : " + toAddress);
                            AccountProvider.updateBalance();
                            TransactionManager.processResponse(hash)
                                    .subscribe(receipt -> AccountProvider.updateBalance());
                        },
                        Throwable::printStackTrace);
    }
}