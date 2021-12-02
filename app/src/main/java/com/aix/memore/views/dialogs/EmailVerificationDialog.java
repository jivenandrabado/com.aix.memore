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
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentEmailVerificationDialogBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.MemoreViewModel;

public class EmailVerificationDialog extends DialogFragment {

    private FragmentEmailVerificationDialogBinding binding;
    private MemoreViewModel memoreViewModel;
    private HighlightViewModel highlightViewModel;
    private GalleryViewModel galleryViewModel;
    private Memore memore;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmailVerificationDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);

        memore = galleryViewModel.getBio().getValue();

        if (memore != null) {
            String email = memore.getOwner_email();

            String emailDisplay = email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
            binding.textViewEmail.setText(emailDisplay);
        }

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memoreViewModel.verifyEmailAddress(String.valueOf(binding.editTextEmail.getText()).trim(),memore.getMemore_id());
            }
        });

        //observer for email  verification

        memoreViewModel.isEmailVerified().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean!=null) {
                    if (aBoolean) {
                        ErrorLog.WriteDebugLog("EMAIL VERIFIED");
                        NewPasswordDialog newPasswordDialog = new NewPasswordDialog();
                        newPasswordDialog.show(getChildFragmentManager(),"NEW PASSWORD DIALOG");
                    } else {
                        ErrorLog.WriteDebugLog("EMAIL NOT VERIFIED");
                        Toast.makeText(requireContext(), "Email does not match. Try again.", Toast.LENGTH_SHORT).show();
                    }
                    memoreViewModel.isEmailVerified().setValue(null);

                }
            }
        });

        //observer password reset
        memoreViewModel.isPasswordReset().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean!=null) {
                    if (aBoolean) {
                        Toast.makeText(requireContext(), "Password reset success!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                    memoreViewModel.isPasswordReset().setValue(null);
                }
            }
        });



    }
}