package com.example.remotecontrolcart;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.4.1/";
    private boolean isConnected = false;

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

        // UI Elements
        ImageView arrowUp = findViewById(R.id.arrowUp);
        ImageView arrowDown = findViewById(R.id.arrowDown);
        ImageView arrowLeft = findViewById(R.id.arrowLeft);
        ImageView arrowRight = findViewById(R.id.arrowRight);
        Button stopButton = findViewById(R.id.stopButton);
        Button checkConnectionBtn = findViewById(R.id.checkConnectionBtn); // Optional
        TextView statusText = findViewById(R.id.statusText);

        // Movement commands
        arrowUp.setOnClickListener(v -> trySendCommand("forward", statusText));
        arrowDown.setOnClickListener(v -> trySendCommand("backward", statusText));
        arrowLeft.setOnClickListener(v -> trySendCommand("left", statusText));
        arrowRight.setOnClickListener(v -> trySendCommand("right", statusText));
        stopButton.setOnClickListener(v -> trySendCommand("stop", statusText));

        // Optional: test connection
        if (checkConnectionBtn != null) {
            checkConnectionBtn.setOnClickListener(v -> testConnection(statusText));
        }
    }

    private void trySendCommand(String command, TextView statusText) {
        if (isConnected) {
            sendCommand(command, statusText);
        } else {
            statusText.setText("Status: Not connected");
            statusText.setTextColor(Color.RED);
        }
    }

    private void sendCommand(String command, TextView statusText) {
        String fullUrl = BASE_URL + command;

        new Thread(() -> {
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1500);
                conn.setReadTimeout(1500);

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        statusText.setText("Status: Connected");
                        statusText.setTextColor(Color.GREEN);
                    } else {
                        statusText.setText("Status: Command failed");
                        statusText.setTextColor(Color.YELLOW);
                    }
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

    private void testConnection(TextView statusText) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "status");
                Log.d("ConnectionTest", "Testing connection to: " + url); // Log the URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1000); // Connection timeout
                conn.setReadTimeout(1000); // Read timeout

                int responseCode = conn.getResponseCode();
                Log.d("ConnectionTest", "Response Code: " + responseCode); // Log the response code

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                conn.disconnect();

                runOnUiThread(() -> {
                    Log.d("ConnectionTest", "Response: " + response.toString()); // Log the response body
                    if (responseCode == 200 && response.toString().contains("Pico")) {
                        statusText.setText("Status: Pico Connected");
                        statusText.setTextColor(Color.GREEN);
                        isConnected = true;
                    } else {
                        statusText.setText("Status: Pico Not Responding");
                        statusText.setTextColor(Color.YELLOW);
                        isConnected = false;
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    statusText.setText("Status: Connection Failed");
                    statusText.setTextColor(Color.RED);
                    isConnected = false;
                });
                e.printStackTrace();
            }
        }).start();
    }

}
