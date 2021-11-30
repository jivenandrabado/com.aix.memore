package com.aix.memore.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.Date;

public class Memore {

    public String bio_first_name;
    public String bio_middle_name;
    public String bio_last_name;
    public Date bio_birth_date;
    public Date bio_death_date;
    public String bio_profile_pic ="";
    public String memore_id;
    public String video_highlight;
    public String owner_id;
    public String password;
    public Date date_created;
    public String address;
    public String address_name;
    public GeoPoint latLng;
    public String owner_email;
    public boolean is_video;

    public String getOwner_email() {
        return owner_email;
    }

    public void setOwner_email(String owner_email) {
        this.owner_email = owner_email;
    }

    public String getMemore_id() {
        return memore_id;
    }

    public String getVideo_highlight() {
        return video_highlight;
    }

    public void setVideo_highlight(String video_highlight) {
        this.video_highlight = video_highlight;
    }


    public String getBio_first_name() {
        return bio_first_name;
    }

    public void setBio_first_name(String bio_first_name) {
        this.bio_first_name = bio_first_name;
    }

    public String getBio_middle_name() {
        return bio_middle_name;
    }

    public void setBio_middle_name(String bio_middle_name) {
        this.bio_middle_name = bio_middle_name;
    }

    public String getBio_last_name() {
        return bio_last_name;
    }

    public void setBio_last_name(String bio_last_name) {
        this.bio_last_name = bio_last_name;
    }

    public Date getBio_birth_date() {
        return bio_birth_date;
    }

    public void setBio_birth_date(Date bio_birth_date) {
        this.bio_birth_date = bio_birth_date;
    }

    public Date getBio_death_date() {
        return bio_death_date;
    }

    public void setBio_death_date(Date bio_death_date) {
        this.bio_death_date = bio_death_date;
    }

    public String getBio_profile_pic() {
        return bio_profile_pic;
    }

    public void setBio_profile_pic(String bio_profile_pic) {
        this.bio_profile_pic = bio_profile_pic;
    }

    public void setMemore_id(String memore_id) {
        this.memore_id = memore_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }

    public String getAddress_name() {
        return address_name;
    }

    public void setAddress_name(String address_name) {
        this.address_name = address_name;
    }

    public boolean isIs_video() {
        return is_video;
    }

    public void setIs_video(boolean is_video) {
        this.is_video = is_video;
    }

    @Override
    public String toString() {
        return "Memore{" +
                "bio_first_name='" + bio_first_name + '\'' +
                ", bio_middle_name='" + bio_middle_name + '\'' +
                ", bio_last_name='" + bio_last_name + '\'' +
                ", bio_birth_date=" + bio_birth_date +
                ", bio_death_date=" + bio_death_date +
                ", bio_profile_pic='" + bio_profile_pic + '\'' +
                ", memore_id='" + memore_id + '\'' +
                ", video_highlight='" + video_highlight + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", password='" + password + '\'' +
                ", date_created=" + date_created +
                ", address='" + address + '\'' +
                ", latLng=" + latLng +
                '}';
    }
}
