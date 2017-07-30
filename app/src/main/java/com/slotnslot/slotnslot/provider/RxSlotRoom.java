package com.slotnslot.slotnslot.provider;

import android.text.TextUtils;
import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.Seed;
import com.slotnslot.slotnslot.models.SlotRoom;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import lombok.Getter;

@Getter
public class RxSlotRoom {
    private static final String TAG = RxSlotRoom.class.getSimpleName();

    private final String slotAddress;
    private SlotRoom slotRoom;
    private BehaviorSubject<SlotRoom> slotRoomSubject = BehaviorSubject.create();

    private SlotMachine machine;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean bankerEventInitialized = false;
    private Seed bankerSeed = new Seed();

    public RxSlotRoom(SlotRoom slotRoom) {
        this.slotAddress = slotRoom.getAddress();
        this.slotRoom = slotRoom;
        this.slotRoomSubject.onNext(slotRoom);
    }

    private void notifyChange() {
        this.slotRoomSubject.onNext(slotRoom);
    }

    public void updateSlotRoom(SlotRoom slotRoom) {
        this.slotRoom = slotRoom;
        this.slotRoomSubject.onNext(slotRoom);
    }

    public Observable<Address> updatePlayerObservable() {
        if (slotRoom == null || TextUtils.isEmpty(slotAddress)) {
            return null;
        }

        SlotMachine machine = SlotMachine.load(slotAddress);
        return machine.mPlayer().map(player -> {
            slotRoom.setPlayerAddress(player.toString());
            notifyChange();
            return player;
        });
    }

    public void updateBalance() {
        if (slotRoom == null || TextUtils.isEmpty(slotAddress)) {
            return;
        }

        SlotMachine machine = SlotMachine.load(slotAddress);
        machine.playerBalance()
                .subscribe(playerBalance -> {
                    slotRoom.setPlayerBalance(playerBalance.getValue());
                    notifyChange();
                });
        machine.bankerBalance()
                .subscribe(bankerBalance -> {
                    slotRoom.setBankerBalance(bankerBalance.getValue());
                    notifyChange();
                });
    }

    public void setBankerEvent() {
        if (bankerEventInitialized) {
            return;
        }
        bankerEventInitialized = true;
        machine = SlotMachine.load(slotAddress);

        setOccupiedEvent();
        setBankerSeedInitializedEvent();
        setGameInitializedEvent();
        setBankerSeedSetEvent();
        setGameConfirmedEvent();
        setPlayerLeftEvent();
    }

    public void removeBankerEvent() {
        if (!bankerEventInitialized) {
            return;
        }
        compositeDisposable.clear();
        bankerEventInitialized = false;
    }

    private void setPlayerLeftEvent() {
        Disposable disposable = machine
                .playerLeftEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "player left : " + response.player.toString());
                    Log.i(TAG, "player's initial balance: " + response.playerBalance.getValue());
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setGameConfirmedEvent() {
        Disposable disposable = machine
                .gameConfirmedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "reward : " + response.reward.getValue());
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    bankerSeed.nextRound();
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setBankerSeedSetEvent() {
        Disposable disposable = machine
                .bankerSeedSetEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "banker seed : " + Utils.byteToHex(response.bankerSeed.getValue()));
                    Log.i(TAG, "idx : " + response.idx.getValue());
                });
        compositeDisposable.add(disposable);
    }

    private void setGameInitializedEvent() {
        Disposable disposable = machine
                .gameInitializedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "player address : " + response.player.toString());
                    Log.i(TAG, "bet : " + response.bet.getValue());
                    Log.i(TAG, "lines : " + response.lines.getValue());
                    Log.i(TAG, "idx : " + response.idx.getValue());

                    machine
                            .setBankerSeed(bankerSeed.getSeed(), new Uint256(bankerSeed.getIndex()))
                            .subscribe();
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setBankerSeedInitializedEvent() {
        Disposable disposable = machine
                .bankerSeedInitializedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "banker seed initialized.");
                    Log.i(TAG, "banker seed1 : " + Utils.byteToHex(response._bankerSeed.getValue().get(0).getValue()));
                    Log.i(TAG, "banker seed2 : " + Utils.byteToHex(response._bankerSeed.getValue().get(1).getValue()));
                    Log.i(TAG, "banker seed3 : " + Utils.byteToHex(response._bankerSeed.getValue().get(2).getValue()));
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void setOccupiedEvent() {
        Disposable disposable = machine
                .gameOccupiedEventObservable()
                .subscribe(response -> {
                    Log.i(TAG, "game occupied by : " + response.player.toString());
                    Log.i(TAG, "player seed1 : " + Utils.byteToHex(response.playerSeed.getValue().get(0).getValue()));
                    Log.i(TAG, "player seed2 : " + Utils.byteToHex(response.playerSeed.getValue().get(1).getValue()));
                    Log.i(TAG, "player seed3 : " + Utils.byteToHex(response.playerSeed.getValue().get(2).getValue()));

                    machine
                            .initBankerSeed(bankerSeed.getInitialSeed())
                            .subscribe();
                }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }
}
