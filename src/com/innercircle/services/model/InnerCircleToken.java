package com.innercircle.services.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleToken implements InnerCircleData {
    @Id
    private String uid;
    private String accessToken;
    private String refreshToken;
    private long timestamp;

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
