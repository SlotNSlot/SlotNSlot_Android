package com.slotnslot.slotnslot;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.slotnslot.slotnslot.service.GethNodeService;

public class MainApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        startService(new Intent(this, GethNodeService.class));
    }

    public static Context getContext() {
        return context;
    }
}
