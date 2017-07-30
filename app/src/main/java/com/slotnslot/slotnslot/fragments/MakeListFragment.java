package com.slotnslot.slotnslot.fragments;

import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;

import java.util.ArrayList;
import java.util.List;

public class MakeListFragment extends SlotRoomListFragment {
    @Override
    void setItemList() {
        RxSlotRooms.rxSlotRoomMapSubject
                .subscribe(rxSlotRoomMap -> {
                    getItems().clear();
                    List<SlotRoomViewModel> makeList = new ArrayList<>();
                    for (RxSlotRoom slotRoom : rxSlotRoomMap.values()) {
                        if (AccountProvider.identical(slotRoom.getSlotRoom().getBankerAddress())) {
                            SlotRoomViewModel model = new SlotRoomViewModel(slotRoom.getSlotAddress());
                            makeList.add(model);
                        }
                    }
                    getItems().addAll(makeList);
                    getAdapter().notifyDataSetChanged();
                });
    }
}