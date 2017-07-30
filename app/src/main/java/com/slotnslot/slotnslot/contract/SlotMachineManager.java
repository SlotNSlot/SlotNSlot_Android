package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.ethereum.geth.Receipt;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public final class SlotMachineManager extends Contract {

    private static final String BINARY = "0x";

    private static final Event SLOT_MACHINE_CREATED = new Event(
            "slotMachineCreated",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Uint16>() {
                    },
                    new TypeReference<Uint256>() {
                    },
                    new TypeReference<Uint256>() {
                    },
                    new TypeReference<Uint16>() {
                    },
                    new TypeReference<Uint256>() {
                    },
                    new TypeReference<Address>() {
                    }));
    private static final Event SLOT_MACHINE_REMOVED = new Event(
            "slotMachineRemoved",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Uint256>() {
                    }));

    private SlotMachineManager(String contractAddress) {
        super(BINARY, contractAddress);
    }

    public static SlotMachineManager load(String contractAddress) {
        return new SlotMachineManager(contractAddress);
    }

    /**
     * methods
     **/
    public Observable<Receipt> createSlotMachine(Uint16 _decider, Uint256 _minBet, Uint256 _maxBet, Uint16 _maxPrize) {
        Function function = new Function(
                "createSlotMachine",
                Arrays.asList(_decider, _minBet, _maxBet, _maxPrize),
                Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Receipt> removeSlotMachine(Uint256 _idx) {
        Function function = new Function("removeSlotMachine", Collections.singletonList(_idx), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Address> getStorageAddr() {
        Function function = new Function(
                "getStorageAddr",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    /**
     * events
     **/
    public List<SlotMachineCreatedEventResponse> getSlotMachineCreatedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(SLOT_MACHINE_CREATED, transactionReceipt);
        ArrayList<SlotMachineCreatedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            SlotMachineCreatedEventResponse typedResponse = new SlotMachineCreatedEventResponse();
            typedResponse._banker = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse._decider = (Uint16) eventValues.getNonIndexedValues().get(1);
            typedResponse._minBet = (Uint256) eventValues.getNonIndexedValues().get(2);
            typedResponse._maxBet = (Uint256) eventValues.getNonIndexedValues().get(3);
            typedResponse._maxPrize = (Uint16) eventValues.getNonIndexedValues().get(4);
            typedResponse._totalNum = (Uint256) eventValues.getNonIndexedValues().get(5);
            typedResponse._slotAddr = (Address) eventValues.getNonIndexedValues().get(6);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<SlotMachineCreatedEventResponse> slotMachineCreatedEventObservable() {
        return filterLogs(SLOT_MACHINE_CREATED)
                .map(eventValues -> {
                    SlotMachineCreatedEventResponse typedResponse = new SlotMachineCreatedEventResponse();
                    typedResponse._banker = (Address) eventValues.getNonIndexedValues().get(0);
                    typedResponse._decider = (Uint16) eventValues.getNonIndexedValues().get(1);
                    typedResponse._minBet = (Uint256) eventValues.getNonIndexedValues().get(2);
                    typedResponse._maxBet = (Uint256) eventValues.getNonIndexedValues().get(3);
                    typedResponse._maxPrize = (Uint16) eventValues.getNonIndexedValues().get(4);
                    typedResponse._totalNum = (Uint256) eventValues.getNonIndexedValues().get(5);
                    typedResponse._slotAddr = (Address) eventValues.getNonIndexedValues().get(6);
                    return typedResponse;
                });
    }

    public List<SlotMachineRemovedEventResponse> getSlotMachineRemovedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(SLOT_MACHINE_REMOVED, transactionReceipt);
        ArrayList<SlotMachineRemovedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            SlotMachineRemovedEventResponse typedResponse = new SlotMachineRemovedEventResponse();
            typedResponse._banker = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse._slotAddr = (Address) eventValues.getNonIndexedValues().get(1);
            typedResponse._totalNum = (Uint256) eventValues.getNonIndexedValues().get(2);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<SlotMachineRemovedEventResponse> slotMachineRemovedEventObservable() {
        return filterLogs(SLOT_MACHINE_REMOVED)
                .map(eventValues -> {
                    SlotMachineRemovedEventResponse typedResponse = new SlotMachineRemovedEventResponse();
                    typedResponse._banker = (Address) eventValues.getNonIndexedValues().get(0);
                    typedResponse._slotAddr = (Address) eventValues.getNonIndexedValues().get(1);
                    typedResponse._totalNum = (Uint256) eventValues.getNonIndexedValues().get(2);
                    return typedResponse;
                });
    }

    public static class SlotMachineCreatedEventResponse {
        public Address _banker;
        public Uint16 _decider;
        public Uint256 _minBet;
        public Uint256 _maxBet;
        public Uint16 _maxPrize;
        public Uint256 _totalNum;
        public Address _slotAddr;
    }

    public static class SlotMachineRemovedEventResponse {
        public Address _banker;
        public Address _slotAddr;
        public Uint256 _totalNum;
    }
}

