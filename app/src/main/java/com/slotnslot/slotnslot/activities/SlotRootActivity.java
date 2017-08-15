package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

public abstract class SlotRootActivity extends RxAppCompatActivity {

    private RelativeLayout _loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void setLoadingView(RelativeLayout loadingView) {
        this._loadingView = loadingView;
        _loadingView.setOnTouchListener((view, motionEvent) -> true);
    }

    public void loadingViewSetVisible(boolean isVisible) {
        _loadingView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
