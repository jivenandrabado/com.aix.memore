package com.aix.memore.views.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aix.memore.R;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class GalleryFullViewAdapter  extends BaseAdapter {
    private Context context;
    private List<Gallery> galleryList;

    public GalleryFullViewAdapter(Context c, List<Gallery> galleryList) {
        context = c;
        this.galleryList = galleryList;
        ErrorLog.WriteDebugLog("GALLERY LIST IN ADAPTER "+galleryList.size());
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
        ImageView imageView = new ImageView(context);

        // set image in ImageView
//        imageView.setImageURI(Uri.parse(galleryList.get(position).getPath()));

        Glide.with(context).load(Uri.parse(galleryList.get(position).getPath()))
                .apply(new RequestOptions().override(400, 300))
                .fitCenter()
                .error(R.drawable.ic_baseline_photo_24).into((imageView));

        // set ImageView param
//        imageView.setLayoutParams(new android.widget.Gallery.LayoutParams());
        return imageView;
    }
}
