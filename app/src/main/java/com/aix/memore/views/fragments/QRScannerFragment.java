package com.aix.memore.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentQrScannerBinding;
import com.aix.memore.helpers.AppPermissionHelper;
import com.aix.memore.helpers.QRScannerHelper;
import com.aix.memore.interfaces.OnQrCodeScanned;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.HighlightViewModel;


public class QRScannerFragment extends Fragment implements OnQrCodeScanned {

    private FragmentQrScannerBinding binding;
    private QRScannerHelper qrScannerHelper;
    private NavController navController;
    private HighlightViewModel highlightViewModel;

    public QRScannerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        qrScannerHelper = new QRScannerHelper(requireContext());
        navController = Navigation.findNavController(view);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        binding.buttonManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(AppPermissionHelper.cameraPermissionGranted(requireContext())) {
            ErrorLog.WriteDebugLog("INIT PREVIEW");
            qrScannerHelper.initQRScannerPreview(requireContext(), binding.surfaceViewQRscanner, getActivity(),this);
        }else{
            ErrorLog.WriteDebugLog("Request permission");
            AppPermissionHelper.requestPermission(requireContext());
            AppPermissionHelper.requestLocationPermission(requireContext());
        }

    }

    @Override
    public void barcodeScanned(String value) {
        highlightViewModel.getScannedValue().setValue(value);
        navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
    }
}