package com.aix.memore.repositories;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.AppConfig;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class AppConfigRepo {

    private final FirebaseFirestore db;
    private MutableLiveData<Boolean> isForceUpdate = new MutableLiveData<>();
    private ListenerRegistration appConfigRegistratin;
    public AppConfigRepo() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void initAppVersionControl(Context context){
        try {

            appConfigRegistratin = db.collection(FirebaseConstants.MEMORE_APP_CONFIG).document(FirebaseConstants.MEMORE_VERSION_CONTROL)
                    .addSnapshotListener((value, error) -> {
                        AppConfig appConfig = null;
//                        if (value != null) {
                            appConfig = value.toObject(AppConfig.class);
                            if(appConfig.getForce_update()) {
                                if (appConfig.getVersion_code() != getAppVersion(context)) {
                                    isForceUpdate.postValue(true);
                                }else{
                                    isForceUpdate.postValue(false);
                                }
                            }else if(!appConfig.getForce_update()){
                                isForceUpdate.postValue(false);
                            }
//                        }else{
//                            ErrorLog.WriteDebugLog("ERROR APP CONFIG");
//                        }

                    });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }


    public void detachAppConfigDocumentListener(){
        if(appConfigRegistratin!=null){
            ErrorLog.WriteDebugLog("APP CONFIG SNAPSHOT REMOVED");
            appConfigRegistratin.remove();
        }
    }

    private int getAppVersion(Context context){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            ErrorLog.WriteErrorLog(e);
        }

        return 0;

    }

    public MutableLiveData<Boolean> getIsForceUpdate(){
        return isForceUpdate;
    }
}
