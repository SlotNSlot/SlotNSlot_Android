package com.slotnslot.slotnslot.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.slotnslot.slotnslot.geth.GethManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class GethNodeService extends Service {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY}, flag = true)
    @interface OnStartCommandFlag {
    }

    private static final String TAG = GethNodeService.class.getSimpleName();
    private GethManager gethManager = null;

    @Override
    public int onStartCommand(Intent intent, @OnStartCommandFlag int flags, int startId) {
        Completable
                .create(e -> {
                    this.gethManager = GethManager.getInstance();
                    this.gethManager.startNode(e);
                })
                .retry()
                .subscribeOn(Schedulers.computation())
                .subscribe();

        return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
    }

    @Override
    public void onDestroy() {
        if (this.gethManager != null) {
            this.gethManager.stopNode();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
