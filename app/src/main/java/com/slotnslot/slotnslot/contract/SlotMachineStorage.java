package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Observable;

public final class SlotMachineStorage extends Contract {
    private static final String BINARY = "0x";

    private SlotMachineStorage(String contractAddress) {
        super(BINARY, contractAddress);
    }

    public static SlotMachineStorage load(String contractAddress) {
        return new SlotMachineStorage(contractAddress);
    }

    /**
     * variables
     **/
    public Observable<Address> bankerAddress(Uint256 param0) {
        Function function = new Function(
                "bankerAddress",
                Collections.singletonList(param0),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> totalNumOfSlotMachine() {
        Function function = new Function(
                "totalNumOfSlotMachine",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    /**
     * methods
     **/
    public Observable<Bool> isValidBanker(Address _banker) {
        Function function = new Function(
                "isValidBanker",
                Collections.singletonList(_banker),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> getNumOfBanker() {
        Function function = new Function(
                "getNumOfBanker",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> getNumOfSlotMachine(Address _banker) {
        Function function = new Function(
                "getNumOfSlotMachine",
                Collections.singletonList(_banker),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Address> getSlotMachine(Address _banker, Uint256 _idx) {
        Function function = new Function(
                "getSlotMachine",
                Arrays.asList(_banker, _idx),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<DynamicArray<Address>> getSlotMachinesArray(Uint256 from, Uint256 to) {
        Function function = new Function(
                "getSlotMachinesArray",
                Arrays.asList(from, to),
                Collections.singletonList(new TypeReference<DynamicArray<Address>>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<DynamicArray<Address>> getSlotMachines(Address _banker) {
        Function function = new Function(
                "getSlotMachines",
                Collections.singletonList(_banker),
                Collections.singletonList(new TypeReference<DynamicArray<Address>>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> getLengthOfSlotMachinesArray() {
        Function function = new Function(
                "getLengthOfSlotMachinesArray",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

}

