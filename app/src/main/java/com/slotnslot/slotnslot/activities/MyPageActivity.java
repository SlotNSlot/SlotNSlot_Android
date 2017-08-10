package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.WalletFragment;

public class MyPageActivity extends SlotFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = new WalletFragment();
        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, fragment);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    public void setTitle(String title) {
        centerTextView.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
