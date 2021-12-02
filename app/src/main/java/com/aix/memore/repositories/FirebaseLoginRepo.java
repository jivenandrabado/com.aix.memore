package com.aix.memore.repositories;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.UserInfo;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.SigninENUM;
import com.aix.memore.utilities.ToastUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.security.auth.callback.Callback;

public class FirebaseLoginRepo {

    public MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> isUserLoggedIn = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<Boolean> isOTPsent = new MutableLiveData<>();
    public MutableLiveData<String> OTPverificationID= new MutableLiveData<>();

    private final GoogleSignInOptions gso = new GoogleSignInOptions.
            Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
            build();

    private final FirebaseAuth mAuth;
    private final ToastUtil toastUtil;
    private final FirebaseRegistrationRepo firebaseRegistrationRepo;

    public FirebaseLoginRepo() {
        mAuth = FirebaseAuth.getInstance();
        toastUtil = new ToastUtil();
        firebaseRegistrationRepo = new FirebaseRegistrationRepo();
    }

    public void loginUserUsernamePassword(String email, String password) {
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ErrorLog.WriteDebugLog("signInWithEmail:success");
                            } else {
                                errorMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                                isUserLoggedIn.setValue(false);
                            }
                        }
                    });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void resetPassword(String email){
        try {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ErrorLog.WriteDebugLog("Email Sent");
                                resetPasswordSuccess.setValue(task.isSuccessful());
                                resetPasswordSuccess.setValue(false);
                            }else{
                                ErrorLog.WriteDebugLog(task.getException());
                                errorMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }

                    });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public MutableLiveData<Boolean> getResetPasswordSuccess(){
        return resetPasswordSuccess;
    }

    public void logoutUser(Activity activity){
        try {
            FirebaseAuth.getInstance().signOut();
            logoutGoogleAccount(activity);
//            LoginManager.getInstance().logOut();
            toastUtil.toastLogoutSuccess(activity);
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void logoutGoogleAccount(Activity activity){
        try {
            GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(activity,gso);

            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        ErrorLog.WriteDebugLog("Google Account Logged out");
                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public FirebaseAuth.AuthStateListener initFirebaseAuthListener(){

        try {
            return new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        ErrorLog.WriteDebugLog("User logged in");
                        isUserLoggedIn.setValue(true);
                    } else {
                        ErrorLog.WriteDebugLog("User logged out");
                        isUserLoggedIn.setValue(false);
                    }
                }
            };
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

        return null;

    }

    public MutableLiveData<Boolean> isUserLoggedIn(){
        return isUserLoggedIn;
    }


    public void loginWithGoogle(String idToken, UserInfo userInfo){

        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                ErrorLog.WriteDebugLog("signInWithCredential:success");
                                userInfo.setUser_id(Objects.requireNonNull(task.getResult().getUser()).getUid());
                                userInfo.setEmail(task.getResult().getUser().getEmail());
                                userInfo.setMobile_no(task.getResult().getUser().getPhoneNumber());

                                firebaseRegistrationRepo.checkUserExist(userInfo, SigninENUM.GOOGLE);
                                isUserLoggedIn.setValue(true);


                            } else {
                                ErrorLog.WriteErrorLog(task.getException());
                                errorMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                                isUserLoggedIn.setValue(false);


                            }
                        }
                    });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void loginAsGuest(){
        try {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ErrorLog.WriteDebugLog("signInAnonymously:success");
                            } else {
                                // If sign in fails, display a message to the user.
                                ErrorLog.WriteDebugLog("signInAnonymously:failure");
                                errorMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<String> getErrorMessage(){
        return errorMessage;
    }

    public void loginWithMobile(String mobile_no, FragmentActivity fragmentActivity) {

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential,fragmentActivity);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                ErrorLog.WriteDebugLog("OTP Code FAILED");

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    ErrorLog.WriteDebugLog("INVALID OTP Request");

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    ErrorLog.WriteDebugLog("Firebase quota exceeded");

                }

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                String mVerificationId = verificationId;
                PhoneAuthProvider.ForceResendingToken mResendToken = token;
                isOTPsent.setValue(true);
                OTPverificationID.setValue(verificationId);
                ErrorLog.WriteDebugLog("OTP Code Sent");
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(mobile_no)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(fragmentActivity)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);



    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential, FragmentActivity fragmentActivity) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(fragmentActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            ErrorLog.WriteDebugLog("signInWithCredential:success");

                        } else {
                            // Sign in failed, display a message and update the UI
                            ErrorLog.WriteDebugLog("signInWithCredential:failure");

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                ErrorLog.WriteErrorLog(task.getException());
                            }
                        }
                    }
                });
    }

    public MutableLiveData<String> getOTPverificationID(){
        return OTPverificationID;
    }
}

