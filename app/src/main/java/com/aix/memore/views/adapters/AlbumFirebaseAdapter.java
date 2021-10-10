package com.aix.memore.views.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.databinding.ItemAlbumBinding;
import com.aix.memore.models.Album;
import com.aix.memore.utilities.ErrorLog;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AlbumFirebaseAdapter extends FirestoreRecyclerAdapter<Album, AlbumFirebaseAdapter.ViewHolder> {


    public AlbumFirebaseAdapter(@NonNull FirestoreRecyclerOptions<Album> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AlbumFirebaseAdapter.ViewHolder holder, int position, @NonNull Album model) {
        Album album = getItem(position);
        holder.binding.setAlbum(album);
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
}
