package com.aix.memore.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Album;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Media;
import com.aix.memore.repositories.GalleryRepo;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

public class GalleryViewModel extends ViewModel {
    private GalleryRepo galleryRepo = new GalleryRepo();

    public FirestoreRecyclerOptions getGalleryRecyclerOptions(String owner_id){
        return galleryRepo.recyclerOptions(owner_id);
    }

    public void getDefaultGallery(String owner_id){
        galleryRepo.getDefaultGallery(owner_id);
    }

    public void detachMediaSnapshotListener(){
        galleryRepo.detachMediaSnapshotListener();
    }

    public void detachBioSnapshotListener(){
        galleryRepo.detachBioSnapshotListener();
    }

    public MutableLiveData<List<Media>> getDefaultGalleryMedia(){
        return galleryRepo.getDefaultGalleryMedia();
    }

    public MutableLiveData<Bio> getBio(){
        return galleryRepo.getBio();
    }

    public void addSnapshotListenerForBio(String doc_id){
        galleryRepo.addSnapshotListenerForBio(doc_id);
    }
}
