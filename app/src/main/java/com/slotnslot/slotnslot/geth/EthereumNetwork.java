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

    public String getGenesis() {
        switch (this) {
            case MAIN:
                return Geth.mainnetGenesis();
            case TESTNET:
                return Geth.testnetGenesis();
            case RINKEBY:
                return Geth.rinkebyGenesis();
            default:
                return Geth.testnetGenesis();
        }
    }

    public Enodes getBootnodes() {
        switch (this) {
            case MAIN:
            case TESTNET:
                return Geth.foundationBootnodes();
            case RINKEBY:
                Enodes enodes = new Enodes();
                enodes.append(new Enode("enode://a24ac7c5484ef4ed0c5eb2d36620ba4e4aa13b8c84684e1b4aab0cebea2ae45cb4d375b77eab56516d34bfbd3c1a833fc51296ff084b770b94fb9028c4d25ccf@52.169.42.101:30303?discport=30304"));
                return enodes;
            default:
                return Geth.foundationBootnodes();
        }
    }
}
