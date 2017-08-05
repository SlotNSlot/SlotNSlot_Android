package com.slotnslot.slotnslot.fragments;

import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class MakeListFragment extends SlotRoomListFragment {
    @Override
    void setItemList() {
        RxSlotRooms.rxMakeSlotRoomMapSubject
                .compose(bindToLifecycle())
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxSlotRoomMap -> {
                    getItems().clear();
                    List<SlotRoomViewModel> makeList = new ArrayList<>();
                    for (RxSlotRoom slotRoom : rxSlotRoomMap.values()) {
                        SlotRoomViewModel model = new SlotRoomViewModel(slotRoom);
                        makeList.add(model);

                        slotRoom.setBankerEvent();
                    }
                    getItems().addAll(makeList);
                    getAdapter().notifyDataSetChanged();
                });
    }
}