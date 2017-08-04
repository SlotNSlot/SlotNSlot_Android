package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Enode;
import org.ethereum.geth.Enodes;
import org.ethereum.geth.Geth;

public enum EthereumNetwork {
    MAIN(1),
    TESTNET(3),
    RINKEBY(4);

    private int networkId;

    EthereumNetwork(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    }
}
