package com.innercircle.services.model;

import java.util.List;

public class InnerCircleRelationList implements InnerCircleData {
    private String uid;
    private List<String> relationList;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return uid;
    }

    public void setRelationList(List<String> relationList) {
        this.relationList = relationList;
    }

    public List<String> getRelationList() {
        return this.relationList;
    }
}
