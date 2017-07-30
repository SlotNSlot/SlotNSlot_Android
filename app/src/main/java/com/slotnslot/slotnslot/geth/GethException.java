package com.slotnslot.slotnslot.geth;

public class GethException extends RuntimeException {
    public GethException() {
    }

    public GethException(String message) {
        super(message);
    }

    public GethException(Throwable cause) {
        super(cause);
    }

    public GethException(String message, Throwable cause) {
        super(message, cause);
    }
}
