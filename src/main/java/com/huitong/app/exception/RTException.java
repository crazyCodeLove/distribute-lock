package com.huitong.app.exception;

/**
 * author pczhao
 * date  2019-12-31 16:50
 */

public class RTException extends RuntimeException {
    protected int code;

    public RTException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RTException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RTException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}
