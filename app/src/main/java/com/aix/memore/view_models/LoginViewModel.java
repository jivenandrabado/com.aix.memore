package com.aix.memore.view_models;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.UserInfo;
import com.aix.memore.repositories.FirebaseLoginRepo;
import com.google.firebase.auth.PhoneAuthCredential;


public class LoginViewModel extends ViewModel{

    MutableLiveData<Boolean> resetSuccess;
    FirebaseLoginRepo firebaseLoginRepo = new FirebaseLoginRepo();
    public void usernamePasswordLogin(String email, String password){
        firebaseLoginRepo.loginUserUsernamePassword(email,password);
    }

    public void resetPassword(String email){
        firebaseLoginRepo.resetPassword(email);
    }

    public MutableLiveData<Boolean> getResetResult(){
        return firebaseLoginRepo.getResetPasswordSuccess();
    }

    public void loginWithGoogle(String idToken, UserInfo userInfo){
        firebaseLoginRepo.loginWithGoogle(idToken,userInfo);
    }

    public void loginAsGuest(){
        firebaseLoginRepo.loginAsGuest();
    }

    public MutableLiveData<String> getErrorMessage(){
        return firebaseLoginRepo.getErrorMessage();
    }

    public void loginWithMobile(String mobile_no, FragmentActivity fragmentActivity) {
        firebaseLoginRepo.loginWithMobile(mobile_no, fragmentActivity);
    }

    public MutableLiveData<Boolean> isOTPSent(){
        return firebaseLoginRepo.isOTPsent;
    }

    public void loginWithMobileWithOTP(PhoneAuthCredential phoneAuthCredential, FragmentActivity fragmentActivity) {
        firebaseLoginRepo.signInWithPhoneAuthCredential(phoneAuthCredential,fragmentActivity);
    }

    public MutableLiveData<String> verificationID(){
        return firebaseLoginRepo.getOTPverificationID();
    }

    public MutableLiveData<Boolean> isUserLoggedIn(){
        return firebaseLoginRepo.isUserLoggedIn();
    }
}
