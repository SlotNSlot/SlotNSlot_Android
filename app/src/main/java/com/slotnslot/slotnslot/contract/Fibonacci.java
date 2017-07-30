package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.ethereum.geth.Receipt;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public final class Fibonacci extends Contract {
    private static final String BINARY = "6060604052341561000c57fe5b5b6101798061001c6000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633c7fdc701461004657806361047ff41461007a575bfe5b341561004e57fe5b61006460048080359060200190919050506100ae565b6040518082815260200191505060405180910390f35b341561008257fe5b6100986004808035906020019091905050610100565b6040518082815260200191505060405180910390f35b60006100b982610100565b90507f71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed8282604051808381526020018281526020019250505060405180910390a15b919050565b600060008214156101145760009050610148565b60018214156101265760019050610148565b61013260028303610100565b61013e60018403610100565b019050610148565b5b5b9190505600a165627a7a7230582091fd85753946f3433825f574224534fb097c4837bb3351bc737a0026c1f20ef30029";

    private Fibonacci(String contractAddress) {
        super(BINARY, contractAddress);
    }

    public List<NotifyEventResponse> getNotifyEvents(Receipt transactionReceipt) throws Exception {
        final Event event = new Event(
                "Notify",
                Collections.emptyList(),
                Arrays.asList(
                        new TypeReference<Uint256>() {
                        },
                        new TypeReference<Uint256>() {
                        }
                ));

        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<NotifyEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            NotifyEventResponse typedResponse = new NotifyEventResponse();
            typedResponse.input = (Uint256) eventValues.getNonIndexedValues().get(0);
            typedResponse.result = (Uint256) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<NotifyEventResponse> notifyEventObservable() {
        final Event event = new Event(
                "Notify",
                Collections.emptyList(),
                Arrays.asList(
                        new TypeReference<Uint256>() {
                        },
                        new TypeReference<Uint256>() {
                        }
                ));

        return filterLogs(event)
                .map(eventValues -> {
                    NotifyEventResponse typedResponse = new NotifyEventResponse();
                    typedResponse.input = (Uint256) eventValues.getNonIndexedValues().get(0);
                    typedResponse.result = (Uint256) eventValues.getNonIndexedValues().get(1);
                    return typedResponse;
                });
    }

    public Observable<Receipt> fibonacciNotify(Uint256 number) {
        Function function = new Function("fibonacciNotify", Collections.singletonList(number), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Uint256> fibonacci(Uint256 number) {
        Function function = new Function("fibonacci",
                Collections.singletonList(number),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public static Fibonacci load(String contractAddress) {
        return new Fibonacci(contractAddress);
    }

    public static class NotifyEventResponse {
        public Uint256 input;

        public Uint256 result;
    }
}

