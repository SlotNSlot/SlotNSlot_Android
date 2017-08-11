package com.slotnslot.slotnslot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.GethManager;
import com.slotnslot.slotnslot.geth.Utils;

import org.ethereum.geth.Header;
import org.ethereum.geth.SyncProgress;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;

public class LandingPageActivity extends SlotRootActivity {
    public static final String TAG = LandingPageActivity.class.getSimpleName();

    @BindView(R.id.landing_loading_view)
    ProgressBar progressBar;
    @BindView(R.id.loading_text)
    TextView loadingText;
    @BindView(R.id.sync_btn)
    Button syncButton;

    private CompletableSubject synced = CompletableSubject.create();
    private boolean doubleBackToExitPressedOnce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        ButterKnife.bind(this);

        startNode();
        setSyncProgressEvent();
        setSyncButtonEvent();
    }

    private void setSyncProgressEvent() {
        Disposable sync = GethManager.getNodeStartedSubject()
                .compose(bindToLifecycle())
                .debounce(2, TimeUnit.SECONDS)
                .filter(b -> b)
                .take(1)
                .flatMap(b -> Observable.interval(1000, TimeUnit.MILLISECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> {
                    if (!GethManager.nodeStarted) {
                        return;
                    }
                    Header header = GethManager.getClient().getHeaderByNumber(GethManager.getMainContext(), -1);
                    long currentTime = System.currentTimeMillis() / 1000;
                    long t = currentTime - header.getTime();
                    long size = GethManager.getNode().getPeersInfo().size();
                    if (size > 1 && t < 300) {
                        synced.onComplete();
                    }

                    SyncProgress syncProgress = GethManager.getClient().syncProgress(GethManager.getMainContext());
                    if (syncProgress == null) {
                        return;
                    }
                    long highestBlock = syncProgress.getHighestBlock();
                    long currentBlock = syncProgress.getCurrentBlock();
                    long knownStates = syncProgress.getKnownStates();
                    long pulledStates = syncProgress.getPulledStates();
                    long startingBlock = syncProgress.getStartingBlock();
                    int progress = (int) ((currentBlock + pulledStates - startingBlock) * 100 / (highestBlock + knownStates - startingBlock));
                    progressBar.setProgress(progress);
                    loadingText.setText("Loading... " + currentBlock);
                }, Throwable::printStackTrace);

        synced
                .compose(bindToLifecycle())
                .subscribe(() -> {
                    sync.dispose();
                    Intent intent = new Intent(getApplicationContext(), SignInUpActivity.class);
                    startActivity(intent);
                    finish();
                }, Throwable::printStackTrace);
    }

    private void setSyncButtonEvent() {
        RxView
                .clicks(syncButton)
                .subscribe(o -> {
                    if (GethManager.nodeStarted) {
                        syncButton.setText("start syncing");
                        GethManager.getInstance().stopNode();
                    } else {
                        syncButton.setText("stop syncing");
                        startNode();
                    }
                }, Throwable::printStackTrace);
    }

    private void startNode() {
        Completable
                .create(e -> {
                    GethManager.getInstance().startNode();
                    e.onComplete();
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Utils.showToast("press back again to exit");
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
