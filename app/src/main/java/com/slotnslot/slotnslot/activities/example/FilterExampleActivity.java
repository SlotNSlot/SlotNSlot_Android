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
import com.slotnslot.slotnslot.models.PlayerSeed;
import com.slotnslot.slotnslot.models.Seed;
import com.slotnslot.slotnslot.utils.Convert;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterExampleActivity extends RxAppCompatActivity {
    public static final String TAG = FilterExampleActivity.class.getSimpleName();

    public static final String HELLO_CONTRACT_ADDR = "0x947d154D99b5497800B9250134Ea83701e11bf45";
    public static final String FIB_CONTRACT_ADDR = "0x4612920e12f4301fb940DD70C2002c0921909716";
    private int input = 11;

    private SlotMachine machine;

    private PlayerSeed playerSeed = new PlayerSeed();
    private Seed bankerSeed = new Seed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.filter_start)
    void filterObservable() {
        Hello
                .load(HELLO_CONTRACT_ADDR)
                .printEventObservable()
                .compose(bindToLifecycle())
                .subscribe(
                        printEventResponse -> Log.i(TAG, "event : output : " + printEventResponse.out.getValue()),
                        Throwable::printStackTrace);

        Fibonacci
                .load(FIB_CONTRACT_ADDR)
                .notifyEventObservable()
                .compose(bindToLifecycle())
                .subscribe(notifyEventResponse -> {
                    Log.i(TAG, "event : fib input : " + notifyEventResponse.input.getValue());
                    Log.i(TAG, "event : fib result : " + notifyEventResponse.result.getValue());
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

    @OnClick(R.id.event_set)
    void gameEventSet() {
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
                    if (!Utils.isValidAddress(address.toString())) {
                        Log.e(TAG, "invalid slot machie address");
                        return;
                    }
                    machine = SlotMachine.load(address.toString());

                    machine
                            .gameOccupiedEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                Log.i(TAG, "game occupied by : " + response.player.toString());
                                Log.i(TAG, "player seed0 : " + Utils.byteToHex(response.playerSeed.getValue().get(0).getValue()));
                                Log.i(TAG, "player seed1 : " + Utils.byteToHex(response.playerSeed.getValue().get(1).getValue()));
                                Log.i(TAG, "player seed2 : " + Utils.byteToHex(response.playerSeed.getValue().get(2).getValue()));

                                CredentialManager.setDefault(0, "asdf");
                                machine
                                        .initBankerSeed(bankerSeed.getInitialSeed())
                                        .subscribe(o -> {}, Throwable::printStackTrace);
                            }, Throwable::printStackTrace);

                    machine
                            .bankerSeedInitializedEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                String seed0 = Utils.byteToHex(response._bankerSeed.getValue().get(0).getValue());
                                String seed1 = Utils.byteToHex(response._bankerSeed.getValue().get(1).getValue());
                                String seed2 = Utils.byteToHex(response._bankerSeed.getValue().get(2).getValue());

                                Log.i(TAG, "banker seed initialized");
                                Log.i(TAG, "banker seed0 : " + seed0);
                                Log.i(TAG, "banker seed1 : " + seed1);
                                Log.i(TAG, "banker seed2 : " + seed2);

                                playerSeed.setBankerSeeds(seed0, seed1, seed2);
                            }, Throwable::printStackTrace);

                    machine
                            .gameInitializedEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                Log.i(TAG, "player address : " + response.player.toString());
                                Log.i(TAG, "bet : " + response.bet.getValue());
                                Log.i(TAG, "lines : " + response.lines.getValue());

                                int idx = response.idx.getValue().intValue();
                                Log.i(TAG, "idx : " + idx);

                                CredentialManager.setDefault(0, "asdf");
                                machine
                                        .setBankerSeed(bankerSeed.getSeed(idx), new Uint8(idx))
                                        .subscribe(o -> {}, Throwable::printStackTrace);
                            }, Throwable::printStackTrace);

                    machine
                            .bankerSeedSetEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                String bankerSeed = Utils.byteToHex(response.bankerSeed.getValue());
                                Log.i(TAG, "banker seed : " + bankerSeed);
                                Log.i(TAG, "idx : " + response.idx.getValue());

                                if (!playerSeed.isValidSeed(bankerSeed)) {
                                    Log.e(TAG, "banker seed is invalid : " + bankerSeed);
                                    Log.e(TAG, "previous banker seed : " + playerSeed.getBankerSeeds()[playerSeed.getIndex()]);
                                    Log.e(TAG, "player seed index : " + playerSeed.getIndex());
                                    return;
                                }
                                playerSeed.setNextBankerSeed(bankerSeed);

                                CredentialManager.setDefault(1, "asdf");
                                machine
                                        .setPlayerSeed(playerSeed.getSeed(), new Uint8(this.playerSeed.getIndex()))
                                        .subscribe(o -> {}, Throwable::printStackTrace);
                            }, Throwable::printStackTrace);

                    machine
                            .gameConfirmedEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                Log.i(TAG, "reward : " + response.reward.getValue());
                                Log.i(TAG, "idx : " + response.idx.getValue());

                                bankerSeed.confirm(response.idx.getValue().intValue());
                                playerSeed.confirm(response.idx.getValue().intValue());
                            }, Throwable::printStackTrace);

                    machine
                            .playerLeftEventObservable()
                            .compose(bindToLifecycle())
                            .subscribe(response -> {
                                Log.i(TAG, "player left : " + response.player.toString());
                                Log.i(TAG, "player's initial balance: " + response.playerBalance.getValue());
                            }, Throwable::printStackTrace);
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.occupy)
    void occupy() {
        CredentialManager.setDefault(1, "asdf");
        machine.occupy(playerSeed.getInitialSeed(), Convert.toWei(0.1, Convert.Unit.ETHER)).subscribe(o -> {}, Throwable::printStackTrace);
    }

    @OnClick(R.id.leave)
    void leave() {
        CredentialManager.setDefault(1, "asdf");
        machine.leave().subscribe(o -> {}, Throwable::printStackTrace);
    }

    @OnClick(R.id.game_start)
    void gameStart() {
        CredentialManager.setDefault(1, "asdf");
        machine
                .initGameForPlayer(
                        new Uint256(Convert.toWei(0.001, Convert.Unit.ETHER)),
                        new Uint8(20),
                        new Uint8(playerSeed.getIndex()))
                .subscribe(o -> {}, Throwable::printStackTrace);
    }
}
