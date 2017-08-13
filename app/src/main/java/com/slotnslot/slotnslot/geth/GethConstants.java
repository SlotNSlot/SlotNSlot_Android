package com.slotnslot.slotnslot.geth;

import java.math.BigInteger;

public class GethConstants {
    // network setup
    public static final EthereumNetwork NETWORK = EthereumNetwork.RINKEBY;

    public static final String GETH_BASE_DATA_DIR = "GethDroid";
    public static final String GETH_CHAIN_DATA_DIR = "chaindata";

    public static final int LATEST_BLOCK = -1;
    public static final int PENDING_BLOCK = -2;

    public static final BigInteger FUND_GAS_LIMIT = BigInteger.valueOf(90000);
    public static final BigInteger DEFAULT_VALUE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(2500000);
    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);
//    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(30000000000L);

    public static String getManagerAddress() {
        if (GethManager.getNetworkConfig().getNetwork() == EthereumNetwork.TESTNET) {
            return "0xa09b797a9c6501f5c032aa038abcefdc165ab42f";
        } else {
            return "0xdbc3e11e344e7449eb34a35fe8bd9b0902b01d13";
        }
    }
}
