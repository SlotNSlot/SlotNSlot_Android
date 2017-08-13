package com.slotnslot.slotnslot.geth;

import android.util.Log;

import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Node;

import io.reactivex.subjects.BehaviorSubject;

public class GethManager {
    private static final String TAG = GethManager.class.getSimpleName();
    private static GethManager instance = null;

    private static BehaviorSubject<Boolean> nodeStartedSubject = BehaviorSubject.create();

    private GethManager() {
    }

    private Context mainContext;

    private EthereumClient client;

    private NetworkConfig networkConfig;
    private Node node;

    public static boolean nodeStarted = false;

    public static GethManager getInstance() {
        if (instance == null) {
            instance = new GethManager.Builder()
//                    .networkConfig(NetworkConfig.getTestnetConfig())
                    .build();
        }
        return instance;
    }

    public static BehaviorSubject<Boolean> getNodeStartedSubject() {
        return nodeStartedSubject;
    }

    public void startNode() {
        if (nodeStarted) {
            return;
        }

        try {
            this.node.start();
            this.client = this.node.getEthereumClient();
            nodeStarted = true;
            nodeStartedSubject.onNext(true);
        } catch (Exception e) {
            nodeStarted = false;
            nodeStartedSubject.onNext(false);
            Log.e(TAG, e.getMessage());
        }
    }

    public void stopNode() {
        if (!nodeStarted) {
            return;
        }

        try {
            nodeStarted = false;
            nodeStartedSubject.onNext(false);
            this.node.stop();
        } catch (Exception e) {
            nodeStarted = false;
            nodeStartedSubject.onNext(false);
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static Context getMainContext() {
        return instance.mainContext;
    }

    public static EthereumClient getClient() {
        return instance.client;
    }

    public static NetworkConfig getNetworkConfig() {
        return instance.networkConfig;
    }

    public static Node getNode() {
        return instance.node;
    }

    public static class Builder {
        GethManager manager;

        public Builder() {
            this.manager = new GethManager();
            withDefault();
        }

        private void withDefault() {
            networkConfig(new NetworkConfig.Builder().build());
            mainContext(new Context());
        }

        public Builder networkConfig(NetworkConfig networkConfig) {
            this.manager.networkConfig = networkConfig;
            return this;
        }

        public Builder mainContext(Context context) {
            this.manager.mainContext = context;
            return this;
        }

        public GethManager build() {
            CredentialManager.setKeyStore();
            this.manager.node = new Node(
                    Utils.getDataDir(),
                    this.manager.networkConfig.getNodeConfig());
            return manager;
        }

    }
}
