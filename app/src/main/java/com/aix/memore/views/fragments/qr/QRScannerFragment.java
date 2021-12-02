package com.aix.memore.views.fragments.qr;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.aix.memore.view_models.HighlightViewModel;
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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class QRScannerFragment extends Fragment implements OnQrCodeScanned, FragmentPermissionInterface {

    private FragmentQrScannerBinding binding;
    private QRScannerHelper qrScannerHelper;
    private NavController navController;
    private HighlightViewModel highlightViewModel;
    private OnQrCodeScanned onQrCodeScanned;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPermissionHelper.requestMultiplePermissions(requireActivity(),this);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater,container,false);
        return binding.getRoot();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            qrScannerHelper = new QRScannerHelper(requireContext());
            navController = Navigation.findNavController(view);
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
            this.onQrCodeScanned = onQrCodeScanned;

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

            initHighlightObserver();

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }


    }

    private void initHighlightObserver() {
        try{
            highlightViewModel.memoreFound().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(aBoolean != null) {
                        if (aBoolean) {
                            navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
                        } else {
                            Toast.makeText(requireContext(), "Invalid QR Code,", Toast.LENGTH_SHORT).show();
                            qrScannerHelper.resumeScanning(binding.surfaceViewQRscanner,requireContext(),AppPermissionHelper.cameraPermissionGranted(requireContext()), highlightViewModel);
                        }
                        highlightViewModel.memoreFound().setValue(null);

                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ErrorLog.WriteDebugLog("init qr Scanner on resume");
        if(AppPermissionHelper.cameraPermissionGranted(requireContext())) {
            qrScannerHelper.initQRScannerPreview(requireContext(), binding.surfaceViewQRscanner, getActivity(),
                    this, AppPermissionHelper.cameraPermissionGranted(requireContext()), highlightViewModel);
        }

    }

    @Override
    public void barcodeScanned(String value) {
        try {
            if(!value.contains("https")) {
                highlightViewModel.getScannedValue().setValue(value);
                navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
            }else{
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(value));
//                startActivity(i);
            }
        }catch(Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }


    @Override
    public void onGranted(Map<String, Boolean> isGranted) {
        ErrorLog.WriteDebugLog("init qr Scanner on permission granted "+ AppPermissionHelper.cameraPermissionGranted(requireContext()));
//        qrScannerHelper.initQRScannerPreview(requireContext(), binding.surfaceViewQRscanner, getActivity(), this);
//        qrScannerHelper.initialiseDetectorsAndSources(binding);

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
        switch (item.getItemId()){
            case R.id.createMemoreFragment:
                navController.navigate(R.id.action_QRScannerFragment_to_createMemoreFragment);
                break;

                default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}