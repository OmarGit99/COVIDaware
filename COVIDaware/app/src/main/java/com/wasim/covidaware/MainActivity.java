package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wasim.covidaware.services.LocationService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Button proxy, maps;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proxy=findViewById(R.id.proximity);
        if(isLocationServiceRunning()){
            proxy.setText("SOS LOCATION PROMITY - RUNNING");
        }
        else{
            proxy.setText("SOS LOCATION PROMITY - STOPPED");
        }
        proxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
            }
        });

        lv = findViewById(R.id.listView);


        maps=findViewById(R.id.maps);
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }
        });

    }

    private double calculateAverage(List <Double> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Double mark : marks) {
            sum += mark;
        }

        return (Double)(sum / marks.size());
    }

    ArrayList<String> predictD  = new ArrayList<>();
    ArrayList<String> predictC  = new ArrayList<>();

    ProgressDialog pd;

    private void predict() {
        pd = new ProgressDialog(this);
        pd.setTitle("Loading Predictions");
        pd.setCancelable(false);
        pd.show();

        FirebaseDatabase.getInstance().getReference("Gdata")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Iterator<DataSnapshot> it = snapshot.getChildren().iterator(); it.hasNext(); ) {
                    DataSnapshot sn = it.next();
                        List<Double> l = new ArrayList<>();
                    for (Iterator<DataSnapshot> iter = sn.getChildren().iterator(); iter.hasNext(); ) {
                        DataSnapshot s = iter.next();
                        l.add(Double.parseDouble(s.child("AC").getValue(String.class)));
                    }
                        Double avg= calculateAverage(l);
                        List<Double> d = new ArrayList<>();
                        for(Double i : l){
                            d.add((i-avg)*(i-avg));
                        }
                        Double realavg = calculateAverage(d);
                        predictD.add(sn.child(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())).child("Name").getValue(String.class)+"");
                        DecimalFormat numberFormat = new DecimalFormat("00.0000000");
                        predictC.add((numberFormat.format((Math.sqrt(realavg)*1000/avg)))+" % incr/decr");
                }
                pd.dismiss();
                ArrayAdapter a = new ArrayAdapter(MainActivity.this,predictD,predictC);
                lv.setAdapter(a);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //pd.dismiss();
        Log.e("TAG", "predict: "+predictD+"\n"+predictC );
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
        else{
            LocationService ls = new LocationService();
            ls.stop();
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d("Location log", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("Location log", "isLocationServiceRunning: location service is not running.");
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        locationpermission.checkLocationPermission(this);
        predict();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pd.dismiss();
    }
}