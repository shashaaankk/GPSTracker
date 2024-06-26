package com.example.gpstracker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.Provider;
import java.util.Timer;
import java.util.concurrent.Executor;

public class gpsService extends Service implements LocationListener {
    private final Intent broadcastIntent = new Intent();
    private float prevTime =0;
    private LocationManager locationManager ;
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("service","reached");
        prevTime = System.currentTimeMillis();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,this);
        broadcastIntent.setAction("com.example.broadcast.GPS");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        Log.d("sevice","stopped");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("location",location.toString());
        broadcastIntent.putExtra("lat",(float) location.getLatitude());
        broadcastIntent.putExtra("lon",(float) location.getLongitude());
        broadcastIntent.putExtra("speed",location.getSpeed());
        float deltatime = (System.currentTimeMillis()-prevTime);
        deltatime =  deltatime<2000? deltatime/1000 : 2;
        broadcastIntent.putExtra("distance",deltatime*location.getSpeed());
        prevTime = System.currentTimeMillis();
        sendBroadcast(broadcastIntent);
    }

}
