package com.aix.memore.helpers;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.aix.memore.interfaces.FragmentPermissionInterface;
import com.aix.memore.utilities.ErrorLog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public static void requestMultiplePermissions(FragmentActivity fragmentActivity, FragmentPermissionInterface fragmentPermissionInterface){

        ErrorLog.WriteDebugLog("Asking multiple permission");
        ActivityResultLauncher<String[]> requestPermissionLauncher =
                fragmentActivity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        fragmentPermissionInterface.onGranted(result);
                    }
                });

        requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION});
    }

}
