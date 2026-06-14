package com.takapay.sms;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    EditText etApiKey, etWebhookUrl;
    Button btnSave;
    TextView tvStatus;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(0xFFFFFFFF);

        TextView title = new TextView(this);
        title.setText("TakaPay SMS Gateway");
        title.setTextSize(22);
        title.setTextColor(0xFF1a73e8);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        TextView label1 = new TextView(this);
        label1.setText("Webhook URL:");
        label1.setTextSize(14);
        layout.addView(label1);

        etWebhookUrl = new EditText(this);
        etWebhookUrl.setHint("https://yourdomain.com/api/sms-webhook.php");
        layout.addView(etWebhookUrl);

        TextView label2 = new TextView(this);
        label2.setText("API Key:");
        label2.setTextSize(14);
        label2.setPadding(0, 20, 0, 0);
        layout.addView(label2);

        etApiKey = new EditText(this);
        etApiKey.setHint("API Key দিন");
        layout.addView(etApiKey);

        btnSave = new Button(this);
        btnSave.setText("Save & চালু করো");
        btnSave.setBackgroundColor(0xFF1a73e8);
        btnSave.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 30, 0, 0);
        btnSave.setLayoutParams(p);
        layout.addView(btnSave);

        tvStatus = new TextView(this);
        tvStatus.setTextSize(14);
        tvStatus.setPadding(0, 20, 0, 0);
        layout.addView(tvStatus);

        setContentView(layout);

        prefs = getSharedPreferences("takapay", MODE_PRIVATE);
        etApiKey.setText(prefs.getString("api_key", ""));
        etWebhookUrl.setText(prefs.getString("webhook_url", ""));
        updateStatus();

        btnSave.setOnClickListener(v -> {
            String apiKey = etApiKey.getText().toString().trim();
            String webhookUrl = etWebhookUrl.getText().toString().trim();
            if (apiKey.isEmpty() || webhookUrl.isEmpty()) {
                Toast.makeText(this, "সব তথ্য দিন!", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit()
                .putString("api_key", apiKey)
                .putString("webhook_url", webhookUrl)
                .putBoolean("active", true)
                .apply();
            updateStatus();
            Toast.makeText(this, "চালু হয়েছে!", Toast.LENGTH_LONG).show();
        });

        String[] perms = {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};
        boolean need = false;
        for (String perm : perms)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                need = true;
        if (need) ActivityCompat.requestPermissions(this, perms, 1);
    }

    void updateStatus() {
        boolean active = prefs.getBoolean("active", false);
        if (active) {
            tvStatus.setText("চালু আছে - SMS Forward হচ্ছে");
            tvStatus.setTextColor(0xFF2e7d32);
        } else {
            tvStatus.setText("চালু নেই - তথ্য দিন");
            tvStatus.setTextColor(0xFFc62828);
        }
    }
}
