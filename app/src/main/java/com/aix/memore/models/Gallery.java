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
    public String media_id;
    public Boolean is_deleted;
    public Long byteSize;

    public Long getByteSize() {
        return byteSize;
    }

    public void setByteSize(Long byteSize) {
        this.byteSize = byteSize;
    }

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

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }
}
