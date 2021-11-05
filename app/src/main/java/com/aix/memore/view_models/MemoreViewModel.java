package com.aix.memore.view_models;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Memore;
import com.aix.memore.repositories.MemoreRepo;

public class MemoreViewModel extends ViewModel {

    private MemoreRepo memoreRepo = new MemoreRepo();
    private MutableLiveData<Memore> memoreMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Bitmap> qrBitmap = new MutableLiveData<>();



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
}
