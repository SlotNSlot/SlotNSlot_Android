package com.slotnslot.slotnslot.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.SignInUpActivity;
import com.slotnslot.slotnslot.activities.SlotMainActivity;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.Account;
import com.slotnslot.slotnslot.provider.AccountProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInFragment extends SlotRootFragment {
    private static final String TAG = SignInFragment.class.getSimpleName();

    @BindView(R.id.signin_title_textview)
    TextView titleTextView;
    @BindView(R.id.signin_password_input_edittext)
    EditText passwordInputEditText;
    @BindView(R.id.signin_auto_signin_checkbox)
    CheckBox autoSignInCheckbox;

    private Account account;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SignInUpActivity) getActivity()).setTitle("Sign in");

        account = (Account) getArguments().getSerializable("account");
        showKeyboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        ButterKnife.bind(this, view);

        if (account != null) {
            titleTextView.setText(account.getAddressHex());
        }
        return view;
    }

    @OnClick(R.id.signin_signin_button)
    public void onClickSignInButton() {
        //TODO SIGN IN

        String passphrase = passwordInputEditText.getText().toString();
        if (TextUtils.isEmpty(passphrase)) {
            Utils.showToast("passphrase is empty.");
            return;
        }

        boolean success = CredentialManager.setDefault(account.getAccount(), passphrase);
        if (!success) {
            Utils.showToast("enter correct passphrase.");
            return;
        }
        AccountProvider.setAccount(account);

        Intent intent = new Intent(getContext(), SlotMainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
