package com.aix.memore.views.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aix.memore.R;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class GalleryFullViewAdapter  extends BaseAdapter {
    private Context context;
    private List<Gallery> galleryList;

    public GalleryFullViewAdapter(Context c, List<Gallery> galleryList) {
        context = c;
        this.galleryList = galleryList;
    }

    // returns the number of images, in our example it is 10
    public int getCount() {
        return galleryList.size();
    }

    // returns the Item  of an item, i.e. for our example we can get the image
    public Object getItem(int position) {
        return position;
    }

    // returns the ID of an item
    public long getItemId(int position) {
        return position;
    }

    // returns an ImageView view
    public View getView(int position, View convertView, ViewGroup parent) {
        // position argument will indicate the location of image
        // create a ImageView programmatically
        Gallery gallery = galleryList.get(position);
        RelativeLayout imageContainer = new RelativeLayout(context);
        ImageView imageView = new ImageView(context);
        imageContainer.addView(imageView);

        // set image in ImageView
//        imageView.setImageURI(Uri.parse(galleryList.get(position).getPath()));

        Glide.with(context).load(Uri.parse(gallery.getPath()))
                .apply(new RequestOptions().override(400, 300))
                .fitCenter()
                .error(R.drawable.ic_baseline_photo_24)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if(gallery.getType() == 0) addPlayButton(imageContainer);
                        return false;
                    }
                })
                .into((imageView));

        // set ImageView param
//        imageView.setLayoutParams(new android.widget.Gallery.LayoutParams());
        return imageContainer;
    }

    private void addPlayButton(RelativeLayout imageContainer){
        ImageView playButtonImage = new ImageView(context);
        RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(100, 100);
        playButtonParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        playButtonImage.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_circle_outline_24));
        playButtonImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.mshop_base_color));
        playButtonImage.setLayoutParams(playButtonParams);
        imageContainer.addView(playButtonImage);
    }
}
