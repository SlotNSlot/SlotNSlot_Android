package com.slotnslot.slotnslot;

import android.app.Application;
import android.content.Intent;

import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.service.GethNodeService;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CredentialManager.setKeyStore(getApplicationContext().getFilesDir().getPath());
        startService(new Intent(this, GethNodeService.class));
    }
}
