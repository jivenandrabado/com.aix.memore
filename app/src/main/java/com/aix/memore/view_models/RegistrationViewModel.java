package com.aix.memore.view_models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Memore;
import com.aix.memore.models.UserInfo;
import com.aix.memore.repositories.FirebaseLoginRepo;
import com.aix.memore.repositories.FirebaseRegistrationRepo;
import com.aix.memore.repositories.GalleryRepo;


public class RegistrationViewModel extends ViewModel {

    private final FirebaseLoginRepo firebaseLoginRepo = new FirebaseLoginRepo();
    private final FirebaseRegistrationRepo firebaseRegistrationRepo = new FirebaseRegistrationRepo(firebaseLoginRepo);
    private GalleryRepo galleryRepo = new GalleryRepo();
    public void registerUser(UserInfo userInfo, String password, Memore memore, Bitmap qrBitmap, Context context){
        firebaseRegistrationRepo.registerUser(password, userInfo, memore, qrBitmap, context);
    }
    public MutableLiveData<Boolean> isRegistered(){return firebaseRegistrationRepo.getIsRegistered();}
    public MutableLiveData<String> getErrorMessage(){return firebaseRegistrationRepo.getErrorMessage();}


}
