package com.aix.memore.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StyleableRes;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.Album;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MemoreRepo extends GalleryRepo {

    private FirebaseFirestore db;
    private MutableLiveData<String> highlightId = new MutableLiveData<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference().child("Memore");
    public MutableLiveData<Boolean> memoreSaved = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<Double> uploadProgresMemore = new MutableLiveData<>();

    public MemoreRepo() {
        db = FirebaseFirestore.getInstance();
    }

    public void createMemore(Memore memore, String owner_id, Bitmap qrBitmap, Context context){
        try{

            memore.setOwner_id(owner_id);
            db.collection(FirebaseConstants.MEMORE).document(memore.getMemore_id()).set(memore).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ErrorLog.WriteDebugLog("SAVED MEMORE "+memore.getPassword());
                        //create public wall
                        Album album = new Album();
                        album.setTitle("Public Wall");
                        album.setDescription("");
                        album.setDate_created(new Date());
                        album.setIs_public(true);
                        album.setIs_default(true);

                        createNewAlbum(memore.getMemore_id(),album,null);

                        //upload hightlight
//                        uploadHighlightToFirebaseStorage(memore.getMemore_id(), Uri.parse(memore.getVideo_highlight()));

                        //upload profile pic
                        if(memore.bio_profile_pic != null) {
                            if (!memore.getBio_profile_pic().isEmpty()){
                                uploadBioProfilePicToFirebaseStorage(memore.getMemore_id(), Uri.parse(memore.getBio_profile_pic()));
                            }
                        }

                        //create vault
                        //create public wall
                        Album album1 = new Album();
                        album1.setTitle("Vault");
                        album1.setDescription("");
                        album1.setDate_created(new Date());
                        album1.setIs_public(false);
                        album1.setIs_default(true);
                        createVault(memore.getMemore_id(),album1,qrBitmap,context);

                        memoreSaved.setValue(true);


                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void uploadBioProfilePicToFirebaseStorage(String memore_id, Uri path) {
        try {
            StorageReference mediaRef = storageRef.child(memore_id + "/image" + System.currentTimeMillis());

            UploadTask uploadTask = mediaRef.putFile(path);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ErrorLog.WriteDebugLog("FAILED TO UPLOAD " + e);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mediaRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ErrorLog.WriteDebugLog("SUCCESS UPLOAD " + uri);
                            updateBioProfilePictURL(memore_id, String.valueOf(uri));
                        }
                    });
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }


    }

    public void createCreateHightlightId(){
        String doc_id = db.collection(FirebaseConstants.MEMORE).document().getId();
        highlightId.setValue(doc_id);
    }

    public MutableLiveData<String> getHighlightId() {
        return highlightId;

    }

    public void uploadHighlightToFirebaseStorage(Memore memore,Bitmap qrBitmap , Context context) {
        try {

            StorageReference mediaRef = storageRef.child(memore.getMemore_id() + "/video" + System.currentTimeMillis());

            UploadTask uploadTask = mediaRef.putFile(Uri.parse(memore.getVideo_highlight()));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ErrorLog.WriteDebugLog("FAILED TO UPLOAD " + e);
                    Toast.makeText(context,"Failed to upload highlight. Please check your internet connection", Toast.LENGTH_LONG).show();
                    errorMessage.setValue(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mediaRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ErrorLog.WriteDebugLog("SUCCESS UPLOAD " + uri);
//                            updateHighlightURL(memore.getMemore_id(), String.valueOf(uri));
                            memore.setVideo_highlight(String.valueOf(uri));
                            createMemore(memore,"",qrBitmap,context);

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    ErrorLog.WriteDebugLog("UPLOAD PROGRESS "+progress);
                    uploadProgresMemore.setValue(progress);
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void updateHighlightToFirebaseStorage(Memore memore , Context context) {
        try {

            StorageReference mediaRef = storageRef.child(memore.getMemore_id() + "/video" + System.currentTimeMillis());

            UploadTask uploadTask = mediaRef.putFile(Uri.parse(memore.getVideo_highlight()));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ErrorLog.WriteDebugLog("FAILED TO UPLOAD " + e);
                    Toast.makeText(context,"Failed to upload highlight. Please check your internet connection", Toast.LENGTH_LONG).show();
                    errorMessage.setValue(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mediaRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ErrorLog.WriteDebugLog("SUCCESS UPLOAD " + uri);
//                            updateHighlightURL(memore.getMemore_id(), String.valueOf(uri));
                            memore.setVideo_highlight(String.valueOf(uri));
                            updateMemore(memore);

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    ErrorLog.WriteDebugLog("UPLOAD PROGRESS "+progress);
                    uploadProgresMemore.setValue(progress);
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void updateHighlightURL(String memore_id ,String path){
        try{
            Map<String,Object> memoreMap = new HashMap<>();
            memoreMap.put("video_highlight", path);

            db.collection(FirebaseConstants.MEMORE).document(memore_id).update(memoreMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ErrorLog.WriteDebugLog("VIDEO HIGHLIGHT PATH UPDATED");
                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void updateBioProfilePictURL(String memore_id ,String path){
        try{
            Map<String,Object> memoreMap = new HashMap<>();
            memoreMap.put("bio_profile_pic", path);

            db.collection(FirebaseConstants.MEMORE).document(memore_id).update(memoreMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ErrorLog.WriteDebugLog("Profile Pic PATH UPDATED");
                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<String> isQrCodeUploaded(){
       return isQRCodeUploaded;
    }

    public void updateMemore(Memore memore){
        try{
            db.collection(FirebaseConstants.MEMORE).document(memore.getMemore_id()).set(memore).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ErrorLog.WriteDebugLog("UPDATE MEMORE SUCCESS");
                        memoreSaved.setValue(true);
                    }else{
                        ErrorLog.WriteDebugLog("UPDATE MEMORE FAILED "+task.getException());
                        errorMessage.setValue(String.valueOf(task.getException()));
                    }

                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }
}
