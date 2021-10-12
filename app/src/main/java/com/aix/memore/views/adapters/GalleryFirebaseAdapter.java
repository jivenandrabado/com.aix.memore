package com.aix.memore.views.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.R;
import com.aix.memore.databinding.ItemAlbumBinding;
import com.aix.memore.databinding.ItemGalleryBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.interfaces.GalleryViewInterface;
import com.aix.memore.models.Album;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

public class GalleryFirebaseAdapter extends FirestoreRecyclerAdapter<Gallery, GalleryFirebaseAdapter.ViewHolder> {

    private Context context;
    private List<Gallery> galleryList = new ArrayList<>();
    private GalleryViewInterface galleryViewInterface;
    public GalleryFirebaseAdapter(@NonNull FirestoreRecyclerOptions<Gallery> options, Context context, GalleryViewInterface galleryViewInterface) {
        super(options);
        this.context = context;
        this.galleryViewInterface = galleryViewInterface;
    }

    @Override
    protected void onBindViewHolder(@NonNull GalleryFirebaseAdapter.ViewHolder holder, int position, @NonNull Gallery gallery) {
        Glide.with(context).load(Uri.parse(gallery.getPath()))
                .apply(new RequestOptions().override(500, 400))
                .fitCenter()
                .error(R.drawable.ic_baseline_photo_24).into((holder.binding.imageView));

        galleryList.add(gallery);

        holder.binding.setGalleryViewInterface(galleryViewInterface);
        holder.binding.setList(galleryList);
        holder.binding.setPosition(position);
    }

    @NonNull
    @Override
    public GalleryFirebaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemGalleryBinding binding = ItemGalleryBinding.inflate(layoutInflater,parent,false);
        return new GalleryFirebaseAdapter.ViewHolder(binding);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemGalleryBinding binding;
        public ViewHolder(ItemGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
