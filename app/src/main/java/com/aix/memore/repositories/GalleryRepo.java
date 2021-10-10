package com.aix.memore.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aix.memore.models.Album;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Highlight;
import com.aix.memore.models.Media;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GalleryRepo {

    private final FirebaseFirestore db;
    private CollectionReference collectionReference;
    private MutableLiveData<List<Media>> mediaListLiveData = new MutableLiveData<>();
    private ListenerRegistration mediaSnapshotListener;
    private ListenerRegistration bioSnapshotListener;
    private MutableLiveData<Bio> bioMutableLiveData = new MutableLiveData<Bio>();


    public GalleryRepo() {
        this.db = FirebaseFirestore.getInstance();
        this.collectionReference = db.collection(FirebaseConstants.MEMORE_OWNER);
    }

    public FirestoreRecyclerOptions<Album> recyclerOptions(String owner_id) {
        Query query = collectionReference.document(owner_id).collection(FirebaseConstants.MEMORE_ALBUM).whereEqualTo("is_default",false);
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

                        mediaListLiveData.setValue(mediaList);
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

            bioSnapshotListener = db.collection(FirebaseConstants.MEMORE_OWNER).document(doc_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        ErrorLog.WriteErrorLog(error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        bioMutableLiveData.setValue(value.toObject(Bio.class));
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

    public MutableLiveData<Bio> getBio(){
        return bioMutableLiveData;
    }

}
