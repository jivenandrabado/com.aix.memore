package com.aix.memore.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.Memore;
import com.aix.memore.models.UserInfo;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.aix.memore.utilities.SigninENUM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class FirebaseRegistrationRepo {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final FirebaseLoginRepo firebaseLoginRepo;
    private final MutableLiveData<Boolean> isRegistered = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MemoreRepo memoreRepo = new MemoreRepo();

    public FirebaseRegistrationRepo(FirebaseLoginRepo firebaseLoginRepo){
        this.firebaseLoginRepo = firebaseLoginRepo;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String password, UserInfo userInfo, Memore memore){
        try{
            mAuth.createUserWithEmailAndPassword(userInfo.getEmail(), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ErrorLog.WriteDebugLog("registration success");
                                ErrorLog.WriteDebugLog("Saving user info...");
                                String user_id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                userInfo.setUser_id(user_id);
                                ErrorLog.WriteDebugLog("OWNER ID = "+user_id);
                                saveRegistrationToUsers(userInfo,password, SigninENUM.NONE);
                                memore.setPassword(password);
                                memoreRepo.createMemore(memore, user_id);

                                isRegistered.setValue(true);

                            } else {
                                ErrorLog.WriteDebugLog("registration failed");
                                ErrorLog.WriteErrorLog(task.getException());
                                errorMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
        }catch (Exception e){
            errorMessage.setValue(e.toString());
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void saveRegistrationToUsers(UserInfo userInfo, String password, SigninENUM signinENUM){
        try {
            db.collection(FirebaseConstants.MEMORE_USERS).document(userInfo.getUser_id()).set(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        switch (signinENUM){
                            case NONE:
                                ErrorLog.WriteDebugLog("Logging in to heylo");
//                                firebaseLoginRepo.loginUserUsernamePassword(userInfo.getEmail(), password);
                                break;
                            case GOOGLE:
                                ErrorLog.WriteDebugLog("User info saved from google");
                                break;
                            case FACEBOOK:
                                ErrorLog.WriteDebugLog("User info saved from facebook");
                                break;
                        }
                    } else {
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void checkUserExist(UserInfo userInfo, SigninENUM signinENUM){
        try{

            db.collection(FirebaseConstants.MEMORE_USERS).document(userInfo.getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()){
                            ErrorLog.WriteDebugLog("user data exists");

                        }else{
                            ErrorLog.WriteDebugLog("user data does not exists");
                            saveRegistrationToUsers(userInfo,"",signinENUM);
                        }
                    }
                }
            });



        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<Boolean> getIsRegistered(){return isRegistered;}
    public MutableLiveData<String> getErrorMessage(){return errorMessage;}
}
