package com.innercircle.services;

public final class Constants {
    private Constants() {}

    // Http Parameters
    public final static String JSON_STRING = "jsonString";
    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";
    public final static String VIP_CODE = "VIPCode";

    public final static String UID = "uid";
    public final static String ACCESS_TOKEN = "accessToken";
    public final static String REFRESH_TOKEN = "refreshToken";
    public final static String TIMESTAMP = "timestamp";
    public final static String RECEIVER_UID = "receiverUid";
    public final static String MESSAGE = "message";

    // Collections
    public final static String COLLECTION_NAME_USER = "users";
    public final static String COLLECTION_NAME_TOKEN = "tokens";

    // Fields
    public final static String KEY_UID = "_id";

    // Others
    public final static long VALID_PERIOD = 2*60*60000;
}
