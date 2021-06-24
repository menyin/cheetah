package com.caisheng.cheetah.common;

public enum ErrorCode {
    OFFLINE(1, "user offline"),
    PUSH_CLIENT_FAILURE(2, "push to connect failure"),
    ROUTER_CHANGE(3, "router change"),
    ACK_TIMEOUT(4, "ack timeout"),
    DISPATCH_ERROR(100, "handler message error"),
    UNSUPPORTED_CMD(101, "unsupported command"),
    REPEAT_HANDSHAKE(102, "repeat handshake"),
    SESSION_EXPIRED(103, "session expired"),
    INVALID_DEVICE(104, "invalid device"),
    UNKNOWN(-1, "unknown");

    ErrorCode(int code, String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorCode = (byte) code;
    }

    private final byte errorCode;
    private final String errorMsg;

    public static ErrorCode toEnum(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.errorCode == code) {
                return errorCode;
            }
        }
        return UNKNOWN;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
