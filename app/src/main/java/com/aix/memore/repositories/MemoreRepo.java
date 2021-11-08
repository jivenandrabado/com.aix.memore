package com.aix.memore.repositories;

import android.net.Uri;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MemoreRepo {

    private FirebaseFirestore db;
    private MutableLiveData<String> highlightId = new MutableLiveData<>();
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private GalleryRepo galleryRepo;

    public MemoreRepo() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        galleryRepo = new GalleryRepo();
    }

    public void createMemore(Memore memore,String owner_id){
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
                        galleryRepo.createNewAlbum(memore.getMemore_id(),album,null);

                        //upload hightlight
                        uploadHighlightToFirebaseStorage(memore.getMemore_id(), Uri.parse(memore.getVideo_highlight()));

                        //upload profile pic
                        if(memore.bio_profile_pic != null) {
                            if (!memore.getBio_profile_pic().isEmpty()){
                                uploadBioProfilePicToFirebaseStorage(memore.getMemore_id(), Uri.parse(memore.getBio_profile_pic()));
                            }
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

    private void uploadBioProfilePicToFirebaseStorage(String memore_id, Uri path) {
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

    public void uploadHighlightToFirebaseStorage(String memore_id, Uri path) {
        try {
            StorageReference mediaRef = storageRef.child(memore_id + "/video" + System.currentTimeMillis());

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
                            updateHighlightURL(memore_id, String.valueOf(uri));
                        }
                    });
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
}
