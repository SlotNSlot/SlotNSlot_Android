package com.slotnslot.slotnslot.fragments;

import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class PlayListFragment extends SlotRoomListFragment {
    @Override
    void setItemList() {
        RxSlotRooms.rxSlotRoomMapSubject
                .compose(bindToLifecycle())
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxSlotRoomMap -> {
                    getItems().clear();
                    List<SlotRoomViewModel> makeList = new ArrayList<>();
                    for (RxSlotRoom slotRoom : rxSlotRoomMap.values()) {
                        if (slotRoom.getSlotRoom().isBankrupt()) {
                            continue; // do not list bankrupt slot room
                        }
                        if (!slotRoom.getSlotRoom().isOccupied()) {
                            continue; // slot room is already occupied by someone
                        }
                        SlotRoomViewModel model = new SlotRoomViewModel(slotRoom);
                        makeList.add(model);
                    }
                    getItems().addAll(makeList);
                    getAdapter().notifyDataSetChanged();
                });
    }
}