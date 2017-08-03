package com.slotnslot.slotnslot.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.SignInUpActivity;
import com.slotnslot.slotnslot.geth.Credential;
import com.slotnslot.slotnslot.geth.CredentialManager;

import org.ethereum.geth.Account;
import org.ethereum.geth.KeyStore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SignUpFragment extends SlotRootFragment {
    private static final String TAG = SignUpFragment.class.getSimpleName();

    enum TransFormation {
        SHOW, HIDE
    }

    enum FocusView {
        PASSWORD, PASSWORD_CONFIRM
    }

    @BindView(R.id.signup_password_show_button)
    Button passwordShowButton;
    @BindView(R.id.signup_password_input_edittext)
    EditText passwordInputEditText;
    @BindView(R.id.signup_password_border_view)
    View passwordBorderView;
    @BindView(R.id.signup_password_confirm_show_button)
    Button passwordConfirmShowButton;
    @BindView(R.id.signup_password_confirm_input_edittext)
    EditText passwordConfirmInputEditText;
    @BindView(R.id.signup_password_confirm_border_view)
    View passwordConfirmBorderView;

    private TransFormation passwordTransFormation = TransFormation.HIDE;
    private TransFormation passwordConfirmTransFormation = TransFormation.HIDE;
    private int focusColor;
    private int defaultColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SignInUpActivity) getActivity()).setTitle("Sign up");

        focusColor = ContextCompat.getColor(getContext(), R.color.pink1);
        defaultColor = ContextCompat.getColor(getContext(), R.color.white);

        showKeyboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_password, container, false);
        ButterKnife.bind(this, view);

        passwordInputEditText.setOnFocusChangeListener((view1, focus) -> setFocusEditText(FocusView.PASSWORD, focus));
        passwordConfirmInputEditText.setOnFocusChangeListener((view1, focus) -> setFocusEditText(FocusView.PASSWORD_CONFIRM, focus));
        return view;
    }

    @OnClick({R.id.signup_password_show_button, R.id.signup_password_confirm_show_button})
    public void passwordShow(View v) {
        int index;
        switch (v.getId()) {
            case R.id.signup_password_show_button:
                index = passwordInputEditText.getSelectionStart();
                passwordTransFormation = reverseTransFormation(passwordTransFormation);
                setInputTypeButtonText(passwordShowButton, passwordTransFormation);
                setTransFormationEditText(passwordInputEditText, passwordTransFormation);
                passwordInputEditText.setSelection(index);
                break;
            case R.id.signup_password_confirm_show_button:
                index = passwordConfirmInputEditText.getSelectionStart();
                passwordConfirmTransFormation = reverseTransFormation(passwordConfirmTransFormation);
                setInputTypeButtonText(passwordConfirmShowButton, passwordConfirmTransFormation);
                setTransFormationEditText(passwordConfirmInputEditText, passwordConfirmTransFormation);
                passwordConfirmInputEditText.setSelection(index);
                break;
        }
    }

    @OnClick(R.id.signup_button)
    public void onClickSignUpButton() {
        if (TextUtils.isEmpty(passwordInputEditText.getText().toString())) {
            Log.i(TAG, "please enter passphrase.");
            return;
        }
        if (!passwordInputEditText.getText().toString().equals(passwordConfirmInputEditText.getText().toString())) {
            Log.i(TAG, "please enter same passphrase.");
            return;
        }

        Completable
                .create(e -> {
                    KeyStore keyStore = CredentialManager.getKeyStore();

                    String passphrase = passwordInputEditText.getText().toString();
                    Account account = keyStore.newAccount(passphrase);

                    CredentialManager.setDefault(Credential.create(account, passphrase));

                    e.onComplete();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Fragment frag = new SignUpPhraseFragment();
                            FragmentManager fmanager = getActivity().getSupportFragmentManager();
                            FragmentTransaction ftrans = fmanager.beginTransaction();
                            ftrans.replace(R.id.fragment_framelayout, frag);
                            ftrans.addToBackStack(null);
                            ftrans.commit();
                        },
                        e -> {
                            e.printStackTrace();
                            Log.i(TAG, "fail to create account : " + e.getMessage());
                        });
    }

    @OnClick(R.id.signup_signin_textview)
    public void onClickSignIn() {
        //TODO Move To SignIn Fragment
        Fragment frag = new SignInListFragment();
        FragmentManager fmanager = getActivity().getSupportFragmentManager();
        FragmentTransaction ftrans = fmanager.beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, frag);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    private void setTransFormationEditText(EditText text, TransFormation transFormation) {
        text.setInputType(transFormation == TransFormation.HIDE ? 129 : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private TransFormation reverseTransFormation(TransFormation transFormation) {
        return transFormation == TransFormation.SHOW ? TransFormation.HIDE : TransFormation.SHOW;
    }

    private void setInputTypeButtonText(Button button, TransFormation transFormation) {
        button.setText(transFormation == TransFormation.SHOW ? "HIDE" : "SHOW");
    }

    private void setFocusEditText(FocusView focusView, boolean isFocus) {
        int color = isFocus ? focusColor : defaultColor;
        switch (focusView) {
            case PASSWORD:
                passwordInputEditText.setTextColor(color);
                passwordBorderView.setBackgroundColor(color);
                passwordShowButton.setTextColor(color);
                if (isFocus) setFocusEditText(FocusView.PASSWORD_CONFIRM, false);
                break;
            case PASSWORD_CONFIRM:
                passwordConfirmInputEditText.setTextColor(color);
                passwordConfirmBorderView.setBackgroundColor(color);
                passwordConfirmShowButton.setTextColor(color);
                if (isFocus) setFocusEditText(FocusView.PASSWORD, false);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingViewSetVisible(false);
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
