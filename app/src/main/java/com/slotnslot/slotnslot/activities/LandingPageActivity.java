package com.slotnslot.slotnslot.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.GethManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.utils.Constants;

import org.ethereum.geth.Header;
import org.ethereum.geth.SyncProgress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;

public class LandingPageActivity extends SlotRootActivity {
    public static final String TAG = LandingPageActivity.class.getSimpleName();

    @BindView(R.id.landing_loading_view)
    ProgressBar progressBar;
    @BindView(R.id.loading_text)
    TextView loadingText;

    private CompletableSubject synced = CompletableSubject.create();
    private boolean doubleBackToExitPressedOnce;
    private String baseDir;
    private String dataDir;
    private PublishSubject<Integer> progressPublisher = PublishSubject.create();
    private PublishSubject<Boolean> copyExpansionPublisher = PublishSubject.create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        ButterKnife.bind(this);

        progressPublisher
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> progressBar.setProgress(progress));
        copyExpansionPublisher
                .compose(bindToLifecycle())
                .observeOn(Schedulers.io())
                .subscribe(isExist -> {
                    if (isExist) {
                        copyExpansionComplete();
                    } else {
                        try {
                            copyExpansion();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        checkPermission(Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE);
    }

    private void saveExpansion() {
        baseDir = Utils.getDataDir() + File.separator + GethConstants.GETH_BASE_DATA_DIR;
        dataDir = baseDir + File.separator + GethConstants.GETH_CHAIN_DATA_DIR;
        File savedNodeFolder = new File(dataDir);
        if (savedNodeFolder.exists()) {
            File completeFile = new File(dataDir + File.separator + "complete");
            copyExpansionPublisher.onNext(completeFile.exists());
        } else {
            savedNodeFolder.mkdirs();
            copyExpansionPublisher.onNext(false);
        }
    }

    private void checkPermission(int requestCode) {
        String permission = null;
        if (requestCode == 0) {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        } else {
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
        } else {
            if (requestCode == Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE) {
                checkPermission(Constants.WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
            } else {
                saveExpansion();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE) {
                checkPermission(Constants.WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
            } else {
                saveExpansion();
            }
        } else if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                checkPermission(requestCode);
            } else {
                //TODO: If user checked that user never ask again, show message
            }
        }
    }

    private void copyExpansionComplete() {
        startNode();
        setSyncProgressEvent();
    }

    private void copyExpansion() throws IOException, PackageManager.NameNotFoundException {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        int version = info.versionCode;

        ZipResourceFile zipFile = APKExpansionSupport.getAPKExpansionZipFile(this, version, 0);
        if (zipFile == null) {
            Log.e(TAG, "no expansion file. download chaindata from other nodes.");
            copyExpansionComplete();
            return;
        }

        Set<String> fileNames = zipFile.getFileNames();
        int currentIndex = 1;
        for (String fileName : fileNames) {
            InputStream inputStream = zipFile.getInputStream(fileName);
            copyFile(inputStream, fileName);
            progressPublisher.onNext((currentIndex * 100) / fileNames.size());
            currentIndex++;
        }
        File completeFile = new File(dataDir + File.separator + "complete");
        completeFile.createNewFile();
        copyExpansionComplete();
    }

    private void copyFile(InputStream inputStream, String destination) {
        File file = new File(baseDir + File.separator + destination);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            if (outputStream == null) {
                Log.d("Expansion", "outputStream is null");
            } else {
                Log.d("Expansion", "outputStream is good");
            }

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    long timeDiff = currentTime - header.getTime();
                    long size = GethManager.getNode().getPeersInfo().size();
                    if (size > 2 && timeDiff < 300) {
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

    private void startNode() {
        Completable
                .create(e -> {
                    GethManager.getInstance().startNode();
                    e.onComplete();
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Utils.showToast("node started"), Throwable::printStackTrace);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            GethManager.getInstance().stopNode();
            Utils.showToast("node stopped");
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Utils.showToast("press back again to exit");
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);
    }
}
