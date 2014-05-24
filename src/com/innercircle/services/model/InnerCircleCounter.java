package com.innercircle.services.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleCounter implements ResponseData {
    private String uid;
    private String receiverUid;
    private int count;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return uid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getReceiverUid() {
        return this.receiverUid;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
