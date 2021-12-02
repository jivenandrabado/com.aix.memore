package com.aix.memore.views.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentUpdateQRCodeDialogBinding;
import com.aix.memore.databinding.FragmentUploadDialogBinding;
import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.interfaces.QRScanHistoryInterface;
import com.aix.memore.view_models.MemoreViewModel;


public class UpdateQRCodeDialog extends DialogFragment {

    private FragmentUpdateQRCodeDialogBinding binding;
    private HighlightInterface highlightInterface;

    public UpdateQRCodeDialog(HighlightInterface highlightInterface) {
        this.highlightInterface = highlightInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateQRCodeDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highlightInterface.onGenerateNewQRCode();
            }
        });

        binding.buttonLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });



    }
}