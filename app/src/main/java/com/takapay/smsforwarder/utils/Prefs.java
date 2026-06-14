package com.takapay.smsforwarder.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREF_NAME = "takapay_prefs";

    private static SharedPreferences get(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveString(Context ctx, String key, String value) {
        get(ctx).edit().putString(key, value).apply();
    }

    public static String getString(Context ctx, String key, String def) {
        return get(ctx).getString(key, def);
    }

    public static void saveBool(Context ctx, String key, boolean value) {
        get(ctx).edit().putBoolean(key, value).apply();
    }

    public static boolean getBool(Context ctx, String key, boolean def) {
        return get(ctx).getBoolean(key, def);
    }

    public static final String SERVER_URL = "server_url";
    public static final String API_KEY = "api_key";
    public static final String IS_CONNECTED = "is_connected";
    public static final String FILTER_BKASH = "filter_bkash";
    public static final String FILTER_NAGAD = "filter_nagad";
    public static final String FILTER_ROCKET = "filter_rocket";
    public static final String FILTER_UPAY = "filter_upay";
    public static final String FILTER_RECEIVED_ONLY = "filter_received_only";
    public static final String FORWARD_COUNT = "forward_count";

    public static int getForwardCount(Context ctx) {
        return get(ctx).getInt(FORWARD_COUNT, 0);
    }

    public static void incrementForwardCount(Context ctx) {
        int count = getForwardCount(ctx);
        get(ctx).edit().putInt(FORWARD_COUNT, count + 1).apply();
    }
}
