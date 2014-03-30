package com.innercircle.services.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleNews implements InnerCircleData {
    private String uid;
    private int newsIndex;
    private long pubTime;
    private int picCount;
    private String content;
    private boolean deleted;

    public void setUid(final String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return this.uid;
    }

    public void setNewsIndex(final int newsIndex) {
        this.newsIndex = newsIndex;
    }

    public int getNewsIndex() {
        return this.newsIndex;
    }

    public void setPubTime(final long pubTime) {
        this.pubTime = pubTime;
    }

    public long getPubTime() {
        return this.pubTime;
    }

    public void setPicCount(final int picCount) {
        this.picCount = picCount;
    }

    public int getPicCount() {
        return this.picCount;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public boolean getDeleted() {
        return this.deleted;
    }
}
