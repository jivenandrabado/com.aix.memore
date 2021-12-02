package com.aix.memore.view_models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Memore;
import com.aix.memore.repositories.FirebaseRegistrationRepo;
import com.aix.memore.repositories.MemoreRepo;

public class MemoreViewModel extends ViewModel {

    private MemoreRepo memoreRepo = new MemoreRepo(new FirebaseRegistrationRepo());
    private MutableLiveData<Memore> memoreMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Bitmap> qrBitmap = new MutableLiveData<>();
    public MutableLiveData<Boolean> isEdit = new MutableLiveData<>();
    public MutableLiveData<Boolean> isHighlighUploaded = new MutableLiveData<>();



    public void createMemore(Memore memore){
//        memoreRepo.createMemore(memore);
    }

    public MutableLiveData<Memore> getMemoreMutableLiveData(){
        return memoreMutableLiveData;
    }
    public MutableLiveData<Bitmap> getQrBitmap(){return  qrBitmap;}
    public void createHighlightId(){memoreRepo.createCreateHightlightId();
    }
    public MutableLiveData<String> getHighlightId() { return memoreRepo.getHighlightId();}

    public void uploadHighlightToFirebase(Memore memore, Bitmap qrBitmap, Context context){
        memoreRepo.uploadHighlightToFirebaseStorage(memore, qrBitmap, context);
    }

    public void updateHightlightToFirebase(Memore memore, Context context){
        memoreRepo.updateHighlightToFirebaseStorage(memore, context);
    }

    public void uploadBioProfilePicToFirebaseStorage(Memore memore){
        memoreRepo.uploadBioProfilePicToFirebaseStorage(memore.getMemore_id(), Uri.parse(memore.getBio_profile_pic()));
    }

    public void updateMemore(Memore memore){
        memoreRepo.updateMemore(memore);
    }

    public MutableLiveData<Boolean> memoreSaved(){
        return memoreRepo.memoreSaved;
    }

    public MutableLiveData<String> getErrorMessage(){
        return memoreRepo.errorMessage;
    }

    public MutableLiveData<Double> getMemoreUploadProgress(){
        return memoreRepo.uploadProgresMemore;
    }

    public void checkIfMemoreExists(Memore memore){
        memoreRepo.checkIfMemoreExists(memore);
    }

    public MutableLiveData<Memore> existingMemore(){
        return memoreRepo.existingMemore;
    }

    public MutableLiveData<Boolean> memoreExists(){
        return memoreRepo.memoreExists;
    }

    public void verifyEmailAddress(String email_address, String memore_id){
        memoreRepo.verifyEmailAddress(email_address, memore_id);
    }

    public MutableLiveData<Boolean> isEmailVerified(){
        return memoreRepo.getIsEmailVerified();
    }

    public void setNewPassword(String password, String memore_id){
        memoreRepo.setNewPassword(password,memore_id);
    }

    public MutableLiveData<Boolean> isPasswordReset(){
        return memoreRepo.getIsPasswordReset();
    }
}
