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
        return new Builder(
                EthereumNetwork.MAIN.getNetworkId(),
                EthereumNetwork.MAIN.getGenesis(),
                EthereumNetwork.MAIN.getBootnodes())
                .setNetwork(EthereumNetwork.MAIN)
                .build();
    }

    public static NetworkConfig getTestnetConfig() {
        return new Builder(
                EthereumNetwork.TESTNET.getNetworkId(),
                EthereumNetwork.TESTNET.getGenesis(),
                EthereumNetwork.TESTNET.getBootnodes())
                .setNetwork(EthereumNetwork.TESTNET)
                .build();
    }

    public static NetworkConfig getRinkebyConfig() {
        return new Builder(
                EthereumNetwork.RINKEBY.getNetworkId(),
                EthereumNetwork.RINKEBY.getGenesis(),
                EthereumNetwork.RINKEBY.getBootnodes())
                .setNetwork(EthereumNetwork.RINKEBY)
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
            setGenesis(network.getGenesis());
            setBootnodes(network.getBootnodes());

            setWhisper(true);
        }

        public Builder(long networkID, String genesis, Enodes bootnodes) {
            config = new NetworkConfig();

            setNetworkID(networkID);
            setGenesis(genesis);
            setBootnodes(bootnodes);
//            setWhisper(true);
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

        // EthereumGenesis is the genesis JSON to use to seed the blockchain with. An
        // empty genesis state is equivalent to using the mainnet's state.
        public Builder setGenesis(String genesis) {
            config.nodeConfig.setEthereumGenesis(genesis);
            return this;
        }

        // Bootstrap nodes used to establish connectivity with the rest of the network.
        public Builder setBootnodes(Enodes bootnodes) {
            config.nodeConfig.setBootstrapNodes(bootnodes);
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
