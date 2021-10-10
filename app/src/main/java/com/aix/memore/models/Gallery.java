package com.aix.memore.models;

import androidx.annotation.NonNull;
import androidx.paging.DifferCallback;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

public class Gallery {

    public String path;
    public String type;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gallery gallery = (Gallery) o;
        return getPath().equals(gallery.getPath()) && getType().equals(gallery.getType());
    }

    public static DiffUtil.ItemCallback<Gallery> itemCallback = new DiffUtil.ItemCallback<Gallery>() {
        @Override
        public boolean areItemsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.getPath().equals(newItem.getPath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.equals(newItem);
        }
    };
}
