package io.github.ojauch;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

public class Datapackage {
    private String profile;

    @JsonProperty(value = "wacz_version")
    private String waczVersion;
    private List<Resource> resources;
    private String title;
    private String description;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private String software;
    private String mainPageUrl;
    private ZonedDateTime mainPageDate;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getWaczVersion() {
        return waczVersion;
    }

    public void setWaczVersion(String waczVersion) {
        this.waczVersion = waczVersion;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getMainPageUrl() {
        return mainPageUrl;
    }

    public void setMainPageUrl(String mainPageUrl) {
        this.mainPageUrl = mainPageUrl;
    }

    public ZonedDateTime getMainPageDate() {
        return mainPageDate;
    }

    public void setMainPageDate(ZonedDateTime mainPageDate) {
        this.mainPageDate = mainPageDate;
    }
}
