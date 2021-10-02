package com.aix.memore.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class AppPermissionHelper {
    public static final int REQUEST_ALL_PERMISSION = 201;
    public static final int REQUEST_LOCATION_PERMISSION = 202;

    public static boolean cameraPermissionGranted(Context context){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean locationPermissionGranted(Context context){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestPermission(Context context){
        ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ALL_PERMISSION);
    }
    public static void requestLocationPermission(Context context){
        ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

}
