package com.slotnslot.slotnslot.activities;

import android.content.Intent;
import android.os.Bundle;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.example.MainActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LandingPageActivity extends SlotRootActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((interval) -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}
