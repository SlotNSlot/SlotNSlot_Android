package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.slotnslot.slotnslot.MyPageType;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.WalletFragment;
import com.slotnslot.slotnslot.fragments.WithDrawFragment;
import com.slotnslot.slotnslot.utils.Constants;

public class MyPageActivity extends SlotFragmentActivity {

    private Fragment fragment;
    private MyPageType myPageType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPageType = (MyPageType)getIntent().getSerializableExtra(Constants.ACTIVITY_EXTRA_KEY_MY_PAGE_TYPE);
        if (myPageType == MyPageType.WALLET) {
            setTitle(Constants.MY_PAGE_TITLE_WALLET);
            fragment = new WalletFragment();
        } else {
            setTitle(Constants.MY_PAGE_TITLE_WITHDRAW_ETH);
            fragment = new WithDrawFragment();
        }

        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, fragment);
        ftrans.commit();
    }

    public void setTitle(String title) {
        centerTextView.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (myPageType == MyPageType.WALLET) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
