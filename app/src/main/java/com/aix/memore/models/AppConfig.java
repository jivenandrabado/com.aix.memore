package com.aix.memore.models;

public class AppConfig {

    private Double version_code;
    private Boolean force_update;

    public Double getVersion_code() {
        return version_code;
    }

    public void setVersion_code(Double version_code) {
        this.version_code = version_code;
    }

    public Boolean getForce_update() {
        return force_update;
    }

    public void setForce_update(Boolean force_update) {
        this.force_update = force_update;
    }
}
