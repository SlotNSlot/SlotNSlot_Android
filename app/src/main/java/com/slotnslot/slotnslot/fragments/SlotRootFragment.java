package com.slotnslot.slotnslot.fragments;

import com.slotnslot.slotnslot.activities.SlotRootActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

public abstract class SlotRootFragment extends RxFragment {

    public void loadingViewSetVisible(boolean isVisible) {
        ((SlotRootActivity) getActivity()).loadingViewSetVisible(isVisible);
    }
}
