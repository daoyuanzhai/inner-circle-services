package com.innercircle.services.model;

import org.springframework.web.multipart.MultipartFile;

public class InnerCircleFileUpload {

    private String uid;
    private String accessToken;
    private String filename;
    private MultipartFile file;
    private String imageUsage;

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public MultipartFile getFile() {
        return this.file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getImageUsage() {
        return this.imageUsage;
    }

    public void setImageUsage(String imageUsage) {
        this.imageUsage = imageUsage;
    }
}
