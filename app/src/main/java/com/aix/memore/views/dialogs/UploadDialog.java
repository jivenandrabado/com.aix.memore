package com.aix.memore.views.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentUploadDialogBinding;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.MemoreViewModel;


public class UploadDialog extends DialogFragment {

    private GalleryViewModel galleryViewModel;
    private FragmentUploadDialogBinding binding;
    private MemoreViewModel memoreViewModel;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUploadDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);


        galleryViewModel.uploadProgress().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.progressBar.setProgress(aDouble.intValue());
                binding.textViewProgressText.setText(aDouble.intValue()+"%");
            }
        });

        memoreViewModel.getMemoreUploadProgress().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.progressBar.setProgress(aDouble.intValue());
                binding.textViewProgressText.setText(aDouble.intValue()+"%");
            }
        });
    }
}