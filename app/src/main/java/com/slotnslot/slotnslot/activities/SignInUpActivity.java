package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.SignUpFragment;

public class SignInUpActivity extends SlotFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment frag = new SignUpFragment();
        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, frag);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    public void setTitle(String title) {
        centerTextView.setText(title);
    }
}
