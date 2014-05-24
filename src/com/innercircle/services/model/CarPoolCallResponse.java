package com.innercircle.services.model;

public class CarPoolCallResponse {
    public enum Status {
        SUCCESS,
        FAILED,
        ERROR_IN_USE,
        ERROR_MISMATCH,
        ERROR_EXPIRED
    }

    private Status status;
    private ResponseData data;

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setData(final ResponseData data) {
        this.data = data;
    }

    public ResponseData getData() {
        return this.data;
    }
}
