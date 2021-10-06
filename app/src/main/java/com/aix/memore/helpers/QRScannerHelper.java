package com.aix.memore.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;


import com.aix.memore.interfaces.OnQrCodeScanned;
import com.aix.memore.utilities.ErrorLog;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

@SuppressLint("MissingPermission")
public class QRScannerHelper extends ContextWrapper {
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private OnQrCodeScanned onQrCodeScanned;
    private SurfaceHolder holder;
    private boolean hasScannedACode = false;
    private boolean enableBarCodeDetection = true;
    private String TAG = "QRScannerHelper";
    private Activity activity;

    public QRScannerHelper(Context base) {
        super(base);
    }

    public void initQRScannerPreview(Context context, SurfaceView surfaceView, Activity activity, OnQrCodeScanned onQrCodeScanned) {
        try {
            this.activity = activity;
            this.onQrCodeScanned = onQrCodeScanned;
            createBarCodeScanner();

            cameraSource = new CameraSource.Builder(context, barcodeDetector)
                    .setRequestedPreviewSize(1920, 1080)
                    .setRequestedFps(60.0F)
                    .setAutoFocusEnabled(true).build();


            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    ErrorLog.WriteDebugLog("Surfaceview Created");
                    holder = surfaceHolder;

                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    ErrorLog.WriteDebugLog("Surface View Changed");

                    if (AppPermissionHelper.cameraPermissionGranted(context)) {
                        try {
                            cameraSource.start(surfaceHolder);

                        } catch (IOException e) {
                            ErrorLog.WriteErrorLog(e);
                        }
                    } else {
                        AppPermissionHelper.requestPermission(context);
                    }
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    if (AppPermissionHelper.cameraPermissionGranted(context)) {
                        try {
                            ErrorLog.WriteDebugLog("Surface View Destroyed");
                            cameraSource.release();
                            cameraSource.stop();


                        } catch (Exception e) {
                            ErrorLog.WriteErrorLog(e);
                        }
                    } else {
                        AppPermissionHelper.requestPermission(context);
                    }
                }
            });

        setUpBarCodeScanner();
        } catch (Exception e) {
            ErrorLog.WriteErrorLog(e);
        }

    }




    public void enableBarcodeScanning(boolean value) {
        enableBarCodeDetection = value;
        if (value) {
            try {
                Log.d(TAG, "enableBarcodeScanning: ");
                cameraSource.start(holder);
                hasScannedACode = false;
                createBarCodeScanner();
            } catch (Exception e) {
                ErrorLog.WriteErrorLog(e);
            }

        } else {
            if(cameraSource == null) return;
            cameraSource.stop();
        }

    }
    private void createBarCodeScanner(){
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

    }
    private void setUpBarCodeScanner(){
        ErrorLog.WriteDebugLog("setUpBarCodeScanner: called");
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                hasScannedACode = false;
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                Log.d(TAG, "receiveDetections: Scanning: " + enableBarCodeDetection);
                ErrorLog.WriteDebugLog("receiveDetections: Scanning: " + enableBarCodeDetection);

                if (!enableBarCodeDetection) return;

                final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                if (barcodeSparseArray.size() != 0) {

                    String scannedValue = barcodeSparseArray.valueAt(0).displayValue;
                    ErrorLog.WriteDebugLog("receiveDetections: " + scannedValue);

                    barcodeDetector.release();

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (onQrCodeScanned != null) {
                                ErrorLog.WriteDebugLog("Barcode Scanned, stopping camera");
                                cameraSource.release();
                                cameraSource.stop();
                                if (!hasScannedACode) {
                                    onQrCodeScanned.barcodeScanned(scannedValue);
                                    hasScannedACode = true;
                                    enableBarCodeDetection = true;
                                }

                            } else {
                                ErrorLog.WriteDebugLog("Error sending scanned value; 'OnQrCodeScanned' is null");
                            }


                        }
                    });


                }

            }
        });


    }
    public void enableQrScanner() {
        try {
            if (AppPermissionHelper.cameraPermissionGranted(getApplicationContext())) {
                cameraSource.start(holder);
                hasScannedACode = false;
            }
        } catch (IOException e) {
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void setCallback(OnQrCodeScanned callback) {
        this.onQrCodeScanned = callback;
    }
}
