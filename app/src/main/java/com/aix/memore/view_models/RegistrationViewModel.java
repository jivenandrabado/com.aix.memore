package com.aix.memore.view_models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Memore;
import com.aix.memore.models.UserInfo;
import com.aix.memore.repositories.FirebaseLoginRepo;
import com.aix.memore.repositories.FirebaseRegistrationRepo;


public class RegistrationViewModel extends ViewModel {

    private final FirebaseLoginRepo firebaseLoginRepo = new FirebaseLoginRepo();
    private final FirebaseRegistrationRepo firebaseRegistrationRepo = new FirebaseRegistrationRepo(firebaseLoginRepo);
    public void registerUser(UserInfo userInfo, String password, Memore memore){
        firebaseRegistrationRepo.registerUser(password, userInfo, memore);
    }
    public MutableLiveData<Boolean> isRegistered(){return firebaseRegistrationRepo.getIsRegistered();}
    public MutableLiveData<String> getErrorMessage(){return firebaseRegistrationRepo.getErrorMessage();}


}
