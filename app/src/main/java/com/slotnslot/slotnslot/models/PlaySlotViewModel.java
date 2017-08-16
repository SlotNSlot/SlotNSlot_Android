package com.slotnslot.slotnslot.models;

import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.geth.InsufficientFundException;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    public PublishSubject<Boolean> stopWatch = PublishSubject.create();

    //Output
    public BehaviorSubject<Double> totalBetSubject = BehaviorSubject.create();
    public BehaviorSubject<Double> lastWinSubject = BehaviorSubject.createDefault(0.0);

    public PublishSubject<DrawOption> drawResultSubject = PublishSubject.create();
    public PublishSubject<Boolean> startSpin = PublishSubject.create();
    public PublishSubject<String> invalidSeedFound = PublishSubject.create();
    public PublishSubject<Boolean> clearSpin = PublishSubject.create();
    public PublishSubject<Boolean> playerKicked = PublishSubject.create();
    public PublishSubject<Boolean> alreadyOccupied = PublishSubject.create();
    public PublishSubject<Boolean> fundInsufficient = PublishSubject.create();
    public PublishSubject<Boolean> balanceInsufficient = PublishSubject.create();
    public Observable<Boolean> timeout = stopWatch.debounce(120, TimeUnit.SECONDS).filter(b -> b);

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
    public boolean spinStarted = false;
    private Boolean[] txConfirmation = {true, true, true};

    private PlayerSeed playerSeed = new PlayerSeed();

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
        if ((currentLine + 1) * currentBetEth > Convert.fromWei(rxSlotRoom.getSlotRoom().getPlayerBalance(), Convert.Unit.ETHER).doubleValue()) {
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
        if (currentLine * (currentBetEth + 0.001) > Convert.fromWei(rxSlotRoom.getSlotRoom().getPlayerBalance(), Convert.Unit.ETHER).doubleValue()) {
            return;
        }
        currentBetEth = BigDecimal.valueOf(currentBetEth).add(BigDecimal.valueOf(0.001)).doubleValue();
        betEthSubject.onNext(currentBetEth);
    }

    public void betMinus() {
        if (currentBetEth <= rxSlotRoom.getSlotRoom().getMinBet()) {
            return;
        }
        currentBetEth = BigDecimal.valueOf(currentBetEth).subtract(BigDecimal.valueOf(0.001)).doubleValue();
        Log.i(TAG, "current bet : " + currentBetEth);
        betEthSubject.onNext(currentBetEth);
    }

    public void maxBet() {
        double playerBal = Convert.fromWei(rxSlotRoom.getSlotRoom().getPlayerBalance(), Convert.Unit.ETHER).doubleValue();
        double slotMaxBet = rxSlotRoom.getSlotRoom().getMaxBet();
        double slotMinBet = rxSlotRoom.getSlotRoom().getMinBet();
        Log.i(TAG, "max bet : " + slotMaxBet);
        Log.i(TAG, "min bet : " + slotMinBet);

        int line;
        double bet;
        if (playerBal <= Constants.BET_MAX_LINE * slotMinBet) {
            int maxLine = (int) (playerBal / slotMinBet);
            line = maxLine == 0 ? 1 : maxLine;
            bet = slotMinBet;
        } else if (playerBal <= Constants.BET_MAX_LINE * slotMaxBet) {
            line = Constants.BET_MAX_LINE;
            int betCount = (int) (playerBal / line / slotMinBet);
            bet = (betCount == 0 ? 1 : betCount) * slotMinBet;
        } else {
            line = Constants.BET_MAX_LINE;
            bet = slotMaxBet;
        }
        currentLine = line;
        currentBetEth = bet;
        Log.i(TAG, "current line : " + currentLine);
        Log.i(TAG, "current bet : " + currentBetEth);
        betLineSubject.onNext(currentLine);
        betEthSubject.onNext(currentBetEth);
    }

    public boolean isBanker() {
        return AccountProvider.identical(rxSlotRoom.getSlotRoom().getBankerAddress());
    }

    public boolean isTest() {
        return true;
    }

    private void setGameEvents() {
        setOccupiedEvent();
        setGameInitializedEvent();
        setBankerSeedSetEvent();
        setGameConfirmedEvent();
        setPlayerLeftEvent();
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

    public void initGame() {
        double playerBalance = Convert.fromWei(rxSlotRoom.getSlotRoom().getPlayerBalance(), Convert.Unit.ETHER).doubleValue();
        double currentBet = currentLine * currentBetEth;
        if (playerBalance < currentBet) {
            balanceInsufficient.onNext(true);
            spinStarted = false;
            return;
        }

        spinStarted = true;
        startSpin.onNext(true);
        if ("test".equals(rxSlotRoom.getSlotAddress())) {
            return;
        }

        machine
                .initialBankerSeedReady()
                .subscribe(bool -> {
                    if (!bool.getValue()) {
                        Log.e(TAG, "banker seed is not initialized yet");
                        return;
                    }
                    int index = playerSeed.getIndex();
                    stopWatch.onNext(true);
                    machine
                            .initGameForPlayer(
                                    new Uint256(Convert.toWei(currentBetEth, Convert.Unit.ETHER)),
                                    new Uint8(currentLine),
                                    new Uint8(index))
                            .subscribe(o -> updateTxConfirmation(true, index), Throwable::printStackTrace);
                    updateTxConfirmation(false, index);
                }, e -> {
                    if (e instanceof InsufficientFundException) {
                        fundInsufficient.onNext(true);
                    }
                    e.printStackTrace();
                });
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
                                    }, e -> {
                                        if (e instanceof InsufficientFundException) {
                                            fundInsufficient.onNext(true);
                                        }
                                        e.printStackTrace();
                                    }));
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setGameConfirmedEvent() {
        Disposable disposable = machine
                .gameConfirmedEventObservable()
                .distinct(response -> response.randomSeed)
                .subscribe(response -> {
                    BigInteger reward = response.reward.getValue();
                    int winRate = reward.divide(Convert.toWei(previousBetEth, Convert.Unit.ETHER)).intValue();
                    int index = response.idx.getValue().intValue();

                    Log.i(TAG, "reward : " + reward);
                    Log.i(TAG, "bet : " + previousBetEth);
                    Log.i(TAG, "win rate : " + winRate);
                    Log.i(TAG, "idx : " + index);
                    Log.i(TAG, "random seed : " + Utils.byteToHex(response.randomSeed.getValue()));

                    stopWatch.onNext(false);
                    drawResultSubject.onNext(new DrawOption(winRate, previousBetEth, (index + 1) % 3));

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

                        seedReadySubject.onNext(false);
                        clearSpin.onNext(true);
                    } else {
                        playerKicked.onNext(true);
                    }
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    public void gameOccupy(Double deposit) {
        machine
                .initialPlayerSeedReady()
                .filter(ready -> !ready.getValue())
                .observeOn(Schedulers.computation())
                .map(ready -> playerSeed.getInitialSeed())
                .flatMap(initialSeeds -> {
                    stopWatch.onNext(true);
                    waitBankerInitialSeeds();
                    return machine.occupy(initialSeeds, Convert.toWei(deposit, Convert.Unit.ETHER));
                })
                .map(receipt -> machine.getGameOccupiedEvents(receipt))
                .subscribe(response -> {
                    if (response.isEmpty() || !AccountProvider.identical(response.get(0).player.toString())) {
                        alreadyOccupied.onNext(true);
                        return;
                    }
                    playerSeed.save(machine.getContractAddress());
                }, e -> {
                    if (e instanceof InsufficientFundException) {
                        fundInsufficient.onNext(true);
                    }
                    e.printStackTrace();
                });
    }

    public void updateTxConfirmation(boolean confirm, int index) {
        txConfirmation[index] = confirm;
        txConfirmationSubject.onNext(txConfirmation);
    }

    public void updateBalance() {
        rxSlotRoom.updateBalance();
    }

    public void onCreate(Double deposit) {
        machine = SlotMachine.load(rxSlotRoom.getSlotAddress());
        setGameEvents();

        if (isBanker()) {
            return;
        }

        if (deposit != null) {
            gameOccupy(deposit);
            return;
        }

        PlayerSeed.load(rxSlotRoom.getSlotAddress())
                .subscribe(seed -> {
                    playerSeed = (PlayerSeed) seed;
                    waitBankerInitialSeeds();
                }, Throwable::printStackTrace);
    }

    private void waitBankerInitialSeeds() {
        if (playerSeed.isBankerSeedValid()) {
            seedReadySubject.onNext(true);
            return;
        }
        Disposable disposable = Observable.interval(1, TimeUnit.SECONDS)
                .flatMap(n -> machine.initialBankerSeedReady())
                .filter(Bool::getValue)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(b -> Observable.zip(
                        machine.previousBankerSeed(new Uint256(0)),
                        machine.previousBankerSeed(new Uint256(1)),
                        machine.previousBankerSeed(new Uint256(2)),
                        (byte0, byte1, byte2) -> {
                            String seed0 = Utils.byteToHex(byte0.getValue());
                            String seed1 = Utils.byteToHex(byte1.getValue());
                            String seed2 = Utils.byteToHex(byte2.getValue());
                            if (Utils.isEmpty(seed0) || Utils.isEmpty(seed1) || Utils.isEmpty(seed2)) {
                                Log.e(TAG, "banker initial seeds invalid...");
                                invalidSeedFound.onNext("invalid");
                                return false;
                            }
                            Log.i(TAG, "banker initial seed1 : " + seed0);
                            Log.i(TAG, "banker initial seed2 : " + seed1);
                            Log.i(TAG, "banker initial seed2 : " + seed2);

                            playerSeed.setBankerSeeds(seed0, seed1, seed2);
                            playerSeed.save(machine.getContractAddress());
                            Utils.showToast("banker seed initialized.\npreparing the game...");
                            return true;
                        }))
                .filter(b -> b)
                .delay(10, TimeUnit.SECONDS)
                .subscribe(o -> seedReadySubject.onNext(true), Throwable::printStackTrace);

        compositeDisposable.add(disposable);
    }

    public void onDestroy() {
        Log.i(TAG, "destroy... clear all events...");
        compositeDisposable.clear();

        if ("test".equals(rxSlotRoom.getSlotAddress())) {
            return;
        }

        machine
                .mPlayer()
                .subscribe(address -> {
                    if (!AccountProvider.identical(address.toString())) {
                        return;
                    }

                    machine
                            .leave()
                            .flatMap(receipt -> machine.mPlayer())
                            .subscribe(o -> {
                            }, e -> {
                                if (e instanceof InsufficientFundException) {
                                    fundInsufficient.onNext(true);
                                }
                                e.printStackTrace();
                            });
                });
        Utils.showToast("Your balance [" + Convert.fromWei(rxSlotRoom.getSlotRoom().getPlayerBalance(), Convert.Unit.ETHER) + "] ETH in the slot has been withdrawn and put into your wallet.");
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
