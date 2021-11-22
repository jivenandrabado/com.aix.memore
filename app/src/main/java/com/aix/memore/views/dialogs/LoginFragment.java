package com.aix.memore.views.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aix.memore.databinding.FragmentLoginBinding;
import com.aix.memore.helpers.FirebaseUserHelper;
import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.interfaces.LoginInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.models.UserInfo;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.NetworkUtil;
import com.aix.memore.utilities.ToastUtil;
import com.aix.memore.view_models.LoginViewModel;
import com.aix.memore.view_models.UserSharedViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class LoginFragment extends DialogFragment implements LoginInterface {

    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;
    private NavController navController;
    private UserSharedViewModel userSharedViewModel;
    private FirebaseUserHelper firebaseUserHelper;
    private ActivityResultLauncher<Intent> activityResultLauncherGoogle, activityResultLauncherFacebook;
    private ProgressDialogFragment progressDialogFragment;
    private String verificationID;

    //facebook
    public final String EMAIL = "email";
//    public CallbackManager callbackManager;
    private ToastUtil toastUtil;
    private String dialogLoginTag = "DialogLoginTag";
    private HighlightInterface highlightInterface;
    private Memore memore;

    public LoginFragment(HighlightInterface highlightInterface, Memore memore) {
        // Required empty public constructor
        this.highlightInterface = highlightInterface;
        this.memore = memore;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initOnActivityResultForGoogle();
//        initOnActivityResultForFacebook();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater,container,false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            toastUtil = new ToastUtil();
            binding.setLoginInterface(this);
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
            userSharedViewModel = new ViewModelProvider(requireActivity()).get(UserSharedViewModel.class);
            firebaseUserHelper = new FirebaseUserHelper();
            progressDialogFragment = new ProgressDialogFragment();

            initLoginListener();
            initMobileNoFilter();
            initOTPListener();
            initDialogMessage();


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    private void initDialogMessage() {
        binding.textViewMessage.setText("Know more about "+memore.getBio_first_name()+"'s life\n Continue with:");
    }

    private void initOTPListener() {
        loginViewModel.verificationID().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.isEmpty()){
                    binding.buttonContinue.setText("OK");
                    verificationID = s;

                    loginViewModel.isOTPSent().setValue(false);
                }
            }
        });

    }

    private void initMobileNoFilter() {

        binding.editTextMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String prefix = "+63";

                int count = (editable == null) ? 0 : editable.toString().length();
                if (count < prefix.length()) {

                    binding.editTextMobileNo.setText(prefix);

                    /*
                     * This line ensure when you do a rapid delete (by holding down the
                     * backspace button), the caret does not end up in the middle of the
                     * prefix.
                     */
                    int selectionIndex = Math.max(count + 1, prefix.length());

                    binding.editTextMobileNo.setSelection(selectionIndex);
                }
            }
        });
    }

    private void initLoginListener(){
        ErrorLog.WriteDebugLog("INIT USER LISTENER");
        loginViewModel.isUserLoggedIn().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                if (progressDialogFragment.getTag() != null && progressDialogFragment.getTag().equals(dialogLoginTag)) {
                    progressDialogFragment.dismiss();
                }
                ErrorLog.WriteDebugLog("Dismiss login dialog");

                dismiss();
                highlightInterface.onCredentialsSubmitted();
            }
        });

        loginViewModel.getErrorMessage().setValue("");
        loginViewModel.getErrorMessage().observe(getViewLifecycleOwner(), s -> {
            if(!s.isEmpty()) {
                toastUtil.makeText(requireContext(),
                        s,
                        Toast.LENGTH_LONG);
                if (progressDialogFragment.getTag() != null && progressDialogFragment.getTag().equals(dialogLoginTag)) {
                    progressDialogFragment.dismiss();
                }
            }
        });

    }


    @Override
    public void onLoginWithGoogle() {
        try {
            if(NetworkUtil.isNetworkAvailable(requireActivity())) {
                firebaseUserHelper.signInWithGoogle(requireActivity(), activityResultLauncherGoogle);
            }else{
                toastUtil.toastNoInternetConnection(requireContext());
            }
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    @Override
    public void onContinueClick() {
        String mobile_no = String.valueOf(binding.editTextMobileNo.getText()).trim();

        if(!mobile_no.isEmpty()){
            loginViewModel.loginWithMobile(mobile_no, requireActivity());
        }else{
            Toast.makeText(requireContext(),"Invalid mobile no.",Toast.LENGTH_SHORT).show();
        }

        if(binding.buttonContinue.getText().equals("OK")){
            String OTP = String.valueOf(binding.editTextOTP.getText());

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, OTP);
            loginViewModel.loginWithMobileWithOTP(credential,requireActivity());
        }

    }

    private boolean isEmptyFields(){
//        if (!getEmail().isEmpty() && !getPassword().isEmpty()) {
//            return false;
//        }
        return true;

    }


    private void initOnActivityResultForGoogle(){
        try {
            activityResultLauncherGoogle = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            ErrorLog.WriteDebugLog("Activity result received" + data);

                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                ErrorLog.WriteDebugLog("firebaseAuthWithGoogle:" + account.getId());
                                UserInfo userInfo = new UserInfo();
                                userInfo.setEmail(account.getEmail());
                                userInfo.setFirst_name(account.getGivenName());
                                userInfo.setLast_name(account.getFamilyName());
                                userInfo.setDate_created(new Date());

                                ErrorLog.WriteDebugLog("firebaseAuthWithGoogle:" + account.getId());
                                loginViewModel.loginWithGoogle(account.getIdToken(), userInfo);
                            } catch (ApiException e) {
                                ErrorLog.WriteDebugLog("Google sign in failed");
                            }

                        }else{
                            ErrorLog.WriteDebugLog("Activity result error" +result.getResultCode());
                        }
                    });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    private void initOnActivityResultForFacebook(){
//        try {
//            activityResultLauncherFacebook = registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            Intent data = result.getData();
//                            ErrorLog.WriteDebugLog("Activity result received" + data);
//                            assert result.getData() != null;
//                            callbackManager.onActivityResult(result.getData().getIntExtra("REQUEST_CODE",0),result.getResultCode(),result.getData());
//
//                        }else{
//                            ErrorLog.WriteDebugLog(result.getData()+"");
//                        }
//                    });
//        }catch (Exception e){
//            ErrorLog.WriteErrorLog(e);
//        }

    }

}