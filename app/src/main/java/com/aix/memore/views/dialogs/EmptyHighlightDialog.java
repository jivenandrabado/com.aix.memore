package com.aix.memore.views.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentEmptyHighlightDialogBinding;
import com.aix.memore.interfaces.UploadHighlightInterface;

public class EmptyHighlightDialog extends DialogFragment {

    private FragmentEmptyHighlightDialogBinding binding;
    private UploadHighlightInterface uploadHighlightInterface;

    public EmptyHighlightDialog(UploadHighlightInterface uploadHighlightInterface) {
        this.uploadHighlightInterface = uploadHighlightInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEmptyHighlightDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        binding.buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadHighlightInterface.onContinueWithoutHighlight();
                dismiss();
            }
        });
    }
}