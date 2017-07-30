package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.MakeSlotStepOneFragment;
import com.slotnslot.slotnslot.utils.Constants;

public class MakeSlotActivity extends SlotFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        centerTextView.setText("MAKE");

        Fragment frag = new MakeSlotStepOneFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_KEY_STEP_INDEX, 1);
        frag.setArguments(bundle);
        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_framelayout, frag);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    @Override
    public void onClickBackButton() {
        finish();
    }
}
