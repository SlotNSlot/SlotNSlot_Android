package com.slotnslot.slotnslot.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.MyPageActivity;
import com.slotnslot.slotnslot.geth.Utils;
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
    @BindView(R.id.wallet_withdraw_eth_button)
    Button withdrawEthButton;
    @BindView(R.id.wallet_change_eth_button)
    Button changeButton;
    @BindView(R.id.wallet_more_button)
    ImageButton moreButton;

    private AccountViewModel accountViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);

        RxView.clicks(withdrawEthButton).subscribe(v -> {
            Fragment fragment = new WithDrawFragment();
            FragmentTransaction ftrans = getFragmentManager().beginTransaction();
            ftrans.replace(R.id.fragment_framelayout, fragment);
            ftrans.addToBackStack(null);
            ftrans.commit();
        });
        RxView.clicks(changeButton).subscribe(v -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", addressTextView.getText());
            clipboard.setPrimaryClip(clip);

            Utils.showToast("Wallet address copied. It will be charged when you send Rinkeby testnet Ether to this address.");
        });
        RxView.clicks(moreButton).subscribe(v -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", addressTextView.getText());
            clipboard.setPrimaryClip(clip);

            Utils.showToast("Wallet address copied.");
        });

        setAccountModel();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MyPageActivity)getActivity()).setTitle("Wallet");
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