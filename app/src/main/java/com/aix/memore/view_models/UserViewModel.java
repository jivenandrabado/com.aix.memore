package com.aix.memore.view_models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.repositories.UserRepo;

public class UserViewModel extends ViewModel {

    private UserRepo userRepo = new UserRepo();
    public void verifyPassword(String doc_id, String password){
        userRepo.verifyPassword(doc_id,password);
    }

    public MutableLiveData<Boolean> isAuthorized(){
        return userRepo.getIsAuthorized();
    }

    public MutableLiveData<String> getErrorMessage(){
        return userRepo.getErrorMessage();
    }
}
