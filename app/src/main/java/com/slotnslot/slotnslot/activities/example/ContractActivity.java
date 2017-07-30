package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.contract.Arrays;
import com.slotnslot.slotnslot.contract.Fibonacci;
import com.slotnslot.slotnslot.contract.Hello;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ContractActivity extends RxAppCompatActivity {
    private static final String TAG = ContractActivity.class.getSimpleName();

    public static final String HELLO_CONTRACT_ADDR = "0x947d154D99b5497800B9250134Ea83701e11bf45";
    public static final String ARRAY_CONTRACT_ADDR = "0x4347398553458Ab4DEa00a9467038366a9efFC48";
    public static final String FIB_CONTRACT_ADDR = "0x4612920e12f4301fb940DD70C2002c0921909716";

    @BindView(R.id.textView)
    TextView info;
    @BindView(R.id.contract_async_txt)
    TextView contractAsyncTxt;
    @BindView(R.id.contract_txt)
    TextView contractTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract);
        ButterKnife.bind(this);

        info.append("contract address : " + HELLO_CONTRACT_ADDR + "\n");

        asyncCheck();
    }

    private void asyncCheck() {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> contractAsyncTxt.setText("" + aLong));
    }


    @OnClick(R.id.call_test)
    void callContract() {
        Hello hello = Hello.load(HELLO_CONTRACT_ADDR);

        hello.say2(new Uint256(10))
                .compose(bindToLifecycle())
                .subscribe(uint256 -> {
                    BigInteger result = uint256.getValue();
                    Log.i(TAG, "say2 output : " + result);
                    contractTxt.append(result + "\n");
                }, err -> {
                    Toast.makeText(getApplicationContext(), "Fail to call message...: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Fail to call message...: " + err.getMessage());
                });
    }

    @OnClick(R.id.send_test)
    void sendTransaction() {
        Hello hello = Hello.load(HELLO_CONTRACT_ADDR);

        hello.say1(new Uint256(10))
                .compose(bindToLifecycle())
                .map(r -> {
                    Log.i(TAG, "say2 receipt : " + r);
                    Log.i(TAG, "say2 transaction hash : " + r.getTxHash().getHex());
                    return hello.getPrintEvents(r);
                })
                .subscribe(
                        printEvents -> {
                            if (printEvents != null && !printEvents.isEmpty()) {
                                Uint256 out = printEvents.get(0).out;
                                Log.i(TAG, "say2 output : " + out.getValue());
                                Toast.makeText(getApplicationContext(), "hello say2 result: " + out.getValue(), Toast.LENGTH_SHORT).show();
                                contractTxt.append(out.getValue() + "\n");
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            Log.i(TAG, "Fail to call message..." + e);
                        });
    }

    @OnClick(R.id.fixed_reverse)
    void callFixedRev() {
        Arrays arrays = Arrays.load(ARRAY_CONTRACT_ADDR);

        arrays.fixedReverse(new StaticArray<>(
                new Uint256(1),
                new Uint256(2),
                new Uint256(3),
                new Uint256(4),
                new Uint256(5),
                new Uint256(6),
                new Uint256(7),
                new Uint256(8),
                new Uint256(9),
                new Uint256(0)))
                .compose(bindToLifecycle())
                .subscribe(uint256StaticArray -> {
                    if (uint256StaticArray.getValue().isEmpty()) {
                        return;
                    }
                    for (int i = 0; i < uint256StaticArray.getValue().size(); i++) {
                        Log.i(TAG, "fixed reversed : " + uint256StaticArray.getValue().get(i).getValue());
                    }
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.dynamic_reverse)
    void callDynamicRev() {
        Arrays arrays = Arrays.load(ARRAY_CONTRACT_ADDR);

        arrays.dynamicReverse(new DynamicArray<>(new Uint256(1), new Uint256(2), new Uint256(3)))
                .compose(bindToLifecycle())
                .subscribe(uint256DynamicArray -> {
                    if (uint256DynamicArray.getValue().isEmpty()) {
                        return;
                    }
                    for (int i = 0; i < uint256DynamicArray.getValue().size(); i++) {
                        Log.i(TAG, "fixed reversed : " + uint256DynamicArray.getValue().get(i).getValue());
                    }
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.fib_call)
    void callFibonacci() {
        Fibonacci fibonacci = Fibonacci.load(FIB_CONTRACT_ADDR);

        fibonacci.fibonacci(new Uint256(11))
                .compose(bindToLifecycle())
                .subscribe(uint256 -> {
                    Log.i(TAG, "fib result : " + uint256.getValue());
                    contractTxt.append(uint256.getValue() + "\n");
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.fib_tx)
    void sendFibonacci() {
        Fibonacci fibonacci = Fibonacci.load(FIB_CONTRACT_ADDR);

        fibonacci.fibonacciNotify(new Uint256(11))
                .compose(bindToLifecycle())
                .observeOn(Schedulers.io())
                .map(fibonacci::getNotifyEvents)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notifyEvents -> {
                    if (notifyEvents.isEmpty()) {
                        return;
                    }
                    Log.i(TAG, "fib input : " + notifyEvents.get(0).input.getValue());
                    Log.i(TAG, "fib result : " + notifyEvents.get(0).result.getValue());
                    contractTxt.append(notifyEvents.get(0).result.getValue() + "\n");
                }, Throwable::printStackTrace);
    }
}
