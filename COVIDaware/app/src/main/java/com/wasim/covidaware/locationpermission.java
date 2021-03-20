package com.wasim.covidaware;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class locationpermission extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    static Context c;
    static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    public static boolean checkLocationPermission(Context con) {
        c=con;
        if (ContextCompat.checkSelfPermission((Activity)c,
                PERMISSIONS[0])
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            ActivityCompat.requestPermissions((Activity)c,
                    PERMISSIONS,
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.e("", "onRequestPermissionsResult: "+requestCode+"\n"+permissions+"\n"+grantResults );
    }
    
}
