package com.slotnslot.slotnslot.activities.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import com.slotnslot.slotnslot.BuildConfig;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.GethManager;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Header;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.SyncProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.sync_txt)
    TextView syncText;
    @BindView(R.id.sync_prog_txt)
    TextView syncProg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());
        }
        subscribeHead();
    }

    private void subscribeHead() {
        GethManager.getNodeStartedSubject()
                .filter(aBoolean -> aBoolean)
                .take(1)
                .map(e -> {
                    EthereumClient ec = GethManager.getClient();

                    NewHeadHandler handler = new NewHeadHandler() {
                        @Override
                        public void onError(String error) {
                        }

                        @Override
                        public void onNewHead(final Header header) {
                            MainActivity.this.runOnUiThread(() -> syncText.setText("#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + " sync...\n"));
                        }
                    };
                    ec.subscribeNewHead(GethManager.getMainContext(), handler, 16);
                    return true;
                })
                .subscribe(
                        b -> Log.i(TAG, "subscribe new head..."),
                        e -> {
                            e.printStackTrace();
                            Log.i(TAG, "error : " + e.getMessage());
                        }
                );
    }

    @OnClick(R.id.pending_test)
    void pendingActivity() {
        Intent intent = new Intent(getApplicationContext(), PendingExampleActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.filter_test)
    void filterActivity() {
        Intent intent = new Intent(getApplicationContext(), FilterExampleActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.play_test)
    void playTestActivity() {
        Intent intent = new Intent(getApplicationContext(), PlayExampleActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.contract_test)
    void getContractActivity() {
        Intent intent = new Intent(getApplicationContext(), ContractActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_test)
    void getAccountActivity() {
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.progress_check)
    void checkSyncProgress() {
        Observable
                .create(subscriber -> {
                    try {
                        EthereumClient ethereumClient = GethManager.getClient();

                        SyncProgress syncProgress = ethereumClient.syncProgress(GethManager.getMainContext());

                        if (syncProgress != null) {
                            long highestBlock = syncProgress.getHighestBlock();
                            long currentBlock = syncProgress.getCurrentBlock();

                            subscriber.onNext("progress : " + currentBlock + " / " + highestBlock + " ...");
                        } else {
                            subscriber.onNext("chain is not in sync progress now...");
                        }
                        subscriber.onComplete();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                })
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> syncProg.setText(result.toString()),
                        error -> Log.i(TAG, "Fail to get progress..." + error.getLocalizedMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GethManager.getInstance().stopNode();
    }
}
