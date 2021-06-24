package com.caisheng.cheetah.api.push;

public class PushException extends RuntimeException {
    public PushException() {
    }

    public PushException(Throwable cause) {
        super(cause);
    }

    public PushException(String message) {
        super(message);
    }

    public PushException(String message, Throwable cause) {
        super(message, cause);
    }
}
