package com.slotnslot.slotnslot.models;

public class PayLineTuple {
    public final int SYMBOL_INDEX;
    public final int LENGTH;
    private int lineNumber;

    public PayLineTuple(int SYMBOL_INDEX, int LENGTH) {
        this.SYMBOL_INDEX = SYMBOL_INDEX;
        this.LENGTH = LENGTH;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
