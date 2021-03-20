package com.wasim.covidaware;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class locationpermission extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    static Context c;
    static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.FOREGROUND_SERVICE
    };

    public static boolean checkLocationPermission(Context con) {
        c=con;
        if (ContextCompat.checkSelfPermission((Activity)c,
                PERMISSIONS[0])
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission((Activity)c,
                PERMISSIONS[1])
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)c, PERMISSIONS[0]) && ActivityCompat.shouldShowRequestPermissionRationale((Activity)c, PERMISSIONS[1])) {

                // Show an explanation to the user *asynchronously* -- don't block
                // (Activity)c thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder((Activity)c)
                        .setTitle("Grant Location Details for Map")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity)c,
                                        PERMISSIONS,
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity)c,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission((Activity)c,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                    }

                } else {

                    if (ContextCompat.checkSelfPermission((Activity)c,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                    }

                }
                return;
            }

        }
    }
    
}
