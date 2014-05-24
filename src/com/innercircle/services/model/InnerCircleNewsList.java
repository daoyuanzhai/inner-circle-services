package com.innercircle.services.model;

import java.util.List;

public class InnerCircleNewsList implements ResponseData {
    private String uid;
    private List<InnerCircleNews> newsList;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return uid;
    }

    public void setNewsList(List<InnerCircleNews> newsList) {
        this.newsList = newsList;
    }

    public List<InnerCircleNews> getNewsList() {
        return this.newsList;
    }
}
