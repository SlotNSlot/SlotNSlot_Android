package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

public abstract class SlotRootActivity extends RxAppCompatActivity {

    private RelativeLayout _loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setLoadingView(RelativeLayout loadingView) {
        this._loadingView = loadingView;
    }

    public void loadingViewSetVisible(boolean isVisible) {
        _loadingView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
