package com.innercircle.services;

public final class Constants {
    private Constants() {}

    // Http Parameters
    public final static String JSON_STRING = "jsonString";
    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";
    public final static String VIP_CODE = "VIPCode";

    public final static String UID = "uid";
    public final static String THE_OTHER_UID = "theOtherUid";
    public final static String ACCESS_TOKEN = "accessToken";
    public final static String REFRESH_TOKEN = "refreshToken";
    public final static String TIMESTAMP = "timestamp";
    public final static String RECEIVER_UID = "receiverUid";
    public final static String MESSAGE = "message";
    public final static String IS_FOLLOWING = "isFollowing";
    public final static String IS_BLOCKED = "isBlocked";
    public final static String SKIP = "skip";
    public final static String LIMIT = "limit";
    public final static String OTHER_UIDS = "otherUids";

    public final static String NEWS_INDEX = "newsIndex";
    public final static String PUB_TIME = "pubTime";
    public final static String PIC_COUNT = "picCount";
    public final static String CONTENT = "content";
    public final static String DELETED = "deleted";
    public final static String COUNT = "count";

    public final static String IMAGE_USAGE = "imageUsage";
    public final static int IMAGE_USAGE_FOR_SETTINGS = 1;
    public final static int IMAGE_USAGE_FOR_TALKS = 2;
    public final static int IMAGE_USAGE_FOR_NEWS = 3;

    public final static String FILE_NAME = "filename";
    public final static String ORIGINAL_FILE_NAME = "originalFilename";
    public final static String GENDER = "gender";
    public final static String USERNAME = "username";

    // Collections
    public final static String COLLECTION_NAME_USERS = "users";
    public final static String COLLECTION_NAME_TOKENS = "tokens";
    public final static String COLLECTION_NAME_RELATIONS = "relations";
    public final static String COLLECTION_NAME_NEWS = "news";
    public final static String COLLECTION_NAME_NEWS_COUNTERS = "news_counters";

    // Fields
    public final static String KEY_UID = "_id";

    // Others
    public final static long VALID_PERIOD = 2*60*60000;
    public final static String EXCHANGE_NAME = "textMessages";
}
