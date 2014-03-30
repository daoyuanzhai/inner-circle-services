package com.innercircle.services.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleTalk implements InnerCircleData {
    private String uid;
    private String receiverUid;
    private int talkType;
    private String talkBody;
    private boolean isReceived;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return this.uid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getReceiverUid() {
        return this.receiverUid;
    }

    public void setTalkType(int talkType) {
        this.talkType = talkType;
    }

    public int getTalkType() {
        return this.talkType;
    }

    public void setTalkBody(String talkBody) {
        this.talkBody = talkBody;
    }

    public String getTalkBody() {
        return this.talkBody;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }

    public boolean getIsReceived() {
        return this.isReceived;
    }
}
