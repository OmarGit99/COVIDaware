package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wasim.covidaware.services.LocationService;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView lv;
    private BottomNavigationView bottomNavigation;
    Switch proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);

        proxy = hView.findViewById(R.id.proximity);
        isLocationServiceRunning();
        proxy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startLocationService();
                if(isChecked){
                    proxy.setChecked(true);
                }
                else
                    proxy.setChecked(false);
            }
        });


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
                if(!isLocationServiceRunning()){
                    proxy.setText("SOS LOCATION PROMITY - RUNNING");
                }
                else{
                    proxy.setText("SOS LOCATION PROMITY - STOPPED");
                    LocationService ls = new LocationService();
                    ls.stop();
                }
            }
        });

        lv = findViewById(R.id.listView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
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
    ArrayList<String> ac  = new ArrayList<>();
    ArrayList<String> dc  = new ArrayList<>();

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
                        List<Double> l = new ArrayList<>();DataSnapshot r = null;
                    for (Iterator<DataSnapshot> iter = sn.getChildren().iterator(); iter.hasNext(); ) {
                        DataSnapshot s = iter.next();
                        r=s;
                        l.add(Double.parseDouble(s.child("AC").getValue(String.class)));
                    }
                    predictD.add(r.child("Name").getValue(String.class)+"");
                    ac.add(r.child("AC").getValue(String.class));
                    dc.add(r.child("Death cases").getValue(String.class));
                        Double avg= calculateAverage(l);
                        List<Double> d = new ArrayList<>();
                        for(Double i : l){
                            d.add((i-avg)*(i-avg));
                        }
                        Double realavg = calculateAverage(d);
                        DecimalFormat numberFormat = new DecimalFormat("00.0000000");
                        predictC.add((numberFormat.format((Math.sqrt(realavg)*1000/avg)))+" % incr/decr");
                }
                pd.dismiss();
                ArrayAdapter a = new ArrayAdapter(MainActivity.this,predictD,ac,dc,predictC);
                lv.setAdapter(a);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //pd.dismiss();
        Log.e("TAG", "predict: "+predictD+"\n"+predictC );
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Toast.makeText(MainActivity.this, ""+item.getTitle(), Toast.LENGTH_SHORT).show();
                    switch (item.getItemId()) {
                        case R.id.predictions:
                            //openFragment(HomeFragment.newInstance("", ""));
                            return true;
                        case R.id.maps:
                            startActivity(new Intent(MainActivity.this,MapsActivity.class));
                            finish();
                            return true;

                    }
                    return false;
                }
            };

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
                proxy.setChecked(true);
                return true;
            }
            else
                proxy.setChecked(false);
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
        predict();
        locationpermission.checkLocationPermission(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pd.dismiss();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.lo:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                break;
                //do someting silly
            default:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}