package com.aix.memore.views.fragments.qr;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
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

            binding.buttonExplore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseImage();
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
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        chooseImageActivityResult.launch(intent);
    }

    private ActivityResultLauncher<Intent> chooseImageActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri imageUri = clipData.getItemAt(i).getUri();
                                // your code for multiple image selection
                                ErrorLog.WriteDebugLog("DATA RECEIVED "+imageUri);
                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);

                            InputStream inputStream = null;
                            try {
                                inputStream = requireContext().getContentResolver().openInputStream(uri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                if (bitmap == null) {
                                    Log.e("TAG", "uri is not a bitmap," + uri.toString());
                                    return;
                                }
                                int width = bitmap.getWidth(), height = bitmap.getHeight();
                                int[] pixels = new int[width * height];
                                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                                bitmap.recycle();
                                bitmap = null;
                                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                                MultiFormatReader reader = new MultiFormatReader();
                                try {
                                    Result result1 = reader.decode(bBitmap);
                                    ErrorLog.WriteDebugLog("QR CODE VALUE " + result1.getText());
                                    highlightViewModel.getScannedValue().setValue(result1.getText());
                                    navController.navigate(R.id.action_QRScannerFragment_to_HighlightFragment2);
                                } catch (NotFoundException e) {
                                    ErrorLog.WriteDebugLog("DECODER EXCEPTION " + e);
                                }

                            }catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }



                        }
                    }
                }
            }
    );

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
            case R.id.userFragment:
                navController.navigate(R.id.action_QRScannerFragment_to_userFragment);
                break;

                default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}