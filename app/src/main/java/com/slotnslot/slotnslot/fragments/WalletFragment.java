package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.models.AccountViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.utils.Convert;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletFragment extends SlotRootFragment {

    @BindView(R.id.wallet_current_amount_textview)
    TextView currentAmountTextView;
    @BindView(R.id.wallet_address_textview)
    TextView addressTextView;

    private AccountViewModel accountViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);

        setAccountModel();
        return view;
    }

    private void setAccountModel() {
        accountViewModel = new AccountViewModel(AccountProvider.accountSubject);
        accountViewModel.addressHex
                .subscribe(hex -> addressTextView.setText(hex));
        accountViewModel.balance
                .subscribe(bigInteger -> currentAmountTextView.setText(Convert.fromWei(bigInteger, Convert.Unit.ETHER) + " ETH"));
    }

    @OnClick(R.id.wallet_change_eth_button)
    public void onClickChangeEthButton() {
        //TODO OnClickChangeEth
    }

    @OnClick(R.id.wallet_withdraw_eth_button)
    public void onClickWithdrawEthButton() {
        //TODO OnClickWithdrawEth
    }

    @OnClick(R.id.wallet_more_button)
    public void onClickMoreButton() {
        //TODO OnClickMoreButton
    }
}