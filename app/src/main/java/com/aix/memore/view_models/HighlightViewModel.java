package com.aix.memore.view_models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.repositories.HighlightRepo;

public class HighlightViewModel extends ViewModel {

    private HighlightRepo highlightRepo = new HighlightRepo();
    private MutableLiveData<String> scannedValue = new MutableLiveData<>();

    public void getHighlight(HighlightInterface highlightInterface,String id){
        highlightRepo.getHighlight(highlightInterface,id);
    }
    public MutableLiveData<String> getScannedValue() {return scannedValue;}

}
