package com.aix.memore.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class HighlightRepo {
    private final FirebaseFirestore db;
    private final MutableLiveData<Boolean> memoreFound = new MutableLiveData<>();
    private final MutableLiveData<JSONObject> scannedJSONObject = new MutableLiveData<>();
    private final MutableLiveData<Boolean> oldMemoreQRFound = new MutableLiveData<>();

    public HighlightRepo() {
        this.db = FirebaseFirestore.getInstance();
    }


    public void getHighlight(HighlightInterface highlightInterface, String id){
        try{

            db.collection(FirebaseConstants.MEMORE)
                    .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Memore memore = task.getResult().toObject(Memore.class);
                    if (memore != null) {
                        highlightInterface.onHighlightFound(memore);
                    }else{
                        highlightInterface.onHighlightFound(null);
                    }
                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void checkOldQRCode(String id,MutableLiveData<String> scannedValue){
        try{

            db.collection(FirebaseConstants.MEMORE)
                    .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Memore memore = task.getResult().toObject(Memore.class);
                            oldMemoreQRFound.setValue(true);
                            String qr_code_value = null;
                            if (memore != null) {
                                qr_code_value = "{" +
                                        "first_name='" + memore.getBio_first_name() + '\'' +
                                        ", last_name='" + memore.getBio_last_name() + '\'' +
                                        ", birth_date='" + memore.getBio_birth_date() + '\'' +
                                        ", death_date='" + memore.getBio_death_date() + '\'' +
                                        '}';

                                try {
                                    scannedJSONObject.setValue(new JSONObject(qr_code_value));
                                    scannedValue.setValue(memore.getMemore_id());
                                    memoreFound.setValue(true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        } else {
                            memoreFound.setValue(false);
                            oldMemoreQRFound.setValue(false);
                        }
                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }

                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void addHighlightView(Memore memore) {
//        Viewer vi
//        db.collection(FirebaseConstants.MEMORE)
//                .document(memore.getMemore_id()).collection(FirebaseConstants.MEMORE_VIEWER)
//                .document().set()
    }

    public void getHighlightFromJSON(JSONObject scannedObject, MutableLiveData<String> scannedValue) {
        try{
            db.collection(FirebaseConstants.MEMORE).whereEqualTo("bio_first_name",scannedObject.get("first_name"))
                    .whereEqualTo("bio_last_name",scannedObject.get("last_name"))
                    .whereEqualTo("bio_birth_date", DateHelper.stringToDate2(String.valueOf(scannedObject.get("birth_date"))))
                    .whereEqualTo("bio_death_date", DateHelper.stringToDate2(String.valueOf(scannedObject.get("death_date"))))
                    .limit(1)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        List<Memore> memore = task.getResult().toObjects(Memore.class);
                        if(memore.size() > 0){
                            ErrorLog.WriteDebugLog("Memore found " +scannedObject);
                            scannedJSONObject.setValue(scannedObject);
                            scannedValue.setValue(memore.get(0).getMemore_id());
                            memoreFound.setValue(true);
                        }else{
                            ErrorLog.WriteDebugLog("Memore not found");
                            memoreFound.setValue(false);
                        }
                    }else{
                        ErrorLog.WriteDebugLog("ERROR GET HIGHLIGHT FROM JSON "+task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteDebugLog(e);
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void getHighlightFromQR(String doc_id, MutableLiveData<String> scannedValue) {
        try{
            db.collection(FirebaseConstants.MEMORE).document(doc_id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        Memore memore = task.getResult().toObject(Memore.class);
                        if(memore!=null){
                            ErrorLog.WriteDebugLog("Memore found");

                            scannedValue.setValue(memore.getMemore_id());
                            memoreFound.setValue(true);
                        }else{
                            ErrorLog.WriteDebugLog("Memore not found");
                            memoreFound.setValue(false);
                        }
                    }else{
                        ErrorLog.WriteDebugLog("ERROR GET HIGHLIGHT FROM JSON "+task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteDebugLog(e);
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<Boolean> getMemoreFound(){
        return memoreFound;
    }

    public MutableLiveData<JSONObject> getScannedJSONObject(){
        return scannedJSONObject;
    }

    public MutableLiveData<Boolean> getOldMemoreQRFound(){return oldMemoreQRFound;}
}
