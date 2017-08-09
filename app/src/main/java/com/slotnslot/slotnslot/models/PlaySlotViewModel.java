package com.slotnslot.slotnslot.models;

import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;

import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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

    public PublishSubject<DrawOption> drawResultSubject = PublishSubject.create();
    public PublishSubject<Boolean> startSpin = PublishSubject.create();
    public PublishSubject<String> invalidSeedFound = PublishSubject.create();
    public PublishSubject<Boolean> clearSpin = PublishSubject.create();

    public Observable<BigInteger> playerBalanceObservable;
    public Observable<BigInteger> bankerBalanceObservable;

    public BehaviorSubject<Boolean> seedReadySubject = BehaviorSubject.createDefault(false);
    public BehaviorSubject<Boolean[]> txConfirmationSubject = BehaviorSubject.createDefault(new Boolean[]{true, true, true});

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SlotMachine machine;
    private RxSlotRoom rxSlotRoom;

    private double previousBetEth = 0.001;
    private int currentLine = 1;
    private double currentBetEth = 0.001;
    private double currentTotalBet = currentLine * currentBetEth;

    private boolean seedReady = false;
    private Boolean[] txConfirmation = {true, true, true};

    private PlayerSeed playerSeed;

    public PlaySlotViewModel(RxSlotRoom rxSlotRoom) {
        this.rxSlotRoom = rxSlotRoom;

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
        currentLine = Constants.BET_MAX_LINE;
        betLineSubject.onNext(currentLine);
        currentBetEth = rxSlotRoom.getSlotRoom().getMaxBet();
        betEthSubject.onNext(currentBetEth);
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
        Disposable disposable = machine
                .initialBankerSeedReady()
                .subscribe(ready -> {
                    Log.i(TAG, "banker seed ready : " + ready.getValue());
                    if (ready.getValue()) {
                        seedReadySubject.onNext(true);
                    }
                });
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

                    Utils.showToast("slot occupied by : " + response.player.toString());
                    rxSlotRoom.updateBalance();
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setBankerSeedInitializedEvent() {
        Disposable disposable = machine
                .bankerSeedInitializedEventObservable()
                .subscribe(response -> {
                    String seed0 = Utils.byteToHex(response._bankerSeed.getValue().get(0).getValue());
                    String seed1 = Utils.byteToHex(response._bankerSeed.getValue().get(1).getValue());
                    String seed3 = Utils.byteToHex(response._bankerSeed.getValue().get(2).getValue());

                    Log.i(TAG, "banker initial seed1 : " + seed0);
                    Log.i(TAG, "banker initial seed2 : " + seed1);
                    Log.i(TAG, "banker initial seed3 : " + seed3);

                    playerSeed.setBankerSeeds(seed0, seed1, seed3);

                    Utils.showToast("banker seed initialized.");
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
                        return;
                    }

                    int index = playerSeed.getIndex();
                    machine
                            .initGameForPlayer(
                                    new Uint256(Convert.toWei(getCurrentBetEth(), Convert.Unit.ETHER)),
                                    new Uint8(currentLine),
                                    new Uint8(index))
                            .subscribe(o -> updateTxConfirmation(true, index), Throwable::printStackTrace);
                    updateTxConfirmation(false, index);
                }, Throwable::printStackTrace);
    }

    private void setGameInitializedEvent() {
        Disposable disposable = machine
                .gameInitializedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "player address : " + response.player.toString());
                    Log.i(TAG, "lines : " + response.lines.getValue());
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    double bet = Convert.fromWei(response.bet.getValue(), Convert.Unit.ETHER).doubleValue();
                    Log.i(TAG, "bet : " + bet);

                    previousBetEth = bet;
                    rxSlotRoom.updateBalance();

                    if (isBanker()) {
                        betEthSubject.onNext(bet);
                        betLineSubject.onNext(response.lines.getValue().intValue());
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

                    if (isBanker()) {
                        return;
                    }

                    if (!playerSeed.isValidSeed(bankerSeed)) {
                        Log.e(TAG, "banker seed is invalid : " + bankerSeed);
                        Log.e(TAG, "previous banker seed : " + playerSeed.getBankerSeeds()[playerSeed.getIndex()]);
                        Log.e(TAG, "player seed index : " + playerSeed.getIndex());
                        invalidSeedFound.onNext(bankerSeed); // invalid seed event
                        return;
                    }
                    playerSeed.setNextBankerSeed(bankerSeed);

                    Completable
                            .complete()
                            .observeOn(Schedulers.computation())
                            .subscribe(() -> machine
                                    .setPlayerSeed(playerSeed.getSeed(), new Uint8(this.playerSeed.getIndex()))
                                    .subscribe(o -> {
                                    }, Throwable::printStackTrace), Throwable::printStackTrace);
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setGameConfirmedEvent() {
        Disposable disposable = machine
                .gameConfirmedEventObservable()
                .distinctUntilChanged(response -> response.idx)
                .subscribe(response -> {
                    BigInteger reward = response.reward.getValue();
                    int winRate = reward.divide(Convert.toWei(previousBetEth, Convert.Unit.ETHER)).intValue();
                    int index = response.idx.getValue().intValue();

                    Log.i(TAG, "reward : " + reward);
                    Log.i(TAG, "bet : " + previousBetEth);
                    Log.i(TAG, "win rate : " + winRate);
                    Log.i(TAG, "idx : " + index);

                    drawResultSubject.onNext(new DrawOption(winRate, previousBetEth, (index + 1) % 3));

                    rxSlotRoom.updateBalance();
                    if (!isBanker()) {
                        playerSeed.confirm(index);
                        playerSeed.save(machine.getContractAddress());
                    }
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setPlayerLeftEvent() {
        Disposable disposable = machine
                .playerLeftEventObservable()
                .subscribe(response -> {
                    if (isBanker()) {
                        Log.i(TAG, "player : " + response.player.toString() + " has left");

                        Utils.showToast("player : " + response.player.toString() + " has left");

                        rxSlotRoom.updateBalance();
                        seedReadySubject.onNext(false);
                        clearSpin.onNext(true);
                        return;
                    }

                    AccountProvider.updateBalance(); // for player only
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    public void gameOccupy(Double deposit) {
        if (deposit == null || isBanker()) {
            return;
        }
        machine
                .initialPlayerSeedReady()
                .filter(ready -> !ready.getValue())
                .observeOn(Schedulers.computation())
                .map(ready -> playerSeed.getInitialSeed())
                .flatMap(initialSeeds -> machine.occupy(initialSeeds, Convert.toWei(deposit, Convert.Unit.ETHER)))
                .subscribe(o -> playerSeed.save(machine.getContractAddress()), Throwable::printStackTrace);
    }

    public void updateTxConfirmation(boolean confirm, int index) {
        txConfirmation[index] = confirm;
        txConfirmationSubject.onNext(txConfirmation);
    }

    public void onCreate(Double deposit) {
        machine = SlotMachine.load(rxSlotRoom.getSlotAddress());
        PlayerSeed.load(rxSlotRoom.getSlotAddress())
                .subscribe(seed -> {
                    playerSeed = (PlayerSeed) seed;
                    gameOccupy(deposit);
                }, Throwable::printStackTrace);
        setGameEvents();
    }

    public void onDestroy() {
        Log.i(TAG, "destroy... clear all events...");
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

    public static class DrawOption {
        public int winRate;
        public double bet;
        public int nextIdx;
        public boolean nextTxConfirmation;

        public DrawOption(int winRate, double bet, int nextIdx) {
            this.winRate = winRate;
            this.bet = bet;
            this.nextIdx = nextIdx;
        }
    }
}
