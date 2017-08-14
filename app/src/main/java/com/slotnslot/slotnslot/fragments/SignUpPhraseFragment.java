package com.slotnslot.slotnslot.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.SignInUpActivity;
import com.slotnslot.slotnslot.activities.SlotMainActivity;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.provider.AccountProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpPhraseFragment extends SlotRootFragment {

    @BindView(R.id.address)
    TextView address;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SignInUpActivity) getActivity()).setTitle("Sign up");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_pass_phrase, container, false);
        ButterKnife.bind(this, view);

        address.setText(AccountProvider.getAccount().getAddressHex());
        return view;
    }

    @OnClick(R.id.signup_pass_phrase_copy_button)
    public void onClickCopyButton() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", address.getText());
        clipboard.setPrimaryClip(clip);

        Utils.showToast("Wallet address copied.");
    }

    @OnClick(R.id.signup_pass_phrase_done_button)
    public void onClickDoneButton() {
        Intent intent = new Intent(getContext(), SlotMainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
