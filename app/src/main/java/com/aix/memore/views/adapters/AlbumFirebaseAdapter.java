package com.aix.memore.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.databinding.ItemAlbumBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.models.Album;
import com.aix.memore.utilities.ErrorLog;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AlbumFirebaseAdapter extends FirestoreRecyclerAdapter<Album, AlbumFirebaseAdapter.ViewHolder> {

    private GalleryInterface galleryInterface;
    private static Boolean isEdit = false;
    public AlbumFirebaseAdapter(@NonNull FirestoreRecyclerOptions<Album> options, GalleryInterface galleryInterface) {
        super(options);
        this.galleryInterface = galleryInterface;
    }

    @Override
    protected void onBindViewHolder(@NonNull AlbumFirebaseAdapter.ViewHolder holder, int position, @NonNull Album model) {
        Album album = getItem(position);
        holder.binding.setAlbum(album);
        holder.binding.setGalleryInterface(galleryInterface);

        if(isEdit){
            holder.binding.checkboxSelector.setVisibility(View.VISIBLE);
            holder.binding.constraintLayoutParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.binding.checkboxSelector.setChecked(true);
                }
            });


        }else{
            holder.binding.checkboxSelector.setVisibility(View.INVISIBLE);
            holder.binding.constraintLayoutParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    galleryInterface.onAlbumSelect(album);
                }
            });
        }
    }

    @NonNull
    @Override
    public AlbumFirebaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemAlbumBinding binding = ItemAlbumBinding.inflate(layoutInflater,parent,false);
        return new ViewHolder(binding);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAlbumBinding binding;
        public ViewHolder(ItemAlbumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setEdit(Boolean isEdit){
        AlbumFirebaseAdapter.isEdit = isEdit;
        notifyDataSetChanged();
    }
}
