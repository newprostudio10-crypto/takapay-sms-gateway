package com.takapay.smsforwarder.network;

import android.content.Context;
import android.util.Log;

import com.takapay.smsforwarder.utils.Prefs;
import com.takapay.smsforwarder.utils.SmsParser;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String TAG = "TakaPayAPI";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public interface Callback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void forwardSms(Context ctx, String sender, String body,
                                   SmsParser.ParsedSms parsed, Callback callback) {
        String serverUrl = Prefs.getString(ctx, Prefs.SERVER_URL, "");
        String apiKey = Prefs.getString(ctx, Prefs.API_KEY, "");

        if (serverUrl.isEmpty() || apiKey.isEmpty()) {
            if (callback != null) callback.onFailure("Server URL or API Key not set");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("api_key", apiKey);
                json.put("sender", sender);
                json.put("sms_body", body);
                json.put("provider", parsed.provider);
                json.put("trx_id", parsed.trxId != null ? parsed.trxId : "");
                json.put("amount", parsed.amount != null ? parsed.amount : "");
                json.put("type", parsed.type != null ? parsed.type : "unknown");
                json.put("timestamp", System.currentTimeMillis() / 1000);

                String url = serverUrl.replaceAll("/$", "") + "/api/sms-receive.php";

                RequestBody body2 = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body2)
                        .addHeader("X-TakaPay-Key", apiKey)
                        .build();

                OkHttpClient client = getClient();
                try (Response response = client.newCall(request).execute()) {
                    String respBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response: " + respBody);
                    if (response.isSuccessful()) {
                        Prefs.incrementForwardCount(ctx);
                        if (callback != null) callback.onSuccess(respBody);
                    } else {
                        if (callback != null) callback.onFailure("HTTP " + response.code());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error forwarding SMS", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public static void testConnection(Context ctx, String url, String apiKey, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("api_key", apiKey);
                json.put("action", "ping");

                String endpoint = url.replaceAll("/$", "") + "/api/sms-receive.php";

                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(endpoint)
                        .post(body)
                        .addHeader("X-TakaPay-Key", apiKey)
                        .build();

                OkHttpClient client = getClient();
                try (Response response = client.newCall(request).execute()) {
                    String respBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        if (callback != null) callback.onSuccess(respBody);
                    } else {
                        if (callback != null) callback.onFailure("HTTP " + response.code());
                    }
                }
            } catch (Exception e) {
                if (callback != null) callback.onFailure(e.getMessage());
            }
        }).start();
    }
}
