package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<MyItem> mi = new ArrayList<MyItem>();
    ProgressDialog pd;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bottomNavigation.setSelectedItemId(R.id.maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);

    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Toast.makeText(MapsActivity.this, ""+item.getTitle(), Toast.LENGTH_SHORT).show();
                    switch (item.getItemId()) {
                        case R.id.predictions:
                            startActivity(new Intent(MapsActivity.this,MainActivity.class));
                            finish();
                            return true;
                        case R.id.maps:

                            return true;

                    }
                    return false;
                }
            };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public ClusterManager<MyItem> clusterManager;
    Algorithm<MyItem> clusterManagerAlgorithm;


    @Override
    public void onMapReady(GoogleMap googleMap) {

        pd = new ProgressDialog(this);
        pd.setTitle("Loading Maps...");
        pd.setCancelable(false);
        pd.show();

        mMap = googleMap;

        mMap.clear();
        if (clusterManager != null)
            clusterManager.clearItems();
        mMap.setTrafficEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { }
        mMap.setMyLocationEnabled(true);

        clusterManager = new ClusterManager<MyItem>(this, mMap);
        cluster();
        /*for(int i=0;i<mi.size();i++){
            if((mi.size()-i)>=100)
                clusterManager.addItems(mi.subList(i,i+100));
            else
                clusterManager.addItems(mi.subList(i,mi.size()));
            i=i+100;
        }*/
        //clusterManager.setRenderer(new MyClusterRenderer(this, mMap, clusterManager));
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 15));

                return true;
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);

        pd.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (!isTaskRoot()) {
            super.onBackPressed();//or finish()
        }
    }

    private void cluster() {
        clusterManager.clearItems();
        String d = "2021-06-04";
        //String d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseDatabase.getInstance().getReference(d+"/Districts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (Iterator<DataSnapshot> it = snapshot.getChildren().iterator(); it.hasNext(); ) {
                                DataSnapshot sn = it.next();
                                Geocoder coder = new Geocoder(MapsActivity.this);
                                List<Address> address;

                                try {
                                    address = coder.getFromLocationName(sn.child("name").getValue(String.class),1);
                                    Log.e("map", ""+ new LatLng(address.get(0).getLatitude(),address.get(0).getLongitude()));
                                    MyItem myItem = new MyItem(new LatLng(address.get(0).getLatitude(),address.get(0).getLongitude()),sn.child("name").getValue(String.class),"Positive cases: "+ sn.child("Positive_cases").getValue(String.class));
                                    clusterManager.addItem(myItem);
                                    clusterManager.cluster();
                                    

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}