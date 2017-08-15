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
    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(21000000000L);
//    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(30000000000L);

    public static String getManagerAddress() {
        if (GethManager.getNetworkConfig().getNetwork() == EthereumNetwork.TESTNET) {
            return "0xce10092dbf587a3af476174bf94d79d480d6940b";
        } else {
            return "0x04d053f69b504ca6b795c5e4e442222e7f16dcb4";
        }
    }
}
