package com.takapay.smsforwarder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.takapay.smsforwarder.network.ApiClient;
import com.takapay.smsforwarder.utils.Prefs;
import com.takapay.smsforwarder.utils.SmsParser;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "TakaPaySmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;

        StringBuilder bodyBuilder = new StringBuilder();
        String sender = messages[0].getDisplayOriginatingAddress();
        for (SmsMessage msg : messages) {
            bodyBuilder.append(msg.getMessageBody());
        }
        String body = bodyBuilder.toString();

        Log.d(TAG, "SMS from: " + sender + " | Body: " + body);

        SmsParser.ParsedSms parsed = SmsParser.parse(sender, body);

        if (!parsed.isValid) {
            Log.d(TAG, "SMS not a payment SMS, ignoring");
            return;
        }

        if (!shouldForward(context, parsed)) {
            Log.d(TAG, "SMS filtered out: " + parsed.provider);
            return;
        }

        Log.d(TAG, "Forwarding: " + parsed.provider + " | " + parsed.amount + " | " + parsed.trxId);

        ApiClient.forwardSms(context, sender, body, parsed, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "SMS forwarded successfully: " + response);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to forward SMS: " + error);
            }
        });
    }

    private boolean shouldForward(Context ctx, SmsParser.ParsedSms parsed) {
        switch (parsed.provider) {
            case "bkash":
                if (!Prefs.getBool(ctx, Prefs.FILTER_BKASH, true)) return false;
                break;
            case "nagad":
                if (!Prefs.getBool(ctx, Prefs.FILTER_NAGAD, true)) return false;
                break;
            case "rocket":
                if (!Prefs.getBool(ctx, Prefs.FILTER_ROCKET, true)) return false;
                break;
            case "upay":
                if (!Prefs.getBool(ctx, Prefs.FILTER_UPAY, true)) return false;
                break;
        }

        if (Prefs.getBool(ctx, Prefs.FILTER_RECEIVED_ONLY, true)) {
            return "received".equals(parsed.type) || "topup".equals(parsed.type);
        }

        return true;
    }
}
