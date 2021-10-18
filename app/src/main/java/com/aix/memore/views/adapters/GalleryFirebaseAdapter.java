package com.aix.memore.views.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.aix.memore.R;
import com.aix.memore.databinding.ItemAlbumBinding;
import com.aix.memore.databinding.ItemGalleryBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.interfaces.GalleryViewInterface;
import com.aix.memore.models.Album;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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
//        Glide.with(context).load(Uri.parse(gallery.getPath()))
//                .apply(new RequestOptions().override(500, 400))
//                .fitCenter()
//                .error(R.drawable.ic_baseline_photo_24).into((holder.binding.imageView));

        galleryList.add(gallery);

        holder.binding.setGalleryViewInterface(galleryViewInterface);
        holder.binding.setList(galleryList);
        holder.binding.setPosition(position);

        initGlideWithImage(gallery.getPath(),holder.binding.imageView);
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


    private void initGlideWithImage(String gallery_path, ImageView imageView){
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(circularProgressDrawable);
        requestOptions.error(R.drawable.ic_baseline_photo_24);
        requestOptions.skipMemoryCache(true);
        requestOptions.fitCenter();
        requestOptions.override(500,400);

//
//        Glide.load(imagePath) //passing your url to load image.
//                .load(imagePath)
//                .apply(requestOptions) // here you have all options you need
//                 // when image (url) will be loaded by glide then this face in animation help to replace url image in the place of placeHolder (default) image.

        Glide.with(context).load(Uri.parse(gallery_path))
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .fitCenter()
                .error(R.drawable.ic_baseline_photo_24).into(imageView);
    }
}
