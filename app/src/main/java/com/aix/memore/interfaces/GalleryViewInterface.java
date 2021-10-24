package com.aix.memore.interfaces;

import com.aix.memore.models.Gallery;

import java.util.List;

public interface GalleryViewInterface {

    void onImageClick(List<Gallery> galleryList, int position);
    void onImageDelete(List<Gallery> gallery);
}
