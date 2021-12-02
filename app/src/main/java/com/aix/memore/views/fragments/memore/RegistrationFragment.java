package com.aix.memore.views.fragments.memore;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.aix.memore.R;
import com.aix.memore.databinding.FragmentRegistrationBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.models.UserInfo;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.ToastUtil;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.MemoreViewModel;
import com.aix.memore.view_models.RegistrationViewModel;
import com.aix.memore.view_models.UserSharedViewModel;
import com.aix.memore.views.dialogs.ProgressDialogFragment;
import com.aix.memore.views.dialogs.ShareDialog;
import com.aix.memore.views.dialogs.UploadDialog;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;


public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding fragmentRegistrationBinding;
    private RegistrationViewModel registrationViewModel;
    private UserInfo userInfo;
    private String email, password, firstName,middleName,lastName, mobile_no, confirm_password;
    private ToastUtil toastUtil;
    private UserSharedViewModel userSharedViewModel;
    private NavController navController;
    private ProgressDialogFragment progressDialogFragment;
    private String dialogTag = "UPLOAD HIGHLIGHT DIALOG";
    private MemoreViewModel memoreViewModel;
    private Memore memore;
    private Bitmap qrBitmap;
    private HighlightViewModel highlightViewModel;
    private UploadDialog uploadDialog;
    private ShareDialog shareDialog;


    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentRegistrationBinding = FragmentRegistrationBinding.inflate(inflater,container,false);
        return fragmentRegistrationBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toastUtil = new ToastUtil();
        navController = Navigation.findNavController(view);
        registrationViewModel = new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);
        userSharedViewModel = new ViewModelProvider(requireActivity()).get(UserSharedViewModel.class);
        userInfo = new UserInfo();
        progressDialogFragment = new ProgressDialogFragment();
        uploadDialog = new UploadDialog();
        shareDialog = new ShareDialog();

        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        initMemore();

        userSharedViewModel.isUserLoggedin().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
//                navController.navigate(R.id.action_registrationFragment_to_parkingSpaceListingFragment);
//                navController.popBackStack(R.id.registrationFragment,true);
//                ErrorLog.WriteDebugLog("SAVED MEMORE AND CREATED USER");
            }
        });

//        registrationViewModel.isRegistered().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                if (progressDialogFragment.getTag() != null && progressDialogFragment.getTag().equals(dialogTag)) {
//                    progressDialogFragment.dismiss();
//                    ErrorLog.WriteDebugLog("SAVED MEMORE AND CREATED USER");
//                }
//                if(aBoolean){
//                    toastUtil.toastRegistrationSucces(requireContext());
//                    highlightViewModel.getScannedValue().setValue(memore.getMemore_id());
//                    navController.navigate(R.id.action_registrationFragment_to_HighlightFragment2);
//                    registrationViewModel.isRegistered().setValue(false);
//
//                }
//            }
//        });

        memoreViewModel.memoreSaved().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (uploadDialog.getTag() != null && uploadDialog.getTag().equals(dialogTag)) {
                    uploadDialog.dismiss();
                    ErrorLog.WriteDebugLog("SAVED MEMORE AND CREATED USER");
                }
                if(aBoolean){
                    toastUtil.toastRegistrationSucces(requireContext());
                    highlightViewModel.getScannedValue().setValue(memore.getMemore_id());
                    navController.navigate(R.id.action_registrationFragment_to_HighlightFragment2);
                }
            }
        });

        memoreViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.isEmpty()){
                    toastUtil.makeText(requireContext(), s, Toast.LENGTH_LONG);
                    if(uploadDialog != null){
                        uploadDialog.dismiss();
                    }
                }
            }
        });

        registrationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.isEmpty()){
                    toastUtil.makeText(requireContext(), s, Toast.LENGTH_LONG);
                    if(uploadDialog != null){
                        uploadDialog.dismiss();
                    }
                }
            }
        });

        fragmentRegistrationBinding.buttonRegister.setOnClickListener(btn -> {
            email = String.valueOf(fragmentRegistrationBinding.editTextEmail.getText());
            password = String.valueOf(fragmentRegistrationBinding.regPasswordEditText.getText());
            confirm_password = String.valueOf(fragmentRegistrationBinding.editTextConfirmPassword.getText());

            if(!isEmptyFields(email,password,confirm_password,qrBitmap)){
                userInfo.setEmail(email);

                if(memore != null) {
                    memore.setOwner_email(email);
                    memore.setPassword(password);

                    memoreViewModel.uploadHighlightToFirebase(memore,qrBitmap, requireContext());
                    uploadDialog.show(getChildFragmentManager(),"UPLOAD HIGHLIGHT DIALOG");
                }else{
                    Toast.makeText(requireContext(),"Please try again", Toast.LENGTH_LONG).show();
                }
            }else {
                ErrorLog.WriteDebugLog("Fields are empty");
            }

        });

    }

    private boolean isEmptyFields(String email, String password, String confirmPassword, Bitmap qrBitmap){

        int passwordLength = 6;
        if (TextUtils.isEmpty(email)){
            toastUtil.makeText(requireContext(), "Empty email", Toast.LENGTH_LONG);
            return true;
        }else if (TextUtils.isEmpty(password)){
            toastUtil.makeText(requireContext(), "Empty password", Toast.LENGTH_LONG);
            return true;
        }else if ( password.length() < passwordLength){
            toastUtil.makeText(requireContext(), "Password must be atleast 6 characters", Toast.LENGTH_LONG);
            return true;
        }else if (confirmPassword.isEmpty()){
            toastUtil.makeText(requireContext(), "Confirm Password Empty", Toast.LENGTH_LONG);
            return true;
        }else if(!confirmPassword.equals(password)){
            toastUtil.makeText(requireContext(), "Password does not match", Toast.LENGTH_LONG);
            return true;
        }else if(qrBitmap == null){
            toastUtil.makeText(requireContext(), "Invalid qr code pls try again", Toast.LENGTH_LONG);
            return true;
        }

        else{
            return false;
        }

    }

    private void initMemore(){
        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        memore = memoreViewModel.getMemoreMutableLiveData().getValue();
        qrBitmap = memoreViewModel.getQrBitmap().getValue();
        ErrorLog.WriteDebugLog("MEMORE STRING "+ memoreViewModel.getMemoreMutableLiveData().getValue().toString());
    }
}