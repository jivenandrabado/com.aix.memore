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
import com.aix.memore.databinding.FragmentMemoreExistsDialogBinding;
import com.aix.memore.interfaces.CreateMemoreInterface;
import com.aix.memore.view_models.MemoreViewModel;


public class MemoreExistsDialog extends DialogFragment {
    private MemoreViewModel memoreViewModel;
    private CreateMemoreInterface createMemoreInterface;
    private FragmentMemoreExistsDialogBinding binding;

    public MemoreExistsDialog(MemoreViewModel memoreViewModel, CreateMemoreInterface createMemoreInterface) {
        this.memoreViewModel = memoreViewModel;
        this.createMemoreInterface = createMemoreInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMemoreExistsDialogBinding.inflate(inflater, container,false);
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
                createMemoreInterface.onMemoreExists(memoreViewModel.existingMemore().getValue());
                dismiss();
            }
        });

    }
}