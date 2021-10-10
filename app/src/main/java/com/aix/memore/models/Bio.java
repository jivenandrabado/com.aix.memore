package com.aix.memore.models;

import com.google.firebase.Timestamp;

public class Bio {

    public String bio_first_name;
    public String bio_middle_name;
    public String bio_last_name;
    public Timestamp bio_birth_date;
    public Timestamp bio_death_date;
    public String bio_profile_pic;


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

    public Timestamp getBio_birth_date() {
        return bio_birth_date;
    }

    public void setBio_birth_date(Timestamp bio_birth_date) {
        this.bio_birth_date = bio_birth_date;
    }

    public Timestamp getBio_death_date() {
        return bio_death_date;
    }

    public void setBio_death_date(Timestamp bio_death_date) {
        this.bio_death_date = bio_death_date;
    }

    public String getBio_profile_pic() {
        return bio_profile_pic;
    }

    public void setBio_profile_pic(String bio_profile_pic) {
        this.bio_profile_pic = bio_profile_pic;
    }
}
