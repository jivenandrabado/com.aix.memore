package com.aix.memore.views.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentQRGeneratorBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.MemoreViewModel;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class QRGeneratorFragment extends Fragment {

    private FragmentQRGeneratorBinding binding;
    private String savePath =  Environment.getDataDirectory().getPath()+"/QRCode/";
    private MemoreViewModel memoreViewModel;
    private String doc_id;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQRGeneratorBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        memoreViewModel.createHighlightId();
        doc_id = String.valueOf(memoreViewModel.getHighlightId().getValue());

        Memore memore = memoreViewModel.getMemoreMutableLiveData().getValue();
        if (memore != null) {
            memore.setMemore_id(doc_id);
            memoreViewModel.getMemoreMutableLiveData().setValue(memore);
        }

        generateQRcode();

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doc_id.isEmpty()) {
                    navController.navigate(R.id.action_QRGeneratorFragment_to_registrationFragment);
                }
            }
        });

    }


    private void generateQRcode(){
        QRGEncoder qrgEncoder = new QRGEncoder(doc_id, null, QRGContents.Type.TEXT, 500);
        Bitmap bitmap;

        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            binding.imageViewQRCode.setImageBitmap(bitmap);

            memoreViewModel.getQrBitmap().setValue(bitmap);

        } catch (WriterException e) {
            ErrorLog.WriteErrorLog(e);
        }
    }

}