package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wasim.covidaware.services.LocationService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.wasim.covidaware.services.LocationService.isIsRunning;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView lv;
    private BottomNavigationView bottomNavigation;
    DrawerLayout dl;
    Switch proxy;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        navView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navView.getHeaderView(0);
        proxy = hView.findViewById(R.id.proximity);
        isLocationServiceRunning();
        setNavigationDrawer();

        lv = findViewById(R.id.listView);

    }

    private double calculateAverage(List<Double> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Double mark : marks) {
            sum += mark;
        }

        return (Double) (sum / marks.size());
    }

    NavigationView navView;

    private void setNavigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(0xffd1b000, PorterDuff.Mode.MULTIPLY);
        mToggle = new ActionBarDrawerToggle(this, dl, toolbar, (R.string.open), (R.string.close));
        dl.addDrawerListener(mToggle);
        mToggle.syncState();// initiate a DrawerLayout
        // initiate a Navigation View
        // implement setNavigationItemSelectedListener event on NavigationView
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getTitle().toString().equalsIgnoreCase("Logout")) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                return false;
            }
        });

        isLocationServiceRunning();
        proxy.setChecked(isIsRunning());
        proxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
            }
        });

    }


    ArrayList<String> predictD = new ArrayList<>();
    ArrayList<String> predictC = new ArrayList<>();
    ArrayList<String> ac = new ArrayList<>();
    ArrayList<String> dc = new ArrayList<>();

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
                            DataSnapshot r = null;
                            for (Iterator<DataSnapshot> iter = sn.getChildren().iterator(); iter.hasNext(); ) {
                                DataSnapshot s = iter.next();
                                r = s;
                                l.add(Double.parseDouble(s.child("AC").getValue(String.class)));
                            }
                            predictD.add(r.child("Name").getValue(String.class) + "");
                            ac.add(r.child("AC").getValue(String.class));
                            dc.add(r.child("Death cases").getValue(String.class));
                            Double avg = calculateAverage(l);
                            List<Double> d = new ArrayList<>();
                            for (Double i : l) {
                                d.add((i - avg) * (i - avg));
                            }
                            Double realavg = calculateAverage(d);
                            DecimalFormat numberFormat = new DecimalFormat("00.0000000");
                            predictC.add((numberFormat.format((Math.sqrt(realavg) * 1000 / avg))) + " % incr/decr");
                        }
                        pd.dismiss();
                        ArrayAdapter a = new ArrayAdapter(MainActivity.this, predictD, ac, dc, predictC);
                        lv.setAdapter(a);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //pd.dismiss();
        Log.e("TAG", "predict: " + predictD + "\n" + predictC);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Toast.makeText(MainActivity.this, "" + item.getTitle(), Toast.LENGTH_SHORT).show();
                    switch (item.getItemId()) {
                        case R.id.predictions:
                            //openFragment(HomeFragment.newInstance("", ""));
                            return true;
                        case R.id.maps:
                            startActivity(new Intent(MainActivity.this, MapsActivity.class));
                            finish();
                            return true;

                    }
                    return false;
                }
            };

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        Log.e("TAG", "startLocationService: " + isLocationServiceRunning());
        if (isLocationServiceRunning()) {
            /*serviceIntent.putExtra("action", "stop");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(serviceIntent);
                Log.e("", "startLocationService: stopped" );
            } else {
                stopService(serviceIntent);
            }*/

//        this.startService(serviceIntent);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serviceIntent.putExtra("action", "start");
                this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        return isIsRunning();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        predict();
        locationpermission.checkLocationPermission(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pd.dismiss();
    }


}