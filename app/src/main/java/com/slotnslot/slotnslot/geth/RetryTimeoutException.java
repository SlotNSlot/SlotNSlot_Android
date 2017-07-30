package com.slotnslot.slotnslot.geth;

public class RetryTimeoutException extends GethException {
    public RetryTimeoutException() {
    }

    public RetryTimeoutException(String message) {
        super(message);
    }
}
