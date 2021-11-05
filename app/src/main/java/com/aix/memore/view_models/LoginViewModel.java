package com.aix.memore.view_models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.UserInfo;
import com.aix.memore.repositories.FirebaseLoginRepo;


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
}
