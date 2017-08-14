package com.slotnslot.slotnslot.geth;

public class InsufficientFundException extends GethException {
    public InsufficientFundException() {
    }

    public InsufficientFundException(String message) {
        super(message);
    }
}
