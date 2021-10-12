package com.aix.memore.models;

import androidx.annotation.NonNull;
import androidx.paging.DifferCallback;
import androidx.recyclerview.widget.DiffUtil;

import com.google.firebase.Timestamp;

import java.util.Objects;

public class Gallery {

    public String path;
    public int type;
    public Timestamp upload_date;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Timestamp getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(Timestamp upload_date) {
        this.upload_date = upload_date;
    }

}
