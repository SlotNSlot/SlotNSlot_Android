package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.SignInFragment;
import com.slotnslot.slotnslot.fragments.SignInListFragment;
import com.slotnslot.slotnslot.fragments.SignUpFragment;
import com.slotnslot.slotnslot.geth.GethManager;
import com.slotnslot.slotnslot.geth.Utils;

public class SignInUpActivity extends SlotFragmentActivity {

    private boolean doubleBackToExitPressedOnce;

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

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_framelayout);
        if (f instanceof SignInListFragment || f instanceof SignInFragment) {
            super.onBackPressed();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            finish();
            GethManager.getInstance().stopNode();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Utils.showToast("press back again to exit");
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);
    }
}
