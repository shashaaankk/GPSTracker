package com.example.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView Display;
    private Intent serviceIntent;
    private LocationManager locationManager;
    private Button btn,updt;
    private boolean isACCESS_COARSE_LOCATION = false;
    private boolean isACCESS_FINE_LOCATION = false;
    private boolean isWRITE_EXTERNAL_STORAGE = false;
    private fileWriter writer = new fileWriter();
    private float distanceTravelled = 0;
    private float latitude = 0;
    private float longitude = 0;
    private float averageSpeed = 0;
    Map<String, Object> results = new HashMap<>();
    ActivityResultLauncher<String[]> mPermissionLauncher;
    private final BroadcastReceiver gpsUpdates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("reciever","working");
            if(intent.getAction().equals("com.example.broadcast.GPS")){
                float lat = intent.getFloatExtra("lat",0);
                float lon = intent.getFloatExtra("lon",0);
                float speed = intent.getFloatExtra("speed",0);
                float distance = intent.getFloatExtra("distance",0);
                results.put("lat", lat);
                results.put("lon", lon);
                results.put("speed", speed);
                results.put("distance", distance);
                updateDisplay();
                //Display.setText("Broadcast Received: "+lat+" "+lon+ " "+ speed);
                writer.append(lat,lon);
            }
        }
    };
    private void updateDisplay() {
        StringBuilder displayText = new StringBuilder();
        displayText.append("Latitude: ").append(results.get("lat")).append("\n");
        displayText.append("Longitude: ").append(results.get("lon")).append("\n");
        displayText.append("Distance Travelled: ").append(results.get("distance")).append("m\n");
        displayText.append("Average Speed: ").append(results.get("speed")).append("m/s\n");

        Display.setText(displayText.toString());
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        writer.header();
        Display = findViewById(R.id.display);
        btn = findViewById(R.id.startbtn);
        //updt = findViewById(R.id.updatebtn);
        mPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            //Update Permissions
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) != null) {
                    isACCESS_COARSE_LOCATION = result.get(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                if (result.get(android.Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                    isACCESS_FINE_LOCATION = result.get(android.Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null) {
                    isACCESS_FINE_LOCATION = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        requestPermission();
        IntentFilter filter = new IntentFilter("com.example.broadcast.GPS");
        registerReceiver(gpsUpdates, filter,RECEIVER_EXPORTED);
        btn.setOnClickListener(v -> {
            if(serviceIntent!=null) {                //When Called, stop current service and restart another service
                this.stopService(serviceIntent);
                Display.setText("Press start to start GPS Service, stop to stop.");
                try {
                    writer.footer();
                    writer.write(this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                serviceIntent = null;
            }
            else {
                serviceIntent = new Intent(this,gpsService.class);
                this.startService(serviceIntent);
                Display.setText("No signal recived yet");
            }
        });
    }
    private void requestPermission() {
        isACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        isACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        isWRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        List<String> permissions = new ArrayList<String>();

        if (!isACCESS_COARSE_LOCATION) {
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!isACCESS_FINE_LOCATION) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!isWRITE_EXTERNAL_STORAGE) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // Request any permissions that have not been granted
        if (!permissions.isEmpty()) {
            mPermissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }
}