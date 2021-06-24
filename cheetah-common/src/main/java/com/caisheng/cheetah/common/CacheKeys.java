package com.caisheng.cheetah.common;


public final class CacheKeys {
    public final static String USER_PREFIX = "cheetah:ur:";
    public final static String SESSION_PREFIX = "cheetah:rs:";
    public final static String FAST_CONNECTION_DEVICE_PREFIX = "cheetah:fcd:";
    public final static String ONLINE_USER_LIST_KEY_PREFIX = "cheetah:oul:";//在线用户列表
    public final static String PUSH_TASK_PREFIX = "cheetah:pt:";
    public final static String SESSION_AES_KEY = "cheetah:sa";
    public final static String SESSION_AES_SEQ_KEY = "cheetah:sas";

    public static String getUserRouterKey(String userId) {
        return USER_PREFIX + userId;
    }

    public static String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String getDeviceIdKey(String deviceId) {
        return FAST_CONNECTION_DEVICE_PREFIX + deviceId;
    }

    public static String getOnlineUserListKey(String publicIP) {
        return ONLINE_USER_LIST_KEY_PREFIX+ publicIP;
    }

    public static String getPushTaskKey(String taskId) {
        return PUSH_TASK_PREFIX + taskId;
    }
}
