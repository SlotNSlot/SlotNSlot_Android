package com.slotnslot.slotnslot.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.slotnslot.slotnslot.ListType;
import com.slotnslot.slotnslot.fragments.MakeListFragment;
import com.slotnslot.slotnslot.fragments.PlayListFragment;
import com.slotnslot.slotnslot.utils.Constants;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        // Returning the current tabs
        switch (position) {
            case 0:
                PlayListFragment playListFragment = new PlayListFragment();
                bundle.putSerializable(Constants.BUNDLE_KEY_LIST_TYPE, ListType.PLAY);
                playListFragment.setArguments(bundle);
                return playListFragment;
            case 1:
                MakeListFragment makeListFragment = new MakeListFragment();
                bundle.putSerializable(Constants.BUNDLE_KEY_LIST_TYPE, ListType.MAKE);
                makeListFragment.setArguments(bundle);
                return makeListFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}