package com.slotnslot.slotnslot.models;

import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;

import io.reactivex.Observable;
import lombok.Getter;

@Getter
public class SlotRoomViewModel {
    private RxSlotRoom rxSlotRoom;

    public Observable<Double> stake;
    public Observable<Integer> playTime;

    public SlotRoomViewModel(String slotRoomAddress) {
        this.rxSlotRoom = RxSlotRooms.getSlotRoom(slotRoomAddress);
        this.stake = rxSlotRoom.getSlotRoomSubject().map(SlotRoom::getStake);
        this.playTime = rxSlotRoom.getSlotRoomSubject().map(SlotRoom::getPlayTime);
    }

    public String getSlotAddress() {
        return rxSlotRoom.getSlotAddress();
    }

    public String getPlayerAddress() {
        return rxSlotRoom.getSlotRoom().getPlayerAddress();
    }

    public String getBankerAddress() {
        return rxSlotRoom.getSlotRoom().getBankerAddress();
    }
}
