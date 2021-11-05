package com.aix.memore.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.utilities.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepo {

    private final FirebaseFirestore db;
    private MutableLiveData<Boolean> isAuthorized = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserRepo() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void verifyPassword(String doc_id,String password){
        db.collection(FirebaseConstants.MEMORE).document(doc_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            String passwordFirebase  = (String) task.getResult().get("password");

                            if(password.equals(passwordFirebase)){
                                isAuthorized.postValue(true);
                            }else{
                                errorMessage.postValue("Password Incorrect");
                            }
                        }
                    }
                });
    }

    public MutableLiveData<Boolean> getIsAuthorized(){
        return isAuthorized;
    }
    public MutableLiveData<String> getErrorMessage(){
        return errorMessage;
    }

}
