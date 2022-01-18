package com.aix.memore.interfaces;

import com.aix.memore.models.Gallery;

import java.util.List;

public interface GalleryViewInterface {

    void onMediaClicked(List<Gallery> galleryList, int position);
    void onMediaSelected(List<Gallery> gallery);
    void setOpenSelector(boolean isSelected);
}
