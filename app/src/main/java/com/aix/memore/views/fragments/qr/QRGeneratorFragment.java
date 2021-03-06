package com.aix.memore.views.fragments.qr;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.MemoreViewModel;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class QRGeneratorFragment extends Fragment {

    private FragmentQRGeneratorBinding binding;
    private String savePath =  Environment.getDataDirectory().getPath()+"/QRCode/";
    private MemoreViewModel memoreViewModel;
    private String doc_id;
    private NavController navController;
    private Memore memore;
    private String full_name, first_name, last_name,birth_date,death_date, qr_code_value;
    private HighlightViewModel highlightViewModel;
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
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);

        memore = memoreViewModel.getMemoreMutableLiveData().getValue();
        if (memore != null) {
            memore.setMemore_id(doc_id);
            memoreViewModel.getMemoreMutableLiveData().setValue(memore);
//            qr_code_value =  "{" +
//                    "first_name='" + memore.getBio_first_name() + '\'' +
//                    ", last_name='" + memore.getBio_last_name() + '\'' +
//                    ", birth_date='" + memore.getBio_birth_date() + '\'' +
//                    ", death_date='" + memore.getBio_death_date() + '\'' +
//                    '}';

            full_name = memore.getBio_first_name() + "\n "+memore.getBio_last_name();

//            try {
//                highlightViewModel.scannedJSONObject().setValue(new JSONObject(qr_code_value));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            generateQRcode(full_name,memore.getMemore_id());

            binding.buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!doc_id.isEmpty()) {
                        navController.navigate(R.id.action_QRGeneratorFragment_to_registrationFragment);
                    }
                }
            });

        }else {
            Toast.makeText(requireActivity().getApplicationContext(),"Failed to generate QR Code. Try again", Toast.LENGTH_LONG).show();
        }


    }

    private void generateQRcode(String name, String qr_code_value){
        QRGEncoder qrgEncoder = new QRGEncoder(qr_code_value, null, QRGContents.Type.TEXT, 400);
        Bitmap bitmap;

        try {
            //generate QR code
            bitmap = qrgEncoder.encodeAsBitmap();

            Bitmap result = Bitmap.createBitmap(400, 480, Bitmap.Config.ARGB_8888);

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
            canvas.drawText(memore.getBio_first_name(), 210, 410, paint);
            canvas.drawText(memore.getBio_last_name(), 210, 460, paint);

            binding.imageViewQRCode.setImageBitmap(result);

            memoreViewModel.getQrBitmap().setValue(result);

        } catch (WriterException e) {
            ErrorLog.WriteErrorLog(e);
        }
    }

}