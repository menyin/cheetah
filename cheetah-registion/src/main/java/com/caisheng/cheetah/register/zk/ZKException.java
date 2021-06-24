package com.caisheng.cheetah.register.zk;

public class ZKException extends RuntimeException {
    public ZKException() {
    }
    public ZKException(String message) {
        super(message);
    }

    public ZKException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZKException(Throwable cause) {
        super(cause);
    }

}
