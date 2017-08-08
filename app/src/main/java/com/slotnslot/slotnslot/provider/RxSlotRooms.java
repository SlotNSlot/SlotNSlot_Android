package com.slotnslot.slotnslot.provider;

import android.util.Log;

import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.contract.SlotMachineStorage;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.SlotRoom;
import com.slotnslot.slotnslot.utils.Convert;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import lombok.Getter;

@Getter
public class RxSlotRooms {
    private static final String TAG = RxSlotRooms.class.getSimpleName();

    public static Map<String, RxSlotRoom> rxSlotRoomMap = new HashMap<>();
    public static BehaviorSubject<Map<String, RxSlotRoom>> rxSlotRoomMapSubject = BehaviorSubject.create();

    public static Map<String, RxSlotRoom> rxMakeSlotRoomMap = new HashMap<>();
    public static BehaviorSubject<Map<String, RxSlotRoom>> rxMakeSlotRoomMapSubject = BehaviorSubject.create();

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

    public static RxSlotRoom getMakeSlotRoom(String slotAddress) {
        return rxMakeSlotRoomMap.get(slotAddress);
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
        for (RxSlotRoom rxSlotRoom : rxMakeSlotRoomMap.values()) {
            rxSlotRoom.removeBankerEvent();
        }

        rxSlotRoomMap.clear();
        notifyChange();

        rxMakeSlotRoomMap.clear();
        notifyMakeSlotChange();
    }

    public static void updateSlotMachines() {
        SlotRoom test = new SlotRoom("test", "test", 0.15, 1000, 0.001, 0.1, "test", Convert.toWei(1, Convert.Unit.ETHER));
        addSlot(test);

        slotMachineStorageLoaded.subscribe(() -> {
            slotMachineStorage
                    .totalNumOfSlotMachine()
                    .subscribe(result -> {
                        numberOfSlotMachine = result.getValue().intValue();
                        Log.i(TAG, "total number of slot machines : " + result.getValue());
                    }, Throwable::printStackTrace);

            slotMachineStorage
                    .getLengthOfSlotMachinesArray()
                    .flatMap(length -> {
                        int slotLength = length.getValue().intValue();
                        Log.i(TAG, "length of slot machine array : " + slotLength);
                        return slotMachineStorage.getSlotMachinesArray(new Uint256(0), new Uint256(slotLength - 1));
                    })
                    .flatMap(dynamicArray -> Observable.fromIterable(dynamicArray.getValue()))
                    .filter(address -> Utils.isValidAddress(address.toString()))
                    .flatMap(RxSlotRooms::createSlotRoom)
                    .filter(slotRoom -> !AccountProvider.identical(slotRoom.getBankerAddress()))
                    .subscribe(RxSlotRooms::addSlot, Throwable::printStackTrace);

            slotMachineStorage
                    .getSlotMachines(new Address(AccountProvider.getAccount().getAddressHex()))
                    .flatMap(dynamicArray -> Observable.fromIterable(dynamicArray.getValue()))
                    .filter(address -> Utils.isValidAddress(address.toString()))
                    .flatMap(RxSlotRooms::createSlotRoom)
                    .subscribe(RxSlotRooms::addMakeSlot, Throwable::printStackTrace);
        });
    }

    private static Observable<SlotRoom> createSlotRoom(Address address) {
        String slotAddress = address.toString();
        SlotMachine machine = SlotMachine.load(slotAddress);

        Observable<SlotMachine.GetInfoResponse> infoOb = machine.getInfo();
        Observable<Address> bankerAddressOb = machine.owner();
        Observable<Bytes16> nameOb = machine.mName();
        Observable<Bool> availableOb = machine.mAvailable();
        Observable<Bool> bankruptOb = machine.mBankrupt();
        Observable<Address> playerOb = machine.mPlayer();
        return Observable
                .zip(infoOb, bankerAddressOb, nameOb, availableOb, bankruptOb, playerOb, (info, bankerAddress, name, available, bankrupt, player) -> {
                    SlotRoom slotRoom = new SlotRoom(
                            slotAddress,
                            Utils.byteToString(name.getValue()),
                            info.mDecider.getValue().intValue() / 1000.0,
                            info.mMaxPrize.getValue().intValue(),
                            Convert.fromWei(info.mMinBet.getValue(), Convert.Unit.ETHER).doubleValue(),
                            Convert.fromWei(info.mMaxBet.getValue(), Convert.Unit.ETHER).doubleValue(),
                            bankerAddress.toString(),
                            info.bankerBalance.getValue()
                    );
                    slotRoom.setAvailable(available.getValue());
                    slotRoom.setBankrupt(bankrupt.getValue());
                    slotRoom.setPlayerAddress(player.toString());
                    return slotRoom;
                });
    }

    public static void addSlot(SlotRoom slotRoom) {
        if (rxSlotRoomMap.containsKey(slotRoom.getAddress())) {
            return;
        }
        RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
        rxSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        notifyChange();
    }

    public static void addSlots(List<SlotRoom> slotRoomList) {
        for (SlotRoom slotRoom : slotRoomList) {
            if (rxSlotRoomMap.containsKey(slotRoom.getAddress())) {
                continue;
            }
            RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
            rxSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        }
        notifyChange();
    }

    private static void notifyChange() {
        rxSlotRoomMapSubject.onNext(rxSlotRoomMap);
    }

    public static void addMakeSlot(SlotRoom slotRoom) {
        if (rxMakeSlotRoomMap.containsKey(slotRoom.getAddress())) {
            return;
        }
        RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
        rxMakeSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        notifyMakeSlotChange();
    }

    public static void addMakeSlots(List<SlotRoom> slotRoomList) {
        for (SlotRoom slotRoom : slotRoomList) {
            if (rxMakeSlotRoomMap.containsKey(slotRoom.getAddress())) {
                continue;
            }
            RxSlotRoom rxSlotRoom = new RxSlotRoom(slotRoom);
            rxMakeSlotRoomMap.put(slotRoom.getAddress(), rxSlotRoom);
        }
        notifyMakeSlotChange();
    }

    public static void removeMakeSlot(String address) {
        RxSlotRoom makeSlot = rxMakeSlotRoomMap.get(address);
        makeSlot.removeBankerEvent();
        rxMakeSlotRoomMap.remove(address);
        notifyMakeSlotChange();
    }

    private static void notifyMakeSlotChange() {
        rxMakeSlotRoomMapSubject.onNext(rxMakeSlotRoomMap);
    }
}
