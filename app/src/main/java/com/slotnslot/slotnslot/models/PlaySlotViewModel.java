package com.slotnslot.slotnslot.models;

import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaySlotViewModel {
    private static final String TAG = PlaySlotViewModel.class.getSimpleName();

    //Input
    public BehaviorSubject<Integer> betLineSubject = BehaviorSubject.create();
    public BehaviorSubject<Double> betEthSubject = BehaviorSubject.create();

    //Output
    public BehaviorSubject<Double> totalBetSubject = BehaviorSubject.create();
    public BehaviorSubject<Double> lastWinSubject = BehaviorSubject.createDefault(0.0);

    public PublishSubject<Integer> drawResultSubject = PublishSubject.create();
    public PublishSubject<String> toastSubject = PublishSubject.create();
    public PublishSubject<Boolean> startSpin = PublishSubject.create();
    public PublishSubject<String> invalidSeedFound = PublishSubject.create();

    public Observable<BigInteger> playerBalanceObservable;
    public Observable<BigInteger> bankerBalanceObservable;

    public BehaviorSubject<Boolean> seedReadySubject = BehaviorSubject.createDefault(false);

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SlotMachine machine;
    private RxSlotRoom rxSlotRoom;

    private double previousBetEth = 0.001;
    private int currentLine = 1;
    private double currentBetEth = 0.001;
    private double currentTotalBet = currentLine * currentBetEth;

    private boolean seedReady = false;

    private PlayerSeed playerSeed = new PlayerSeed();

    public PlaySlotViewModel(String slotAddress) {
        this.rxSlotRoom = RxSlotRooms.getSlotRoom(slotAddress);

        // init bet line, eth
        betLineSubject.onNext(Constants.BET_MIN_LINE);
        currentBetEth = rxSlotRoom.getSlotRoom().getMinBet();
        betEthSubject.onNext(currentBetEth);

        // set total bet subject
        Observable
                .combineLatest(betLineSubject, betEthSubject, (line, eth) -> line * eth)
                .distinctUntilChanged()
                .subscribe(totalBet -> {
                    this.currentTotalBet = totalBet;
                    totalBetSubject.onNext(this.currentTotalBet);
                });

        playerBalanceObservable = rxSlotRoom.getSlotRoomSubject().map(SlotRoom::getPlayerBalance);
        bankerBalanceObservable = rxSlotRoom.getSlotRoomSubject().map(SlotRoom::getBankerBalance);

        seedReadySubject.subscribe(ready -> this.seedReady = ready);
    }

    public void kickPlayer() {
        //TODO Implement kick player
    }

    public void linePlus() {
        if (currentLine >= Constants.BET_MAX_LINE) {
            return;
        }
        betLineSubject.onNext(++currentLine);
    }

    public void lineMinus() {
        if (currentLine <= Constants.BET_MIN_LINE) {
            return;
        }
        betLineSubject.onNext(--currentLine);
    }

    public void betPlus() {
        if (currentBetEth >= rxSlotRoom.getSlotRoom().getMaxBet()) {
            return;
        }
        currentBetEth += 0.001;
        betEthSubject.onNext(currentBetEth);
    }

    public void betMinus() {
        if (currentBetEth <= Constants.BET_MIN_ETH) {
            return;
        }
        currentBetEth -= 0.001;
        betEthSubject.onNext(currentBetEth);
    }

    public void maxBet() {
        betLineSubject.onNext(Constants.BET_MAX_LINE);
        betEthSubject.onNext(rxSlotRoom.getSlotRoom().getMaxBet());
    }

    public boolean isBanker() {
        return AccountProvider.identical(rxSlotRoom.getSlotRoom().getBankerAddress());
    }

    public boolean isTest() {
        return true;
    }

    private void setGameEvents() {
        setSeedReadyEvent();

        setOccupiedEvent();
        setBankerSeedInitializedEvent();
        setGameInitializedEvent();
        setBankerSeedSetEvent();
        setGameConfirmedEvent();
        setPlayerLeftEvent();
    }

    private void setSeedReadyEvent() {
        Observable<Bool> playerSeedReadyObservable = machine.initialPlayerSeedReady();
        Observable<Bool> bankerSeedReadyObservable = machine.initialBankerSeedReady();
        Disposable disposable = Observable
                .zip(playerSeedReadyObservable, bankerSeedReadyObservable, (playerSeedReady, bankerSeedReady) -> {
                    Log.i(TAG, "player seed ready : " + playerSeedReady.getValue());
                    Log.i(TAG, "banker seed ready : " + bankerSeedReady.getValue());

                    if (playerSeedReady.getValue() && bankerSeedReady.getValue()) {
                        seedReadySubject.onNext(true);
                    }

                    return true;
                })
                .subscribe(o -> {
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setOccupiedEvent() {
        Disposable disposable = machine
                .gameOccupiedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "occupied by : " + response.player.toString());
                    Log.i(TAG, "player seed1 : " + Utils.byteToHex(response.playerSeed.getValue().get(0).getValue()));
                    Log.i(TAG, "player seed2 : " + Utils.byteToHex(response.playerSeed.getValue().get(1).getValue()));
                    Log.i(TAG, "player seed3 : " + Utils.byteToHex(response.playerSeed.getValue().get(2).getValue()));

                    toastSubject.onNext("slot occupied by : " + response.player.toString());
                    rxSlotRoom.updateBalance();
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setBankerSeedInitializedEvent() {
        Disposable disposable = machine
                .bankerSeedInitializedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "banker initial seed1 : " + Utils.byteToHex(response._bankerSeed.getValue().get(0).getValue()));
                    Log.i(TAG, "banker initial seed2 : " + Utils.byteToHex(response._bankerSeed.getValue().get(1).getValue()));
                    Log.i(TAG, "banker initial seed3 : " + Utils.byteToHex(response._bankerSeed.getValue().get(2).getValue()));

                    toastSubject.onNext("banker seed initialized.");
                    seedReadySubject.onNext(true);
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    public void initGame() {
        machine
                .initialBankerSeedReady()
                .subscribe(bool -> {
                    if (!bool.getValue()) {
                        Log.e(TAG, "banker seed is not initialized yet");
//                            spinButton.setEnabled(true);
                        return;
                    }

                    previousBetEth = currentBetEth;
                    machine
                            .initGameForPlayer(
                                    new Uint256(Convert.toWei(getCurrentBetEth(), Convert.Unit.ETHER)),
                                    new Uint256(currentLine),
                                    new Uint8(playerSeed.getIndex()))
                            .subscribe(o -> {
                            }, Throwable::printStackTrace);
                }, Throwable::printStackTrace);
    }

    private void setGameInitializedEvent() {
        Disposable disposable = machine
                .gameInitializedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "player address : " + response.player.toString());
                    Log.i(TAG, "bet : " + response.bet.getValue());
                    Log.i(TAG, "lines : " + response.lines.getValue());
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    toastSubject.onNext("game initialized.");

                    betEthSubject.onNext(Convert.fromWei(response.bet.getValue(), Convert.Unit.ETHER).doubleValue());
                    currentLine = response.lines.getValue().intValue();
                    betLineSubject.onNext(response.lines.getValue().intValue());

                    rxSlotRoom.updateBalance();

                    if (isBanker()) {
                        startSpin.onNext(true);
                    }
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setBankerSeedSetEvent() {
        Disposable disposable = machine
                .bankerSeedSetEventObservable()
                .subscribe(response -> {
                    String bankerSeed = Utils.byteToHex(response.bankerSeed.getValue());
                    Log.i(TAG, "banker seed : " + bankerSeed);
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    toastSubject.onNext("banker seed set.");

                    if (!playerSeed.isValidSeed(bankerSeed)) {
                        Log.e(TAG, "banker seed is invalid : " + bankerSeed);
                        Log.e(TAG, "previous banker seed : " + playerSeed.getBankerSeeds()[playerSeed.getIndex()]);
                        Log.e(TAG, "player seed index : " + playerSeed.getIndex());
                        invalidSeedFound.onNext(bankerSeed); // invalid seed event
                        return;
                    }
                    playerSeed.setNextBankerSeed(bankerSeed);

                    if (!isBanker()) {
                        machine
                                .setPlayerSeed(playerSeed.getSeed(), new Uint8(playerSeed.getIndex()))
                                .subscribe(o -> {
                                }, Throwable::printStackTrace);
                    }
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setGameConfirmedEvent() {
        Disposable disposable = machine
                .gameConfirmedEventObservable()
                .subscribe(response -> {
                    BigInteger reward = response.reward.getValue();
                    BigInteger winRate = reward.divide(Convert.toWei(previousBetEth, Convert.Unit.ETHER));

                    Log.i(TAG, "reward : " + reward);
                    Log.i(TAG, "bet : " + previousBetEth);
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    toastSubject.onNext("game confirmed. reward : " + Convert.fromWei(reward, Convert.Unit.ETHER));

                    lastWinSubject.onNext(Convert.fromWei(reward, Convert.Unit.ETHER).doubleValue());
                    drawResultSubject.onNext(winRate.intValue());

                    rxSlotRoom.updateBalance();
                    playerSeed.confirm(response.idx.getValue().intValue());
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setPlayerLeftEvent() {
        Disposable disposable = machine
                .playerLeftEventObservable()
                .subscribe(response -> {
                    if (isBanker()) {
                        Log.i(TAG, "player : " + response.player.toString() + " has left");

                        toastSubject.onNext("player : " + response.player.toString() + " has left");

                        rxSlotRoom.updateBalance();
                        seedReadySubject.onNext(false);
                        return;
                    }

                    AccountProvider.updateBalance(); // for player only
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    public void onCreate() {
        machine = SlotMachine.load(rxSlotRoom.getSlotAddress());
        setGameEvents();
    }

    public void onDestroy() {
        compositeDisposable.clear();

        if ("test".equals(rxSlotRoom.getSlotAddress())) {
            return;
        }

        if (isBanker()) {
            return;
        }

        machine
                .leave()
                .flatMap(receipt -> machine.mPlayer())
                .subscribe(
                        address -> Log.i(TAG, "leave... now occupied by : " + address.toString()),
                        Throwable::printStackTrace
                );
    }
}
