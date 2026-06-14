package com.takapay.smsforwarder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.takapay.smsforwarder.service.SmsForwarderService;
import com.takapay.smsforwarder.utils.Prefs;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            String apiKey = Prefs.getString(context, Prefs.API_KEY, "");
            String serverUrl = Prefs.getString(context, Prefs.SERVER_URL, "");

            if (!apiKey.isEmpty() && !serverUrl.isEmpty()) {
                Intent serviceIntent = new Intent(context, SmsForwarderService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
