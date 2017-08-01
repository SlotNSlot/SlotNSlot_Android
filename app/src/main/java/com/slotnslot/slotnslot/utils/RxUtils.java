package com.slotnslot.slotnslot.utils;

import android.util.Log;

import io.reactivex.Notification;
import io.reactivex.functions.Consumer;

public class RxUtils {
    public static <T> Consumer<Notification<? super T>> log(String tag, String description) {
        return (Notification<? super T> notification) -> {
            if (notification.isOnNext()) {
                Log.d(tag, description);
            } else if (notification.isOnError()) {
                Log.d(tag, description);
            } else if (notification.isOnComplete()) {
                Log.d(tag, description);
            }
        };
    }
}
