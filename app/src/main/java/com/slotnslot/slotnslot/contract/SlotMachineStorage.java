package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
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
                "bankeraddress",
                Collections.singletonList(param0),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Address> slotMachines(Address param0, Uint256 param1) {
        Function function = new Function(
                "slotMachines",
                Arrays.asList(param0, param1),
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

    public Observable<Address> getBankerAddresses(int numberOfBanker) {
        return Observable.create(e -> {
            for (int i = 0; i < numberOfBanker; i++) {
                bankerAddress(new Uint256(i))
                        .subscribe(
                                address -> {
                                    if (!e.isDisposed()) {
                                        e.onNext(address);
                                    }
                                },
                                Throwable::printStackTrace);
            }
        });
    }

    public Observable<BankerSlotMachineResponse> getSlotMachineAddresses(Address bankerAddress) {
        return Observable.create(e -> getNumOfSlotMachine(bankerAddress)
                .subscribe(numberOfSlotMachine -> {
                    int slotNum = numberOfSlotMachine.getValue().intValue();
                    System.out.println("banker " + bankerAddress.toString() + " has " + slotNum + " slot machines");
                    for (int i = 0; i < slotNum; i++) {
                        getSlotMachine(bankerAddress, new Uint256(i))
                                .subscribe(slotAddress -> {
                                            if (!e.isDisposed()) {
                                                BankerSlotMachineResponse response = new BankerSlotMachineResponse();
                                                response.bankerAddress = bankerAddress;
                                                response.slotMachineAddress = slotAddress;
                                                e.onNext(response);
                                            }
                                        },
                                        Throwable::printStackTrace);
                    }
                }));
    }

    public static class BankerSlotMachineResponse {
        public Address bankerAddress;
        public Address slotMachineAddress;
    }
}

