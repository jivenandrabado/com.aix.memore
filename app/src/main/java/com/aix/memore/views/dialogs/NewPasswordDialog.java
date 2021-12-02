package com.aix.memore.views.dialogs;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentNewPasswordDialogBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.MemoreViewModel;

public class NewPasswordDialog extends DialogFragment {

    private FragmentNewPasswordDialogBinding binding;
    private MemoreViewModel memoreViewModel;
    private GalleryViewModel galleryViewModel;
    private Memore memore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewPasswordDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);

        memore = galleryViewModel.getBio().getValue();

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = String.valueOf(binding.edittextPassword.getText()).trim();
                String confirmPassword = String.valueOf(binding.edittextConfirmPassword.getText()).trim();

                if(!isEmptyFields(password,confirmPassword)){
                    memoreViewModel.setNewPassword(password,memore.getMemore_id());
                }
            }
        });

        memoreViewModel.isPasswordReset().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean!=null) {
                    if (aBoolean) {
                        ErrorLog.WriteDebugLog("Password reset success");
                        dismiss();
                    } else {
                        ErrorLog.WriteDebugLog("Password reset failed");
                        Toast.makeText(requireContext(), "Password reset failed. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                    memoreViewModel.isPasswordReset().setValue(null);
                }
            }
        });


    }

    private boolean isEmptyFields(String password, String confirmPassword){

        int passwordLength = 6;
        if (TextUtils.isEmpty(password)){
            Toast.makeText(requireContext(), "Empty password", Toast.LENGTH_LONG).show();
            return true;
        }else if ( password.length() < passwordLength){
            Toast.makeText(requireContext(), "Password must be atleast 6 characters", Toast.LENGTH_LONG).show();
            return true;
        }else if (confirmPassword.isEmpty()){
            Toast.makeText(requireContext(), "Confirm Password Empty", Toast.LENGTH_LONG).show();
            return true;
        }else if(!confirmPassword.equals(password)){
            Toast.makeText(requireContext(), "Password does not match", Toast.LENGTH_LONG).show();
            return true;
        }

        else{
            return false;
        }

    }
}