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
import com.aix.memore.databinding.FragmentPasswordDialogBinding;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;

public class PasswordDialog extends DialogFragment {

    private FragmentPasswordDialogBinding binding;
    private UserViewModel userViewModel;
    private HighlightViewModel highlightViewModel;
    private String doc_id;


    public PasswordDialog() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPasswordDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //viewmodel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        highlightViewModel.getScannedValue().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                doc_id = s;
            }
        });

        binding.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userViewModel.verifyPassword(doc_id, String.valueOf(binding.edittextPassword.getText()));
            }
        });


        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.equals("")){
                    Toast.makeText(requireActivity().getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                    userViewModel.getErrorMessage().setValue("");
                }
            }
        });

        binding.textViewFrogotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmailVerificationDialog emailVerificationDialog = new EmailVerificationDialog();
                emailVerificationDialog.show(getChildFragmentManager(),"EMAIL VERIFICATION DIALOG");
            }
        });
    }
}