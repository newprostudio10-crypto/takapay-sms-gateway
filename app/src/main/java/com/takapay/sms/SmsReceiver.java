package com.takapay.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("takapay", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("active", false)) return;

        String apiKey = prefs.getString("api_key", "");
        String webhookUrl = prefs.getString("webhook_url", "");
        if (apiKey.isEmpty() || webhookUrl.isEmpty()) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
            String from = msg.getOriginatingAddress();
            String text = msg.getMessageBody();
            if (text == null) continue;

            String low = text.toLowerCase();
            if (!low.contains("bkash") && !low.contains("nagad") &&
                !low.contains("rocket") && !low.contains("trxid")) continue;

            final String f = from, t = text, k = apiKey, u = webhookUrl;
            new Thread(() -> {
                try {
                    String clean = t.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
                    String json = "{\"from\":\"" + f + "\",\"text\":\"" + clean + "\",\"api_key\":\"" + k + "\"}";
                    URL url = new URL(u);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-Type", "application/json");
                    c.setRequestProperty("X-API-Key", k);
                    c.setDoOutput(true);
                    c.setConnectTimeout(10000);
                    OutputStream os = c.getOutputStream();
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    c.getResponseCode();
                    c.disconnect();
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }
}
