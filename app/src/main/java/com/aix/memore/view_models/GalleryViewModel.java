package com.aix.memore.view_models;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aix.memore.models.Album;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Gallery;
import com.aix.memore.models.Media;
import com.aix.memore.repositories.GalleryRepo;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.net.URI;
import java.util.List;

public class GalleryViewModel extends ViewModel {
    private GalleryRepo galleryRepo = new GalleryRepo();
    private MutableLiveData<Album> selectedAlbum = new MutableLiveData<>();
    private MutableLiveData<List<Gallery>> galleryList = new MutableLiveData<>();
    private MutableLiveData<Integer> selectedMediaPosition = new MutableLiveData<>();

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

    public void createNewAlbum(String doc_id, Album album, List<Uri> imageUriList){
        galleryRepo.createNewAlbum(doc_id, album, imageUriList);
    }

    public MutableLiveData<Boolean> isAlbumCreated(){
        return galleryRepo.isAlbumCreated();
    }

    public MutableLiveData<Album> getSelectedAlbum(){return selectedAlbum;}

    public FirestoreRecyclerOptions getGalleryViewRecyclerOptions(String owner_id, String album_id){
        return galleryRepo.galleryViewRecyclerOptions(owner_id, album_id);
    }

    public void uploadToFirebaseStorage(String owner_id, Uri path, String album_id){
        galleryRepo.uploadToFirebaseStorage(owner_id, path,album_id);
    }

    public void uploadToFirebaseStorageToPublicWall(String owner_id, Uri path){
        galleryRepo.uploadToFirebaseStoragePublicWall(owner_id, path);
    }

    public MutableLiveData<Boolean> isUploaded(){
        return galleryRepo.getIsUploaded();
    }

    public MutableLiveData<List<Gallery>> getSelectedGalleryList() {
        return galleryList;
    }

    public MutableLiveData<Integer> getSelectedMediaPosition() {
        return selectedMediaPosition;
    }

    public void deleteAlbums(List<Album> albumList,String owner_id){
        galleryRepo.deleteAlbums(albumList, owner_id);
    }

    public MutableLiveData<Boolean> isDeleted(){
        return galleryRepo.getIsDeleted();
    }

    public void deleteImage(List<Gallery> galleryList, String owner_id,String album_id) {
        galleryRepo.deleteImage(galleryList, owner_id, album_id);
    }

    public MutableLiveData<Boolean> isImageDeleted(){
        return galleryRepo.getIsImageDeleted();
    }
}
