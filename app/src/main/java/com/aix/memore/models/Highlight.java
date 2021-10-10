package com.aix.memore.models;

public class Highlight {

    public String owner_id;
    public String path;
    public String video_highlight;

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVideo_highlight() {
        return video_highlight;
    }

    public void setVideo_highlight(String video_highlight) {
        this.video_highlight = video_highlight;
    }

    @Override
    public String toString() {
        return "Highlight{" +
                "owner_id='" + owner_id + '\'' +
                ", path='" + path + '\'' +
                ", video_highlight='" + video_highlight + '\'' +
                '}';
    }
}
