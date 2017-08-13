package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Geth;
import org.ethereum.geth.NodeConfig;

public class NetworkConfig {

    private EthereumNetwork network = null;
    private NodeConfig nodeConfig = Geth.newNodeConfig();

    private NetworkConfig() {
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

        public Builder() {
            config = new NetworkConfig();
            network(GethConstants.NETWORK);
            networkID(GethConstants.NETWORK.getNetworkId());
        }

        public Builder(long networkID) {
            config = new NetworkConfig();
            networkID(networkID);
        }

        public Builder network(EthereumNetwork network) {
            config.network = network;
            return this;
        }

        // EthereumNetworkID is the network identifier used by the Ethereum protocol to
        // decide if remote peers should be accepted or not.
        public Builder networkID(long networkID) {
            config.nodeConfig.setEthereumNetworkID(networkID);
            return this;
        }

        // EthereumDatabaseCache is the system memory in MB to allocate for database caching.
        // A minimum of 16MB is always reserved.
        public Builder dbCache(long dbCache) {
            config.nodeConfig.setEthereumDatabaseCache(dbCache);
            return this;
        }

        // MaxPeers is the maximum number of peers that can be connected. If this is
        // set to zero, then only the configured static and trusted peers can connect.
        public Builder maxPeerConnection(long maxPeers) {
            config.nodeConfig.setMaxPeers(maxPeers);
            return this;
        }

        public Builder whisper(boolean enabled) {
            config.nodeConfig.setWhisperEnabled(enabled);
            return this;
        }


        public Builder netStats(String netStats) {
            config.nodeConfig.setEthereumNetStats(netStats);
            return this;
        }

        public NetworkConfig build() {
            return config;
        }
    }
}
