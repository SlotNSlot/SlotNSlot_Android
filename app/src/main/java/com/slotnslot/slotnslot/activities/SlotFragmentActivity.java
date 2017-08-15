package com.slotnslot.slotnslot.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SlotFragmentActivity extends SlotRootActivity {

    @BindView(R.id.toolbar_center_tetview)
    TextView centerTextView;
    @BindView(R.id.global_loading_container)
    RelativeLayout loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        setLoadingView(loadingView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back_button);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onClickBackButton();
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    public void onClickBackButton() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }
}
