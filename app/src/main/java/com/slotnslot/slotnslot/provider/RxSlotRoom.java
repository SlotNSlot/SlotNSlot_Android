package com.slotnslot.slotnslot.provider;

import android.text.TextUtils;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.models.SlotRoom;

import org.web3j.abi.datatypes.Address;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import lombok.Getter;

@Getter
public class RxSlotRoom {
    private final String slotAddress;
    private SlotRoom slotRoom;
    private BehaviorSubject<SlotRoom> slotRoomSubject = BehaviorSubject.create();

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
}
