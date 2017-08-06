package com.slotnslot.slotnslot.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.slotnslot.slotnslot.MainApplication;

public class StorageUtil {
    public static void save(String identifier, String key, Object object) {
        Gson gson = new Gson();
        SharedPreferences pref = MainApplication.getContext().getSharedPreferences(identifier, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, gson.toJson(object));
        editor.apply();
    }

    public static <T> T load(String identifier, String key, Class<T> clazz) {
        Gson gson = new Gson();
        SharedPreferences pref = MainApplication.getContext().getSharedPreferences(identifier, Context.MODE_PRIVATE);
        String json = pref.getString(key, null);
        return json == null ? null : gson.fromJson(json, clazz);
    }
}
