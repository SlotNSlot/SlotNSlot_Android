package com.slotnslot.slotnslot.geth;

import java.math.BigInteger;

public class GethConstants {
    public static final String SLOT_MANAGER_CONTRACT_ADDRESS = "0x4d43f38f7b698ef64ba5c5d23311f36a3cc3ef86";
    public static final int LATEST_BLOCK = -1;

    public static final BigInteger FUND_GAS_LIMIT = BigInteger.valueOf(90000);
    public static final BigInteger DEFAULT_VALUE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(2500000);
    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);
//    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(30000000000L);
}
