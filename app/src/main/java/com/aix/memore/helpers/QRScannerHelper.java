package com.aix.memore.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.aix.memore.databinding.FragmentQrScannerBinding;
import com.aix.memore.interfaces.OnQrCodeScanned;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.NetworkUtil;
import com.aix.memore.view_models.HighlightViewModel;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

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
    private JSONObject scannedObject;
    private Toast toastNoInternet;
    private Context context;


    public QRScannerHelper(Context base) {
        super(base);

    }

    public void initQRScannerPreview(Context context, SurfaceView surfaceView, Activity activity, OnQrCodeScanned onQrCodeScanned, boolean permissionGranted, HighlightViewModel highlightViewModel) {
        try {
            this.activity = activity;
            this.context = context;
            this.onQrCodeScanned = onQrCodeScanned;
            toastNoInternet = Toast.makeText(activity, "No internet connection.", Toast.LENGTH_SHORT);

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
                    ErrorLog.WriteDebugLog("Surface View Changed "+ AppPermissionHelper.cameraPermissionGranted(context));

                    if (permissionGranted) {
                        try {
                            cameraSource.start(surfaceHolder);
                            ErrorLog.WriteDebugLog("Camera Start");


                        } catch (IOException e) {
                            ErrorLog.WriteErrorLog(e);
                        }
                    } else {
                        AppPermissionHelper.requestPermission(context);
                        ErrorLog.WriteDebugLog("Camera permission not granted cannot start");

                    }
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    if (permissionGranted) {
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

        setUpBarCodeScanner(highlightViewModel);
        } catch (Exception e) {
            ErrorLog.WriteErrorLog(e);
        }

    }




    public void enableBarcodeScanning(boolean value) {
        enableBarCodeDetection = value;
        if (value) {
            try {
                Log.d(TAG, "enableBarcodeScanning: ");
                createBarCodeScanner();

                cameraSource = new CameraSource.Builder(context, barcodeDetector)
                        .setRequestedPreviewSize(1920, 1080)
                        .setRequestedFps(60.0F)
                        .setAutoFocusEnabled(true).build();

                cameraSource.start(holder);
                hasScannedACode = false;
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
    public void setUpBarCodeScanner(HighlightViewModel highlightViewModel){
        try {
            ErrorLog.WriteDebugLog("setUpBarCodeScanner: called");
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                    hasScannedACode = false;
                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
//                    Log.d(TAG, "receiveDetections: Scanning: " + enableBarCodeDetection);
//                    ErrorLog.WriteDebugLog("receiveDetections: Scanning: " + enableBarCodeDetection);

//                if (!enableBarCodeDetection) return;

                    final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                    if (barcodeSparseArray.size() != 0) {

                        String scannedValue = barcodeSparseArray.valueAt(0).displayValue;
                        ErrorLog.WriteDebugLog("receiveDetections: " + scannedValue);

                        barcodeDetector.release();

                        //scannedObject = new JSONObject(scannedValue);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (onQrCodeScanned != null) {

                                    if (NetworkUtil.isNetworkAvailable(activity)) {
                                        ErrorLog.WriteDebugLog("Barcode Scanned, stopping camera");
//                                        cameraSource.release();
                                        cameraSource.stop();
//                                        highlightViewModel.getHighlightFromJSON(scannedObject);
                                        highlightViewModel.getHighlightFromQR(scannedValue);
                                    } else {
                                        setUpBarCodeScanner(highlightViewModel);
                                        toastNoInternet.cancel();
                                        toastNoInternet.show();
                                    }

//                                if (!hasScannedACode) {
//                                    onQrCodeScanned.barcodeScanned(scannedValue);
//                                    hasScannedACode = true;
//                                    enableBarCodeDetection = true;
//                                }

                                } else {
                                    ErrorLog.WriteDebugLog("Error sending scanned value; 'OnQrCodeScanned' is null");
                                }


                            }
                        });

                    }

                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }


    }
    public void enableQrScanner() {
        try {
            if (AppPermissionHelper.cameraPermissionGranted(getApplicationContext())) {
                if(cameraSource != null) {
                    cameraSource.start(holder);
                    hasScannedACode = false;
                    ErrorLog.WriteDebugLog("CAMERA ON RESUME START");
                }
                ErrorLog.WriteDebugLog("CAMERA PERMISSION NOT ENABLED");

            }else{
                if(cameraSource == null) return;
                cameraSource.stop();
                ErrorLog.WriteDebugLog("CAMERA ON RESUME STOP");

            }
        } catch (IOException e) {
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void setCallback(OnQrCodeScanned callback) {
        this.onQrCodeScanned = callback;
    }


    public void initialiseDetectorsAndSources(FragmentQrScannerBinding binding) {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        binding.surfaceViewQRscanner.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(binding.surfaceViewQRscanner.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    ErrorLog.WriteDebugLog("BARCODE "+barcodes.toString());
                }
            }
        });
    }

    public void resumeScanning(SurfaceView surfaceView, Context context,boolean permissionGranted,HighlightViewModel highlightViewModel) {
        enableBarcodeScanning(true);
        setUpBarCodeScanner(highlightViewModel);
    }
}
