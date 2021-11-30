package com.aix.memore.views.fragments.user;

import android.os.Bundle;
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

import com.aix.memore.databinding.FragmentForgotPasswordBinding;
import com.aix.memore.utilities.ToastUtil;
import com.aix.memore.view_models.LoginViewModel;


public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding fragmentForgotPasswordBinding;
    private LoginViewModel loginViewModel;
    private NavController navController;
    private ToastUtil toastUtil;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentForgotPasswordBinding = FragmentForgotPasswordBinding.inflate(inflater,container,false);
        return fragmentForgotPasswordBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toastUtil = new ToastUtil();
        navController = Navigation.findNavController(view);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        fragmentForgotPasswordBinding.buttonResetPassword.setOnClickListener(btn -> {
            if(!String.valueOf(fragmentForgotPasswordBinding.editTextEmail.getText()).isEmpty()) {
                loginViewModel.resetPassword(String.valueOf(fragmentForgotPasswordBinding.editTextEmail.getText()));
            }else{
                toastUtil.makeText(requireContext(),"Empty email field", Toast.LENGTH_LONG);
            }
        });

        loginViewModel.getResetResult().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
//                    navController.navigate(R.id.action_forgotPasswordFragment_to_listingFragment);
                    toastUtil.makeText(requireContext(),"Email has been sent to your email", Toast.LENGTH_LONG);
                }
            }
        });

        loginViewModel.getErrorMessage().observe(getViewLifecycleOwner(), s -> {
            if(!s.isEmpty()) {
                toastUtil.makeText(requireContext(),
                        s,
                        Toast.LENGTH_LONG);
            }
        });


    }
}