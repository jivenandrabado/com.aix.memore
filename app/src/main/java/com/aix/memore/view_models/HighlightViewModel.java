package com.aix.memore.view_models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.repositories.HighlightRepo;

import org.json.JSONObject;

public class HighlightViewModel extends ViewModel {

    private HighlightRepo highlightRepo = new HighlightRepo();
    private MutableLiveData<String> scannedValue = new MutableLiveData<>();

    public void getHighlight(HighlightInterface highlightInterface,String id){
        highlightRepo.getHighlight(highlightInterface,id);
    }
    public MutableLiveData<String> getScannedValue() {return scannedValue;}

    public void addhighlightView(Memore memore){
        highlightRepo.addHighlightView(memore);
    }

    public void getHighlightFromJSON(JSONObject scannedObject) {
        highlightRepo.getHighlightFromJSON(scannedObject, scannedValue);
    }

    public MutableLiveData<Boolean> memoreFound(){
        return highlightRepo.getMemoreFound();
    }

    public MutableLiveData<JSONObject> scannedJSONObject(){
        return highlightRepo.getScannedJSONObject();
    }

    public void checkOldQRHighlight(String doc_id){
        highlightRepo.checkOldQRCode(doc_id,scannedValue);
    }

    public MutableLiveData<Boolean> getOldQRHighlightExists(){
        return highlightRepo.getOldMemoreQRFound();
    }

    public void getHighlightFromQR(String doc_id) {
        highlightRepo.getHighlightFromQR(doc_id,scannedValue);
    }
}
