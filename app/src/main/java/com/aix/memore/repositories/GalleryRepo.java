package com.aix.memore.repositories;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.Album;
import com.aix.memore.models.Memore;
import com.aix.memore.models.Gallery;
import com.aix.memore.models.Media;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.aix.memore.utilities.GlobalFunctions;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    private ListenerRegistration albumMediaSnapshotListener;
    private MutableLiveData<List<Gallery>> galleryList = new MutableLiveData<>();
    private MutableLiveData<Memore> bioMutableLiveData = new MutableLiveData<Memore>();
    private MutableLiveData<Boolean> isAlbumCreated = new MutableLiveData<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference().child("Memore");
    private MutableLiveData<Boolean> isUploaded = new MutableLiveData<>();
    public String public_wall_id = "";
    public MutableLiveData<Boolean> isDeleted = new MutableLiveData<>();
    public MutableLiveData<Boolean> isImageDeleted = new MutableLiveData<>();
    public MutableLiveData<String> isQRCodeUploaded = new MutableLiveData<String>();
    public MutableLiveData<Double> uploadProgress = new MutableLiveData<>();



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

    public void createNewAlbum(String doc_id, Album album, List<Map.Entry<Uri,Integer>> imageUriList) {
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
                                        uploadToFirebaseStorage(doc_id, imageUriList.get(i).getKey(), document.getId(), imageUriList.get(i).getValue());
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

    public void createVault(String doc_id, Album album, Bitmap qrBitmap, Context context) {
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
                                if(qrBitmap != null) {
                                    //upload qr code bitmap
                                    uploadBitmapToFirebaseStorage(doc_id,qrBitmap, document.getId(), context, 1);
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

    public void uploadToFirebaseStorage(String owner_id, Uri path, String album_id, int mediaType) {
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
                            addNewMedia(owner_id, String.valueOf(uri), album_id, taskSnapshot.getTotalByteCount(), mediaType);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    ErrorLog.WriteDebugLog("UPLOAD PROGRESS "+progress);
                    uploadProgress.setValue(progress);

                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void uploadBitmapToFirebaseStorage(String owner_id, Bitmap qrBitmap, String album_id, Context context, int mediaType) {
        try {
            StorageReference mediaRef = storageRef.child(owner_id + "/image" + System.currentTimeMillis());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mediaRef.putBytes(data);
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
                            ErrorLog.WriteDebugLog("SUCCESS QR CODE UPLOAD " + uri);

                            downloadImageNew("image"+System.currentTimeMillis(), String.valueOf(uri),context);
                            addNewMedia(owner_id, String.valueOf(uri), album_id, taskSnapshot.getTotalByteCount(), mediaType);
                        }
                    });
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void uploadBitmapToVault(String owner_id, Bitmap qrBitmap, Context context) {
        try {
            StorageReference mediaRef = storageRef.child(owner_id + "/image" + System.currentTimeMillis());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mediaRef.putBytes(data);
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
                            ErrorLog.WriteDebugLog("SUCCESS QR CODE UPLOAD " + uri);

                            downloadImageNew("image"+System.currentTimeMillis(), String.valueOf(uri),context);
                            addNewMediaToVault(owner_id, String.valueOf(uri));
                        }
                    });
                }
            });
        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    public void addNewMedia(String owner_id, String path, String album_id, long byteSize, int mediaType){
        try{
            Gallery gallery = new Gallery();
            gallery.setType(mediaType);
            gallery.setUpload_date(new Timestamp(new Date()));
            gallery.setPath(path);
            gallery.setIs_deleted(false);
            gallery.setByteSize(byteSize);

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

    public void addNewMediaToVault(String owner_id, String path){
        try{

            Gallery gallery = new Gallery();
            gallery.setType(1);
            gallery.setUpload_date(new Timestamp(new Date()));
            gallery.setPath(path);
            gallery.setIs_deleted(false);

            db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                    .whereEqualTo("title","Vault").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                public_wall_id = document.getId();
                            }

                            String doc_id = db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                                    .document(public_wall_id).collection(FirebaseConstants.MEMORE_MEDIA).document().getId();

                            gallery.setMedia_id(doc_id);

                            db.collection(FirebaseConstants.MEMORE).document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).document(public_wall_id)
                                    .collection(FirebaseConstants.MEMORE_MEDIA).document(doc_id).set(gallery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        ErrorLog.WriteDebugLog("UPLOAD TO VAULT SUCCESS");
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
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    ErrorLog.WriteDebugLog("UPLOAD PROGRESS TO PUBLIC WALL "+progress);
                    uploadProgress.setValue(progress);

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

    private void downloadImageNew(String filename, String downloadUrlOfImage,Context context){
        try{
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            Toast.makeText(context, "Image download started.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(context, "Image download failed.", Toast.LENGTH_SHORT).show();
            ErrorLog.WriteDebugLog("DOWNLOAD ERROR "+e);
        }
    }


    public void attachAlbumDetailsListener(String owner_id,String doc_id) {
        List<Gallery> mediaList = new ArrayList<>();
        albumMediaSnapshotListener = collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM)
                .document(doc_id).collection(FirebaseConstants.MEMORE_MEDIA).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mediaList.clear();
                        for (DocumentSnapshot item : value.getDocuments()){
                            mediaList.add(item.toObject(Gallery.class));
                        }
                        galleryList.setValue(mediaList);
                    }
                });
    }

    public MutableLiveData<List<Gallery>> getGalleryList() {
        return galleryList;
    }

    public void detachAlbumDetailsListener() {
        albumMediaSnapshotListener.remove();
    }
}
