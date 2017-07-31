package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.contract.Hello;
import com.slotnslot.slotnslot.geth.FilterManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PendingExampleActivity extends RxAppCompatActivity {
    private static final String TAG = PendingExampleActivity.class.getSimpleName();

    public static final String HELLO_CONTRACT_ADDR = "0x947d154D99b5497800B9250134Ea83701e11bf45";
    public int input = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_example);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.pending_event_set)
    public void pendingEventSet() {
        final Event event = new Event(
                "Print",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));

        FilterManager.Filter filter = new FilterManager.Filter(event, HELLO_CONTRACT_ADDR);
        FilterManager.pendingFilterLogs(filter)
                .compose(bindToLifecycle())
                .map(log -> Utils.extractEventParameters(event, log))
                .subscribe(eventValues -> {
                    Log.i(TAG, "output : " + eventValues.getNonIndexedValues().get(0).getValue());
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.send_tx)
    void sendTx() {
        Hello hello = Hello.load(HELLO_CONTRACT_ADDR);

        hello.say1(new Uint256(input++))
                .map(hello::getPrintEvents)
                .subscribe(
                        printEvents -> {
                            if (printEvents != null && !printEvents.isEmpty()) {
                                Uint256 out = printEvents.get(0).out;
                                Log.i(TAG, "say2 output : " + out.getValue());
                            }
                        },
                        Throwable::printStackTrace);
    }
}
