package com.example.remotecontrolcart;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.4.1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get button views
        ImageView arrowUp = findViewById(R.id.arrowUp);
        ImageView arrowDown = findViewById(R.id.arrowDown);
        ImageView arrowLeft = findViewById(R.id.arrowLeft);
        ImageView arrowRight = findViewById(R.id.arrowRight);
        Button stopButton = findViewById(R.id.stopButton);
        TextView statusText = findViewById(R.id.statusText);

        // Set listeners
        arrowUp.setOnClickListener(v -> sendCommand("forward", statusText));
        arrowDown.setOnClickListener(v -> sendCommand("backward", statusText));
        arrowLeft.setOnClickListener(v -> sendCommand("left", statusText));
        arrowRight.setOnClickListener(v -> sendCommand("right", statusText));
        stopButton.setOnClickListener(v -> sendCommand("stop", statusText));
    }

    private void sendCommand(String command, TextView statusText) {
        String fullUrl = BASE_URL + command;

        runOnUiThread(() -> {
            statusText.setText("Status: Connecting...");
            statusText.setTextColor(Color.GRAY);
        });

        new Thread(() -> {
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1500);
                conn.setReadTimeout(1500);
                conn.connect();
                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    statusText.setText("Status: Connected");
                    statusText.setTextColor(Color.GREEN);
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    statusText.setText("Status: Disconnected");
                    statusText.setTextColor(Color.RED);
                });
                e.printStackTrace();
            }
        }).start();
    }
}
