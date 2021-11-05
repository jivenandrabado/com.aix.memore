package com.aix.memore.views.fragments;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.Map;


public class QRScannerFragment extends Fragment implements OnQrCodeScanned, FragmentPermissionInterface {

    private FragmentQrScannerBinding binding;
    private QRScannerHelper qrScannerHelper;
    private NavController navController;
    private HighlightViewModel highlightViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPermissionHelper.requestMultiplePermissions(requireActivity(),this);

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
//            binding.bottomNav.getMenu().getItem(0).setCheckable(false);
//            binding.bottomNav.getMenu().getItem(1).setCheckable(false);

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
                    this, AppPermissionHelper.cameraPermissionGranted(requireContext()));
        }

    }

    @Override
    public void barcodeScanned(String value) {
        try {
            if(!value.contains("https")) {
                highlightViewModel.getScannedValue().setValue(value);
                navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
            }else{
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(value));
                startActivity(i);
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
}