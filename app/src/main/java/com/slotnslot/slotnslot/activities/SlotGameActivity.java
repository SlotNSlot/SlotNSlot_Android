package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.SlotType;
import com.slotnslot.slotnslot.fragments.SlotBankerFragment;
import com.slotnslot.slotnslot.fragments.SlotPlayerFragment;
import com.slotnslot.slotnslot.utils.Constants;

public class SlotGameActivity extends SlotFragmentActivity {

    private Fragment fragment;
    private SlotType slotType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        slotType = (SlotType) getIntent().getSerializableExtra(Constants.ACTIVITY_EXTRA_KEY_SLOT_TYPE);
        if (slotType == SlotType.BANKER) {
            setTitle(Constants.SLOT_BANKER_TITLE);
            fragment = new SlotBankerFragment();
        } else {
            setTitle(Constants.SLOT_PLAYER_TITLE);
            fragment = new SlotPlayerFragment();
        }
        fragment.setArguments(getIntent().getExtras());
        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, fragment);
        ftrans.commit();
    }

    public void setTitle(String title) {
        centerTextView.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.play_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit game?")
                .setPositiveButton("Yes", (dialog, id) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}
