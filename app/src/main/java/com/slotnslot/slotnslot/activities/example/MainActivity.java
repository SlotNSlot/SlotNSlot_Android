package com.slotnslot.slotnslot.activities.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.slotnslot.slotnslot.BuildConfig;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.CredentialManager;
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

        CredentialManager.setDefault(0, "asdf");
        subscribeHead();
    }

    private void subscribeHead() {
        GethManager.getNodeStartedObservable()
                .filter(aBoolean -> aBoolean)
                .take(1)
                .map(e -> {
                    GethManager manager = GethManager.getInstance();
                    EthereumClient ec = manager.getClient();

                    NewHeadHandler handler = new NewHeadHandler() {
                        @Override
                        public void onError(String error) {
                        }

                        @Override
                        public void onNewHead(final Header header) {
                            MainActivity.this.runOnUiThread(() -> syncText.setText("#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + " sync...\n"));
                        }
                    };
                    ec.subscribeNewHead(manager.getMainContext(), handler, 16);
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

    @OnClick(R.id.filter_test)
    void filterActivity() {
        Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.play_test)
    void playTestActivity() {
        Intent intent = new Intent(getApplicationContext(), PlayExampleActivity.class);
        startActivity(intent);
    }

    public void getAccountActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        startActivity(intent);
    }

    public void getTransactionActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), TransactionActivity.class);
        startActivity(intent);
    }

    public void getContractActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ContractActivity.class);
        startActivity(intent);
    }

    public void checkSyncProgress(View view) {
        Observable
                .create(subscriber -> {
                    try {
                        GethManager manager = GethManager.getInstance();
                        EthereumClient ethereumClient = manager.getClient();

                        SyncProgress syncProgress = ethereumClient.syncProgress(manager.getMainContext());

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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(
                        result -> syncProg.setText(result.toString()),
                        error -> Log.i(TAG, "Fail to get progress..." + error.getLocalizedMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Stop Node...", Toast.LENGTH_SHORT).show();
    }
}
