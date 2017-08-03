package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Enodes;
import org.ethereum.geth.Geth;
import org.ethereum.geth.NodeConfig;

public class NetworkConfig {

    private EthereumNetwork network = null;
    private NodeConfig nodeConfig = Geth.newNodeConfig();

    private NetworkConfig() {
    }

    public static NetworkConfig getMainnetConfig() {
        return new Builder(EthereumNetwork.MAIN)
                .build();
    }

    public static NetworkConfig getTestnetConfig() {
        return new Builder(EthereumNetwork.TESTNET)
                .build();
    }

    public static NetworkConfig getRinkebyConfig() {
        return new Builder(EthereumNetwork.RINKEBY)
                .build();
    }

    public long getNetworkID() {
        return this.nodeConfig.getEthereumNetworkID();
    }

    public EthereumNetwork getNetwork() {
        return network;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public static class Builder {
        private NetworkConfig config;

        public Builder(EthereumNetwork network) {
            config = new NetworkConfig();
            setNetwork(network);
            setNetworkID(network.getNetworkId());
        }

        public Builder(long networkID) {
            config = new NetworkConfig();
            setNetworkID(networkID);
        }

        public Builder setNetwork(EthereumNetwork network) {
            config.network = network;
            return this;
        }

        // EthereumNetworkID is the network identifier used by the Ethereum protocol to
        // decide if remote peers should be accepted or not.
        public Builder setNetworkID(long networkID) {
            config.nodeConfig.setEthereumNetworkID(networkID);
            return this;
        }

        // EthereumDatabaseCache is the system memory in MB to allocate for database caching.
        // A minimum of 16MB is always reserved.
        public Builder setDBCache(long dbCache) {
            config.nodeConfig.setEthereumDatabaseCache(dbCache);
            return this;
        }

        // MaxPeers is the maximum number of peers that can be connected. If this is
        // set to zero, then only the configured static and trusted peers can connect.
        public Builder setMaxPeerConnection(long maxPeers) {
            config.nodeConfig.setMaxPeers(maxPeers);
            return this;
        }

        public Builder setWhisper(boolean enabled) {
            config.nodeConfig.setWhisperEnabled(enabled);
            return this;
        }


        public Builder setNetStats(String netStats) {
            config.nodeConfig.setEthereumNetStats(netStats);
            return this;
        }

        public NetworkConfig build() {
            return config;
        }
    }
}
