package com.aix.memore.views.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

public class GalleryFirebaseAdapter extends FirestoreRecyclerAdapter<Gallery, GalleryFirebaseAdapter.ViewHolder> {

    private Context context;
    private List<Gallery> galleryList = new ArrayList<>();
    private GalleryViewInterface galleryViewInterface;
    private Boolean isDelete = false;
    private List<Gallery> galleryListDelete = new ArrayList<>();
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

        holder.binding.setGalleryViewInterface(galleryViewInterface);
        holder.binding.setList(galleryList);
        holder.binding.setPosition(position);
        holder.gallery = gallery;

        initGlideWithImage(gallery.getPath(),holder.binding.imageView,holder.binding.progressBarGallery);

        //filter for gallery view
        if(!gallery.getIs_deleted()){
            galleryList.add(gallery);
        }

        if(isDelete){
            holder.binding.checkboxSelector.setVisibility(View.VISIBLE);
            holder.binding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.binding.checkboxSelector.toggle();
                }
            });


        }else{
            holder.binding.checkboxSelector.setVisibility(View.INVISIBLE);
            holder.binding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        galleryViewInterface.onImageClick(galleryList,holder.getAbsoluteAdapterPosition());
                }
            });
        }

        holder.binding.checkboxSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b) {
                    galleryListDelete.add(gallery);
                    ErrorLog.WriteDebugLog("GALLERY LIST SIZE " + galleryListDelete.size());
                }else{
                    galleryListDelete.remove(gallery);
                }
                galleryViewInterface.onImageDelete(galleryListDelete);
            }
        });
    }

    @NonNull
    @Override
    public GalleryFirebaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemGalleryBinding binding = ItemGalleryBinding.inflate(layoutInflater,parent,false);
        return new GalleryFirebaseAdapter.ViewHolder(binding);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemGalleryBinding binding;
        Gallery gallery;
        public ViewHolder(ItemGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;



        }
    }


    private void initGlideWithImage(String gallery_path, ImageView imageView, ProgressBar progressBar){
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(context).load(Uri.parse(gallery_path))
//                .apply(new RequestOptions().override(500, 400))
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
//                .placeholder(circularProgressDrawable)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.ic_baseline_photo_24).into(imageView);
    }

    public void setDelete(Boolean isDelete){
        this.isDelete = isDelete;
        notifyDataSetChanged();
    }
}
