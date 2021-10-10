package com.aix.memore.views.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.models.Gallery;

public class GalleryFirebaseAdapter extends ListAdapter<Gallery, GalleryFirebaseAdapter.ViewHolder> {

    protected GalleryFirebaseAdapter() {
        super(Gallery.itemCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryFirebaseAdapter.ViewHolder holder, int position) {

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
