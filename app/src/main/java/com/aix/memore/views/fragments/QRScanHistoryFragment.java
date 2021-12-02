package com.aix.memore.views.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentQRScanHistoryBinding;
import com.aix.memore.interfaces.QRScanHistoryInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.adapters.ScanHistoryAdapter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QRScanHistoryFragment extends Fragment implements QRScanHistoryInterface {
    private static final String KEY_PREFS = "memore_history";
    private FragmentQRScanHistoryBinding binding;
    private ScanHistoryAdapter scanHistoryAdapter;
    private HighlightViewModel highlightViewModel;
    private NavController navController;
    private QRScanHistoryInterface qrScanHistoryInterface;
    private SharedPreferences prefs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQRScanHistoryBinding.inflate(inflater,container,false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        navController = Navigation.findNavController(view);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        this.qrScanHistoryInterface = this;
        initRecyclerView();
        initHighlightObserver();
        initGalleryPicker();
    }

    private void initGalleryPicker() {

        binding.buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
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
                                Result result1 = null;
                                try {
                                    result1 = reader.decode(bBitmap);
                                    ErrorLog.WriteDebugLog("QR CODE VALUE " + result1.getText());
//                                    highlightViewModel.getHighlightFromJSON(new JSONObject(result1.getText()));
                                    highlightViewModel.getHighlightFromQR(result1.getText());
                                } catch (NotFoundException e) {
                                    ErrorLog.WriteDebugLog("DECODER EXCEPTION " + e);
                                    Toast.makeText(requireContext(), "Image does not contain QR Code. Try again.", Toast.LENGTH_SHORT).show();

                                }
//                                catch (JSONException e) {
//                                    ErrorLog.WriteDebugLog("JSON Exception "+e);
//                                    e.printStackTrace();
//                                    if(result1!=null) {
//                                        //check if it is old qr code
//                                        highlightViewModel.checkOldQRHighlight(result1.getText());
//                                    }else {
//                                        Toast.makeText(requireContext(), "Invalid QR Code. Try again.", Toast.LENGTH_SHORT).show();
//                                    }
//                                }

                            }catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }



                        }
                    }
                }
            }
    );


    private void initRecyclerView() {
        scanHistoryAdapter = new ScanHistoryAdapter(requireContext(),  getArrayList("memore_history"),this);
        binding.recyclerViewMemoreHistory.setAdapter(scanHistoryAdapter);
        binding.recyclerViewMemoreHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewMemoreHistory.setItemAnimator(null);
    }

    public List<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public List<Memore> getList(){
        List<Memore> arrayItems = new ArrayList<>();
        String serializedObject = prefs.getString(KEY_PREFS, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Memore>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
            return arrayItems;
        }
        return arrayItems;
    }

    @Override
    public void onClick(String memore) {
        highlightViewModel.getHighlightFromQR(memore);
    }

    @Override
    public void onCancelGenerateNewQR() {

    }

    private void initHighlightObserver() {
        try{
            highlightViewModel.memoreFound().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(aBoolean != null) {
                        if (aBoolean) {
                            navController.navigate(R.id.action_QRScanHistoryFragment_to_HighlightFragment);
                        } else {
                            Toast.makeText(requireContext(), "Invalid QR Code,", Toast.LENGTH_SHORT).show();
                        }
                        highlightViewModel.memoreFound().setValue(null);
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }
}