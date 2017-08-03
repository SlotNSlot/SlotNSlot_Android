package com.slotnslot.slotnslot.utils;

import android.util.Log;

import com.slotnslot.slotnslot.geth.Utils;

import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

public class ErrorConsumer implements Consumer<Throwable> {

    private String msg;
    private String tag;

    public ErrorConsumer(String msg) {
        this.msg = msg;
    }

    public ErrorConsumer(String tag, String msg) {
        this(msg);
        this.tag = tag;
    }

    @Override
    public void accept(Throwable t) {
        if (tag != null) {
            Log.e(tag, msg, t);
        } else {
            Utils.showToast(msg);
        }
    }
}

