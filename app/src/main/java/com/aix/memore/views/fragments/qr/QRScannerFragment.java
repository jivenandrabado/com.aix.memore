package com.aix.memore.views.fragments.qr;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentQrScannerBinding;
import com.aix.memore.helpers.AppPermissionHelper;
import com.aix.memore.helpers.QRScannerHelper;
import com.aix.memore.interfaces.FragmentPermissionInterface;
import com.aix.memore.interfaces.OnQrCodeScanned;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.NetworkUtil;
import com.aix.memore.view_models.HighlightViewModel;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class QRScannerFragment extends Fragment {

    private FragmentQrScannerBinding binding;
    private NavController navController;
    private HighlightViewModel highlightViewModel;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private SurfaceHolder surfaceHolder;
    private Toast toastNoInternet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {

            navController = Navigation.findNavController(view);
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);

            binding.buttonLifecare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_QRScannerFragment_to_lifeCareFragment);
                }
            });

            binding.frameLayoutSquare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_QRScannerFragment_to_createMemoreFragment);
                }
            });

            binding.buttonExplore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    chooseImage();

                    navController.navigate(R.id.action_QRScannerFragment_to_QRScanHistoryFragment);
                }
            });

            initQRScanner();
            initHighlightObserver();

        } catch (Exception e) {
            ErrorLog.WriteErrorLog(e);
        }


    }

    private void initQRScanner() {
        initQRScannerPrev();
    }

    public void initQRScannerPrev() {
        try {
            toastNoInternet = Toast.makeText(requireContext(), "No internet connection.", Toast.LENGTH_SHORT);

            barcodeDetector = new BarcodeDetector.Builder(requireContext())
                    .setBarcodeFormats(Barcode.ALL_FORMATS)
                    .build();

            cameraSource = new CameraSource.Builder(requireContext(), barcodeDetector)
                    .setRequestedPreviewSize(1920, 1080)
                    .setRequestedFps(60.0F)
                    .setAutoFocusEnabled(true).build();


            binding.surfaceViewQRscanner.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder1) {
                    ErrorLog.WriteDebugLog("Surfaceview Created");
                    surfaceHolder = surfaceHolder1;
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    try {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraSource.start(surfaceHolder);
                            ErrorLog.WriteDebugLog("Camera Start");

                        } else {
                            ErrorLog.WriteDebugLog("Camera stop permission not granted surface change");
                            requestPermissionLauncher.launch(
                                    Manifest.permission.CAMERA);
                        }


                    } catch (IOException e) {
                        ErrorLog.WriteErrorLog(e);
                    }
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    try {
                        ErrorLog.WriteDebugLog("Surface View Destroyed");
                        cameraSource.release();
                        cameraSource.stop();


                    } catch (Exception e) {
                        ErrorLog.WriteErrorLog(e);
                    }
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {

                    final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                    if (barcodeSparseArray.size() != 0) {

                        String scannedValue = barcodeSparseArray.valueAt(0).displayValue;
                        ErrorLog.WriteDebugLog("receiveDetections: " + scannedValue);

                        barcodeDetector.release();
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (NetworkUtil.isNetworkAvailable(requireActivity())) {
                                    ErrorLog.WriteDebugLog("Barcode Scanned, stopping camera");
//                                        cameraSource.release();
                                    cameraSource.stop();
                                    highlightViewModel.getHighlightFromQR(scannedValue);
                                } else {
                                        toastNoInternet.cancel();
                                        toastNoInternet.show();
                                }
                            }
                        });

                    }

                }
            });

        } catch (Exception e) {
            ErrorLog.WriteErrorLog(e);
        }

    }

    private void initHighlightObserver() {
        try {
            highlightViewModel.memoreFound().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean != null) {
                        if (aBoolean) {
                            navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment);
                        } else {
                            Toast.makeText(requireContext(), "Invalid QR Code,", Toast.LENGTH_SHORT).show();
                            resumeCamera();
                        }
                        highlightViewModel.memoreFound().setValue(null);

                    }
                }
            });

        } catch (Exception e) {
            ErrorLog.WriteErrorLog(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        // Add the new menu items
        inflater.inflate(R.menu.menu_qr_scanner, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createMemoreFragment:
                navController.navigate(R.id.action_QRScannerFragment_to_createMemoreFragment);
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Permissions granted.", Toast.LENGTH_SHORT).show();
                    resumeCamera();
                } else {
                    ErrorLog.WriteDebugLog("REQUEST PERMISSION NOT GRANTED");
                }
            });


    public void resumeCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                if(cameraSource != null) {
                    this.cameraSource.start(surfaceHolder);
                    barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                        @Override
                        public void release() {
                        }

                        @Override
                        public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {

                            final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                            if (barcodeSparseArray.size() != 0) {

                                String scannedValue = barcodeSparseArray.valueAt(0).displayValue;
                                ErrorLog.WriteDebugLog("receiveDetections: " + scannedValue);

                                barcodeDetector.release();
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (NetworkUtil.isNetworkAvailable(requireActivity())) {
                                            ErrorLog.WriteDebugLog("Barcode Scanned, stopping camera");
//                                        cameraSource.release();
                                            cameraSource.stop();
                                            highlightViewModel.getHighlightFromQR(scannedValue);
                                        } else {
                                            toastNoInternet.cancel();
                                            toastNoInternet.show();
                                        }
                                    }
                                });

                            }

                        }
                    });
                    ErrorLog.WriteDebugLog("Camera start again");
                }
            } catch (IOException e) {
                ErrorLog.WriteDebugLog("Camera stop error");

                ErrorLog.WriteDebugLog(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(cameraSource != null){
            ErrorLog.WriteDebugLog("Camera released");
            cameraSource.release();
            cameraSource.stop();
        }

        if(barcodeDetector!=null){
            ErrorLog.WriteDebugLog("barcode released");
            barcodeDetector.release();
        }
    }
}