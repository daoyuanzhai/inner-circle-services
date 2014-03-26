package com.innercircle.services.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.innercircle.services.Constants;

@Document
public class InnerCircleRelation implements InnerCircleData {
    private String uid;
    private String theOtherUid;
    private boolean isFollowing;
    private boolean isBlocked;

    public void setUid(final String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return this.uid;
    }

    public void setTheOtherUid(final String theOtherUid) {
        this.theOtherUid = theOtherUid;
    }

    public String getTheOtherUid() {
        return this.theOtherUid;
    }

    public void setIsFollowing(final boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

    public boolean getIsFollowing() {
        return this.isFollowing;
    }

    public void setIsBlocked(final boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public boolean getIsBlocked() {
        return this.isBlocked;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{" + Constants.UID + ": " + this.uid + "; ");
        stringBuilder.append(Constants.THE_OTHER_UID + ": " + this.theOtherUid + "; ");
        stringBuilder.append(Constants.IS_FOLLOWING + ": " + String.valueOf(this.isFollowing) + "; ");
        stringBuilder.append(Constants.IS_BLOCKED + ": " + String.valueOf(this.isBlocked) + "}");
        return stringBuilder.toString();
    }
}
