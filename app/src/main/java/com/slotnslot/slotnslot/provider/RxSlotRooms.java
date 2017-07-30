package com.slotnslot.slotnslot.provider;

import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.contract.SlotMachineStorage;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.models.SlotRoom;
import com.slotnslot.slotnslot.utils.Convert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import lombok.Getter;

@Getter
public class RxSlotRooms {
    private static final String TAG = RxSlotRooms.class.getSimpleName();

    public static Map<String, RxSlotRoom> rxSlotRoomMap = new HashMap<>();
    public static BehaviorSubject<Map<String, RxSlotRoom>> rxSlotRoomMapSubject = BehaviorSubject.create();

    public static SlotMachineManager slotMachineManager = SlotMachineManager.load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS);
    public static SlotMachineStorage slotMachineStorage;
    public static CompletableSubject slotMachineStorageLoaded = CompletableSubject.create();

    public static int numberOfSlotMachine;
    public static int numberOfBanker;

    private RxSlotRooms() {
    }

    public static RxSlotRoom getSlotRoom(String slotAddress) {
        return rxSlotRoomMap.get(slotAddress);
    }

    public static void init() {
        slotMachineManager.getStorageAddr()
                .subscribe(address -> {
                    slotMachineStorage = SlotMachineStorage.load(address.toString());
                    slotMachineStorageLoaded.onComplete();
                }, Throwable::printStackTrace);
        updateSlotMachines();
    }

    public static void destroy() {
        for (RxSlotRoom rxSlotRoom : rxSlotRoomMap.values()) {
            rxSlotRoom.removeBankerEvent();
        }
        rxSlotRoomMap.clear();
        notifyChange();
    }

    public static void updateSlotMachines() {
        SlotRoom test = new SlotRoom("test", "test", 1.0, 0.15, 1000, 0.001, 0.1);
        addSlot(test);

        slotMachineStorageLoaded.subscribe(() -> {
            slotMachineStorage
                    .totalNumOfSlotMachine()
                    .subscribe(result -> {
                        numberOfSlotMachine = result.getValue().intValue();
                        Log.i(TAG, "total number of slot machines : " + result.getValue());
                    }, Throwable::printStackTrace);

            slotMachineStorage.getNumOfBanker()
                    .flatMap(bankerNum -> {
                        int numberOfBanker = bankerNum.getValue().intValue();
                        RxSlotRooms.numberOfBanker = numberOfBanker;
                        Log.i(TAG, "num of banker : " + numberOfBanker);
                        return slotMachineStorage.getBankerAddresses(numberOfBanker);
                    })
                    .flatMap(bankerAddress -> {
                        Log.i(TAG, "banker address : " + bankerAddress.toString());
                        return slotMachineStorage.getSlotMachineAddresses(bankerAddress);
                    })
                    .subscribe(bankerSlotMachineResponse -> {
                        String bankerAddress = bankerSlotMachineResponse.bankerAddress.toString();
                        String slotAddress = bankerSlotMachineResponse.slotMachineAddress.toString();
                        Log.i(TAG, "banker address : " + bankerAddress + " , slot address : " + slotAddress);

                        SlotMachine.load(slotAddress).getInfo()
                                .subscribe(response -> {
                                    SlotRoom slotRoom = new SlotRoom(
                                            slotAddress,
                                            slotAddress,
                                            Convert.fromWei(response.bankerBalance.getValue(), Convert.Unit.ETHER).doubleValue(),
                                            response.mDecider.getValue().intValue() / 1000.0,
                                            response.mMaxPrize.getValue().intValue(),
                                            Convert.fromWei(response.mMinBet.getValue(), Convert.Unit.ETHER).doubleValue(),
                                            Convert.fromWei(response.mMaxBet.getValue(), Convert.Unit.ETHER).doubleValue()
                                    );

                                    slotRoom.setBankerAddress(bankerAddress);
                                    slotRoom.setBankerBalance(response.bankerBalance.getValue());

                                    addSlot(slotRoom);
                                }, Throwable::printStackTrace);
                    }, Throwable::printStackTrace);
        });
    }

    public static void addSlot(SlotRoom slotRoom) {
        RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
        rxSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        notifyChange();
    }

    public static void addSlots(List<SlotRoom> slotRoomList) {
        for (SlotRoom slotRoom : slotRoomList) {
            RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
            rxSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        }
        notifyChange();
    }

    private static void notifyChange() {
        rxSlotRoomMapSubject.onNext(rxSlotRoomMap);
    }
}
