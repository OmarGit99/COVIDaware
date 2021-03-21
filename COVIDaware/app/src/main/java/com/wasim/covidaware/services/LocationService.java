package com.wasim.covidaware.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wasim.covidaware.MyItem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 10000;  /* 1 secs */
    private final static long FASTEST_INTERVAL = 10000; /* 1/2 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        extra = intent.getStringExtra("action");
        this.intent = intent;
        return null;
    }

    Notification notification;
    String extra = "";

    @Override
    public void onCreate() {
        super.onCreate();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


    }

    private void check(String extra) {
        if (extra.equalsIgnoreCase("start")) {

            if (Build.VERSION.SDK_INT >= 26) {
                String CHANNEL_ID = "CHANNEL_01";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "My Channel",
                        NotificationManager.IMPORTANCE_MIN);

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);


                notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("COVID-aware Location Guard Saving You From The Wrath Of COVID-19").build();

                startForeground(1, notification);
                setIsRunning(true);
                getLocation();
            }
        } else if (extra.equalsIgnoreCase("stop")) {
            Toast.makeText(this, "trying", Toast.LENGTH_SHORT).show();
            setIsRunning(false);
            stopForeground(true);
            stopSelf();

            stop();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: called." + intent.getStringExtra("action"));
        extra = intent.getStringExtra("action");
        if(extra.equalsIgnoreCase("stop"))
            stopForeground(true);

        else
            check(extra);

        return START_NOT_STICKY;

    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        FirebaseDatabase.getInstance().getReference("LocData").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    List<LatLng> l = new ArrayList<>();
                                    List<Double> d = new ArrayList<>();
                                    d.add(1000.0);
                                    l.add(new LatLng(0, 0));
                                    for (Iterator<DataSnapshot> it = snapshot.getChildren().iterator(); it.hasNext(); ) {
                                        DataSnapshot sn = it.next();
                                        String[] sll = sn.getValue(String.class).split(",");
                                        LatLng ll = new LatLng(Double.parseDouble(sll[0]), Double.parseDouble(sll[1]));
                                        Location A = locationResult.getLastLocation();
                                        Location locationB = new Location("point B");
                                        locationB.setLatitude(ll.latitude);
                                        locationB.setLongitude(ll.longitude);

                                        double dist = A.distanceTo(locationB);

                                        if (dist <= 1000) {
                                            double min = Math.min(d.get(0), dist);

                                            d.add(0, Double.valueOf(new DecimalFormat("#.000").format(min)));
                                            if (min == dist) {
                                                l.add(0, ll);
                                            }
                                        }

                                    }

                                    try {
                                        List<Address> a = new Geocoder(LocationService.this).getFromLocation(l.get(0).latitude, l.get(0).longitude, 1);
                                        Toast.makeText(LocationService.this, "You are " + d.get(0) + "m away from a containment zone\narea :" + a.get(0).getFeatureName(), Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "The Closest Containment zone is " + d.get(0) + "m away from your current location\narea :" + a.get(0).getFeatureName());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //Toast.makeText(LocationService.this, "distance"+dist, Toast.LENGTH_SHORT).show();


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    static boolean isRunning = false;

    public static boolean isIsRunning() {
        return isRunning;
    }

    private static void setIsRunning(boolean isRunning) {
        LocationService.isRunning = isRunning;
    }

    Intent intent;

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " + extra);
        super.onDestroy();
        stopSelf();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
    }

    public void stop() {
        stopForeground(true);
        stopSelf();

    }

}
