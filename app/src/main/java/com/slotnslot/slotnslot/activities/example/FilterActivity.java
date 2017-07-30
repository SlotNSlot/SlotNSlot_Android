package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.contract.Fibonacci;
import com.slotnslot.slotnslot.contract.Hello;
import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.contract.SlotMachineStorage;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.utils.Convert;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterActivity extends RxAppCompatActivity {
    public static final String TAG = FilterActivity.class.getSimpleName();

    public static final String HELLO_CONTRACT_ADDR = "0x947d154D99b5497800B9250134Ea83701e11bf45";
    public static final String FIB_CONTRACT_ADDR = "0x4612920e12f4301fb940DD70C2002c0921909716";
    private int input = 11;

    private SlotMachine machine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.filter_start)
    void filterStart() {
        CredentialManager.setDefault(0, "asdf");
        SlotMachineManager
                .load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS)
                .getStorageAddr()
                .flatMap(address -> {
                    Log.i(TAG, "slot storage address : " + address.toString());
                    return SlotMachineStorage
                            .load(address.toString())
                            .getSlotMachine(new Address(CredentialManager.getDefaultAccountHex()), new Uint256(0));
                })
                .subscribe(address -> {
                    Log.i(TAG, "slot machine address : " + address.toString());
                    if (Utils.isValidAddress(address.toString())) {
                        machine = SlotMachine.load(address.toString());

                        machine
                                .gameInitializedEventObservable()
                                .subscribe(response -> {
                                    Log.i(TAG, "player address : " + response.player.toString());
                                    Log.i(TAG, "bet : " + response.bet.getValue());
                                    Log.i(TAG, "lines : " + response.lines.getValue());

                                    CredentialManager.setDefault(0, "asdf");
                                    machine
                                            .setBankerSeed(null, null)
                                            .subscribe();
                                });

                        machine
                                .bankerSeedSetEventObservable()
                                .subscribe(response -> {
                                    Log.i(TAG, "banker seed : " + Utils.byteToHex(response.bankerSeed.getValue()));

                                    CredentialManager.setDefault(1, "asdf");
                                    machine
                                            .setPlayerSeed(null, null)
                                            .subscribe();
                                });

                        machine
                                .gameConfirmedEventObservable()
                                .subscribe(response -> Log.i(TAG, "reward : " + response.reward.getValue()));
                    }
                });
    }

    @OnClick(R.id.filter_ob)
    void filterObservable() {
        Hello
                .load(HELLO_CONTRACT_ADDR)
                .printEventObservable()
                .subscribe(
                        printEventResponse -> System.out.println("event : output : " + printEventResponse.out.getValue()),
                        Throwable::printStackTrace);

        Fibonacci
                .load(FIB_CONTRACT_ADDR)
                .notifyEventObservable()
                .subscribe(notifyEventResponse -> {
                    Log.i(TAG, "event : fib input : " + notifyEventResponse.input.getValue());
                    Log.i(TAG, "event : fib result : " + notifyEventResponse.result.getValue());
                });
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
                                System.out.println("say2 output : " + out.getValue());
                            }
                        },
                        Throwable::printStackTrace);
    }

    @OnClick(R.id.send_tx2)
    void sendTx2() {
        Fibonacci fibonacci = Fibonacci.load(FIB_CONTRACT_ADDR);

        fibonacci.fibonacciNotify(new Uint256(11))
                .map(fibonacci::getNotifyEvents)
                .subscribe(
                        notifyEvents -> {
                            if (notifyEvents.isEmpty()) {
                                return;
                            }
                            Log.i("test", "fib input : " + notifyEvents.get(0).input.getValue());
                            Log.i("test", "fib result : " + notifyEvents.get(0).result.getValue());
                        },
                        Throwable::printStackTrace);
    }

    @OnClick(R.id.game_start)
    void gameStart() {
        CredentialManager.setDefault(1, "asdf");
        machine
                .initGameForPlayer(new Uint256(Convert.toWei(0.02, Convert.Unit.ETHER)), new Uint256(1), new Uint256(0))
                .subscribe();
    }
}
