package com.aix.memore.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.databinding.ItemAlbumBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.models.Album;
import com.aix.memore.utilities.ErrorLog;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

public class AlbumFirebaseAdapter extends FirestoreRecyclerAdapter<Album, AlbumFirebaseAdapter.ViewHolder> {

    private GalleryInterface galleryInterface;
    private static Boolean isEdit = false;
    private List<Album> albumList;
    public AlbumFirebaseAdapter(@NonNull FirestoreRecyclerOptions<Album> options, GalleryInterface galleryInterface) {
        super(options);
        this.galleryInterface = galleryInterface;
        albumList = new ArrayList<>();

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
                    holder.binding.checkboxSelector.toggle();
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

        holder.binding.checkboxSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    albumList.add(album);
                }else{
                    albumList.remove(album);
                }

                galleryInterface.onAlbumSelectDelete(albumList);
            }
        });
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
