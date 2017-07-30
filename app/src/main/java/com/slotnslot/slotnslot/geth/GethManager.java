package com.slotnslot.slotnslot.geth;

import android.util.Log;

import com.slotnslot.slotnslot.MainApplication;

import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Node;

import io.reactivex.CompletableEmitter;
import io.reactivex.subjects.BehaviorSubject;

public class GethManager {
    private static final String TAG = GethManager.class.getSimpleName();
    private static GethManager instance = null;

    private static BehaviorSubject<Boolean> nodeStartedObservable = BehaviorSubject.create();

    private GethManager() {
    }

    private Context mainContext;

    private EthereumClient client;

    private NetworkConfig networkConfig;
    private Node node;

    private boolean nodeStarted = false;

    public static GethManager getInstance() {
        if (instance == null) {
            instance = new GethManager.Builder(MainApplication.getContext().getFilesDir().getPath()).build();
        }
        return instance;
    }

    public static BehaviorSubject<Boolean> getNodeStartedObservable() {
        return nodeStartedObservable;
    }

    public void startNode(CompletableEmitter emitter) {
        if (this.nodeStarted) {
            return;
        }

        try {
            this.node.start();
            this.nodeStarted = true;
            this.client = this.node.getEthereumClient();
            nodeStartedObservable.onNext(true);
            emitter.onComplete();
        } catch (Exception e) {
            this.nodeStarted = false;
            nodeStartedObservable.onNext(false);
            Log.e(TAG, e.getLocalizedMessage());
            emitter.onError(new Throwable());
        }
    }

    public void stopNode() {
        if (!this.nodeStarted) {
            return;
        }

        try {
            this.node.stop();
            this.nodeStarted = false;
            nodeStartedObservable.onNext(false);
        } catch (Exception e) {
            this.nodeStarted = false;
            nodeStartedObservable.onNext(false);
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
        String fileDirPath;

        public Builder(String fileDirPath) {
            this.manager = new GethManager();
            this.fileDirPath = fileDirPath;
            withDefault();
        }

        private void withDefault() {
            setNetworkConfig(NetworkConfig.getRinkebyConfig());
            setMainContext(new Context());
        }

        public Builder setNetworkConfig(NetworkConfig networkConfig) {
            this.manager.networkConfig = networkConfig;
            return this;
        }

        public Builder setMainContext(Context context) {
            this.manager.mainContext = context;
            return this;
        }

        public GethManager build() {
            this.manager.node = new Node(
                    this.fileDirPath + getDataDir(this.manager.networkConfig.getNetwork()),
                    this.manager.networkConfig.getNodeConfig());
            return manager;
        }

        private String getDataDir(EthereumNetwork network) {
            if (network == null) {
                return "/temp";
            }
            switch (network) {
                case MAIN:
                    return "/mainnet";
                case TESTNET:
                    return "/testnet";
                case RINKEBY:
                    return "/rinkeby";
                default:
                    return "/temp";
            }
        }
    }
}
