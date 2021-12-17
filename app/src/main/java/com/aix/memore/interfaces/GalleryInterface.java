package com.aix.memore.interfaces;

import com.aix.memore.models.Album;

import java.util.List;

public interface GalleryInterface {

    void onAlbumSelect(Album album);
    void onAlbumSelectDelete(List<Album> albumList);
}
