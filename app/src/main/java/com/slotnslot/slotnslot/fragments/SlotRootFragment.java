package com.slotnslot.slotnslot.fragments;

import android.support.v4.app.Fragment;

import com.slotnslot.slotnslot.activities.SlotRootActivity;

public abstract class SlotRootFragment extends Fragment {

    public void loadingViewSetVisible(boolean isVisible) {
        ((SlotRootActivity)getActivity()).loadingViewSetVisible(isVisible);
    }
}
