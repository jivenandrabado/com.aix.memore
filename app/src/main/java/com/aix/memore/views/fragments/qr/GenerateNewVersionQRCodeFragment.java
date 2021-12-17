package com.aix.memore.views.fragments.qr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentGenerateNewVersionQRCodeBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GenerateNewVersionQRCodeFragment extends Fragment {

    private FragmentGenerateNewVersionQRCodeBinding binding;
    private HighlightViewModel highlightViewModel;
    private GalleryViewModel galleryViewModel;
    private Bitmap result;
    private String full_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGenerateNewVersionQRCodeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        Memore memore = galleryViewModel.getBio().getValue();

        String qr_value = String.valueOf(highlightViewModel.scannedJSONObject().getValue());
        if (memore != null) {
            full_name = memore.getBio_first_name() + " "+memore.getBio_last_name();
            generateQRcode(full_name,qr_value);
        }else{
            Toast.makeText(requireContext(), "Failed to generate QR Code. Try again", Toast.LENGTH_SHORT).show();
        }

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (memore != null) {
                        if(result!=null) {
                            galleryViewModel.uploadBitmapToVault(memore.getMemore_id(), result, requireContext());
                        }else{
                            Toast.makeText(requireContext(), "Failed to save QR Code. Try again", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(requireContext(), "Failed to save QR Code. Try again", Toast.LENGTH_SHORT).show();
                    }

            }
        });

    }

    private void generateQRcode(String name, String qr_code_value){
        QRGEncoder qrgEncoder = new QRGEncoder(qr_code_value, null, QRGContents.Type.TEXT, 400);
        Bitmap bitmap;
        try {
            //generate QR code
            bitmap = qrgEncoder.encodeAsBitmap();

            result = Bitmap.createBitmap(400, 450, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(result);

            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.black));
            paint.setTextSize(55);
            paint.setAntiAlias(true);
            paint.setUnderlineText(false);
            paint.setTextAlign(Paint.Align.CENTER);
            Rect textBounds = new Rect();
            paint.getTextBounds(name,0,name.length(),textBounds);

            //draw
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            canvas.drawText(name, 100, 410, paint);

            binding.imageViewQRCode.setImageBitmap(result);

        } catch (WriterException e) {
            ErrorLog.WriteErrorLog(e);
        }
    }
}