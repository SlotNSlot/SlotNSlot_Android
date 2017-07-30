package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.ethereum.geth.Receipt;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class Hello extends Contract {
    private static final String BINARY = "606060405234610000576040516020806101fa833981016040528080519060200190919050505b806000819055505b505b6101bb8061003f6000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063257c1b7d1461005f5780639c8944481461007c578063bb806dc9146100ad578063c5348f24146100d0575b610000565b346100005761007a6004808035906020019091905050610101565b005b3461000057610097600480803590602001909190505061013c565b6040518082815260200191505060405180910390f35b34610000576100ba610147565b6040518082815260200191505060405180910390f35b34610000576100eb600480803590602001909190505061014d565b6040518082815260200191505060405180910390f35b7f24abdb5865df5079dcc5ac590ff6f01d5c16edbc5fab4e195d9febd1114503da816040518082815260200191505060405180910390a15b50565b60008190505b919050565b60005481565b60007f24abdb5865df5079dcc5ac590ff6f01d5c16edbc5fab4e195d9febd1114503da826040518082815260200191505060405180910390a18190505b9190505600a165627a7a72305820c41d277e0cae968dd5854df82b1087f520d61e1263a433832bbd7fd98cd423460029";

    private Hello(String contractAddress) {
        super(BINARY, contractAddress);
    }

    public static Hello load(String contractAddress) {
        return new Hello(contractAddress);
    }

    public List<PrintEventResponse> getPrintEvents(Receipt transactionReceipt) throws Exception {
        final Event event = new Event(
                "Print",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));

        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<PrintEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            PrintEventResponse printEventResponse = new PrintEventResponse();
            printEventResponse.out = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(printEventResponse);
        }
        return responses;
    }

    public Observable<PrintEventResponse> printEventObservable() {
        final Event event = new Event(
                "Print",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));

        return filterLogs(event)
                .map(eventValues -> {
                    PrintEventResponse typedResponse = new PrintEventResponse();
                    typedResponse.out = (Uint256) eventValues.getNonIndexedValues().get(0);
                    return typedResponse;
                });
    }

    public Observable<Receipt> say1(Uint256 input) {
        Function function = new Function("say1", Collections.singletonList(input), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Uint256> say2(Uint256 input) {
        Function function = new Function(
                "say2",
                Collections.singletonList(input),
                Collections.singletonList(new TypeReference<Uint256>() {
                })
        );

        return executeCallSingleValueReturnObservable(function);
    }

    public static class PrintEventResponse {
        public Uint256 out;
    }
}
