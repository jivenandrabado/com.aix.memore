package com.aix.memore.repositories;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.Album;
import com.aix.memore.models.Memore;
import com.aix.memore.models.Gallery;
import com.aix.memore.models.Media;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GalleryRepo {

    private final FirebaseFirestore db;
    private CollectionReference collectionReference;
    private MutableLiveData<List<Media>> mediaListLiveData = new MutableLiveData<>();
    private ListenerRegistration mediaSnapshotListener;
    private ListenerRegistration bioSnapshotListener;
    private MutableLiveData<Memore> bioMutableLiveData = new MutableLiveData<Memore>();
    private MutableLiveData<Boolean> isAlbumCreated = new MutableLiveData<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private MutableLiveData<Boolean> isUploaded = new MutableLiveData<>();
    public String public_wall_id = "";
    public MutableLiveData<Boolean> isDeleted = new MutableLiveData<>();
    public MutableLiveData<Boolean> isImageDeleted = new MutableLiveData<>();



    public GalleryRepo() {
        this.db = FirebaseFirestore.getInstance();
        this.collectionReference = db.collection(FirebaseConstants.MEMORE);
    }

    public FirestoreRecyclerOptions<Album> recyclerOptions(String owner_id) {
        Query query = collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                .orderBy("date_created");
        return new FirestoreRecyclerOptions.Builder<Album>()
                .setQuery(query, Album.class)
                .build();
    }


    public void getDefaultGallery(String owner_id) {
        try{
            collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).whereEqualTo("is_default",true)
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                getSubCollectionDefaultGallery(owner_id,document.getId());
                            }
                        }
                    }

                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void getSubCollectionDefaultGallery(String owner_id,String doc_id){
        try{
            List<Media> mediaList = new ArrayList<>();
            mediaSnapshotListener = collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .document(doc_id).collection(FirebaseConstants.MEMORE_MEDIA).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Media media = dc.getDocument().toObject(Media.class);
                                    mediaList.add(media);
                                    break;

                                case REMOVED:
                                    ErrorLog.WriteDebugLog("REMOVE MEDIA");
                                    media = dc.getDocument().toObject(Media.class);
                                    if(mediaListLiveData != null) {
                                        if (!mediaListLiveData.getValue().isEmpty()) {
                                            Objects.requireNonNull(mediaListLiveData.getValue()).remove(media);
                                        }
                                    }
                                    break;

//                                case MODIFIED:
//                                    media = dc.getDocument().toObject(Media.class);
//                                    Objects.requireNonNull(mediaListLiveData.getValue()).equals(media);
//                                    break;

                            }
                        }

                        if (mediaListLiveData != null) {
                            mediaListLiveData.postValue(mediaList);
                        }
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public MutableLiveData<List<Media>> getDefaultGalleryMedia(){
        return mediaListLiveData;
    }

    public void detachMediaSnapshotListener(){
        if(mediaSnapshotListener!=null){
            ErrorLog.WriteDebugLog("MEDIA SNAPSHOT REMOVED");
            mediaSnapshotListener.remove();
        }
    }

    public void addSnapshotListenerForBio(String doc_id) {

        try{

            bioSnapshotListener = db.collection(FirebaseConstants.MEMORE).document(doc_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        ErrorLog.WriteErrorLog(error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        bioMutableLiveData.postValue(value.toObject(Memore.class));
                    }else{
                        ErrorLog.WriteDebugLog("NO VALUE");
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteDebugLog(e);
        }
    }

    public void detachBioSnapshotListener(){
        if(bioSnapshotListener!=null){
            ErrorLog.WriteDebugLog("BIO SNAPSHOT REMOVED");
            bioSnapshotListener.remove();
        }
    }

    public MutableLiveData<Memore> getBio(){
        return bioMutableLiveData;
    }

    public void createNewAlbum(String doc_id, Album album, List<Uri> imageUriList) {
        try{
            DocumentReference document = db.collection(FirebaseConstants.MEMORE).document(doc_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .document();

            album.setAlbum_id(document.getId());
            db.collection(FirebaseConstants.MEMORE).document(doc_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .document(document.getId()).set(album)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //public wall
                                if(imageUriList != null) {
                                    for (int i = 0; i < imageUriList.size(); i++) {
                                        uploadToFirebaseStorage(doc_id, imageUriList.get(i), document.getId());
                                    }
                                }

                                isAlbumCreated.postValue(true);
                            }else{
                                isAlbumCreated.postValue(false);
                            }
                        }
                    });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<Boolean> isAlbumCreated() {
        return isAlbumCreated;
    }

    public FirestoreRecyclerOptions galleryViewRecyclerOptions(String owner_id, String album_id) {
        Query query = collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).document(album_id).collection(FirebaseConstants.MEMORE_MEDIA)
                .whereEqualTo("is_deleted", false)
                .orderBy("upload_date");
        return new FirestoreRecyclerOptions.Builder<Gallery>()
                .setQuery(query, Gallery.class)
                .build();
    }

    public void uploadToFirebaseStorage(String owner_id, Uri path, String album_id) {
        try {
            StorageReference mediaRef = storageRef.child(owner_id + "/image" + System.currentTimeMillis());
//            InputStream stream = new FileInputStream(new File(path));

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
                            addNewMedia(owner_id, String.valueOf(uri), album_id);
                        }
                    });
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void addNewMedia(String owner_id, String path, String album_id){
        try{
            Gallery gallery = new Gallery();
            gallery.setType(1);
            gallery.setUpload_date(new Timestamp(new Date()));
            gallery.setPath(path);
            gallery.setIs_deleted(false);

            String doc_id = db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .document(album_id).collection(FirebaseConstants.MEMORE_MEDIA).document().getId();

            gallery.setMedia_id(doc_id);

            db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).document(album_id)
                    .collection(FirebaseConstants.MEMORE_MEDIA).document(doc_id).set(gallery).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        ErrorLog.WriteDebugLog("UPLOAD SUCCESS");
                        isUploaded.postValue(true);
                    }else{
                        ErrorLog.WriteErrorLog(task.getException());
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void uploadToFirebaseStoragePublicWall(String owner_id, Uri path) {
        try {
            StorageReference mediaRef = storageRef.child(owner_id + "/" + path.getLastPathSegment());
//            InputStream stream = new FileInputStream(new File(path));

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
                            addNewMediaToPublicWall(owner_id, String.valueOf(uri));
                        }
                    });
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void addNewMediaToPublicWall(String owner_id, String path){
        try{

            Gallery gallery = new Gallery();
            gallery.setType(1);
            gallery.setUpload_date(new Timestamp(new Date()));
            gallery.setPath(path);
            gallery.setIs_deleted(false);

            db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .whereEqualTo("title","Public Wall").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        public_wall_id = document.getId();
                                        ErrorLog.WriteDebugLog("Public wall id "+public_wall_id);
                                    }

                                    String doc_id = db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                                            .document(public_wall_id).collection(FirebaseConstants.MEMORE_MEDIA).document().getId();

                                    gallery.setMedia_id(doc_id);

                                    db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).document(public_wall_id)
                                            .collection(FirebaseConstants.MEMORE_MEDIA).document(doc_id).set(gallery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                ErrorLog.WriteDebugLog("UPLOAD SUCCESS");
                                                isUploaded.postValue(true);
                                            }else{
                                                ErrorLog.WriteErrorLog(task.getException());
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });




        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<Boolean> getIsUploaded(){
        return isUploaded;
    }

    public void deleteAlbums(List<Album> albumList, String owner_id) {
        try{


            WriteBatch writeBatch = db.batch();

            for (int i=0;i<albumList.size();i++){
                DocumentReference documentReference = db.collection(FirebaseConstants.MEMORE).document(owner_id)
                        .collection(FirebaseConstants.MEMORE_ALBUM).document(albumList.get(i).getAlbum_id());
                writeBatch.delete(documentReference);
            }

            writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Do anything here
                    isDeleted.postValue(true);
                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public MutableLiveData<Boolean> getIsDeleted(){
        return isDeleted;
    }

    public MutableLiveData<Boolean> getIsImageDeleted(){
        return isImageDeleted;
    }


    public void deleteImage(List<Gallery> galleryList, String owner_id, String album_id){
        WriteBatch writeBatch = db.batch();

        Map<String, Object> gallery = new HashMap<>();
        gallery.put("is_deleted", true);

        for (int i=0;i<galleryList.size();i++){
            DocumentReference documentReference = db.collection(FirebaseConstants.MEMORE).document(owner_id)
                    .collection(FirebaseConstants.MEMORE_ALBUM).document(album_id)
                    .collection(FirebaseConstants.MEMORE_MEDIA)
                    .document(galleryList.get(i).getMedia_id());
//            writeBatch.delete(documentReference);
            writeBatch.update(documentReference, gallery);
        }

        writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Do anything here
                isImageDeleted.postValue(true);
            }
        });


    }
    public void deleteImageStorage(Gallery gallery) {
        StorageReference storageReference = storage.getReferenceFromUrl(gallery.getPath());

        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ErrorLog.WriteDebugLog("DELETED IMAGE");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ErrorLog.WriteErrorLog(e);
            }
        });

    }
}
