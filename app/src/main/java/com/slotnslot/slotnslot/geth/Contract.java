package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Receipt;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;

import io.reactivex.Observable;

public abstract class Contract {
    private final String contractBinary;
    private final String contractAddress;
    private BigInteger gasPrice;
    private BigInteger gasLimit;

    protected Contract(String binary, String contractAddress, BigInteger gasPrice, BigInteger gasLimit) {
        this.contractBinary = binary;
        this.contractAddress = contractAddress;

        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    protected Contract(String binary, String contractAddress) {
        this(binary, contractAddress, null, null);
    }

    public String getContractAddress() {
        return contractAddress;
    }

    protected Observable<List<Type>> executeCall(Function function) {
        return TransactionManager.executeCall(
                BigInteger.ZERO,
                BigInteger.ZERO,
                contractAddress,
                FunctionEncoder.encode(function),
                BigInteger.ZERO)
                .map(result -> FunctionReturnDecoder.decode(result, function.getOutputParameters()));
    }

    protected Observable<List<Type>> executeCallMultipleValueReturnObservable(Function function) {
        return executeCall(function)
                .flatMap(types -> {
                    if (types == null) {
                        return Observable.error(new GethException("value is null"));
                    }
                    return Observable.just(types);
                });
    }

    protected <T extends Type> Observable<T> executeCallSingleValueReturnObservable(Function function) {
        return executeCall(function)
                .flatMap(types -> {
                    if (types == null || types.isEmpty()) {
                        return Observable.error(new GethException("value is null"));
                    }
                    return Observable.just((T) types.get(0));
                });
    }

    protected Observable<Receipt> executeTransaction(Function function) {
        return send(contractAddress, FunctionEncoder.encode(function), BigInteger.ZERO, gasPrice, gasLimit);
    }

    protected Observable<Receipt> send(String to, String data, BigInteger value, BigInteger gasPrice, BigInteger gasLimit) {
        return TransactionManager.executeTransaction(gasPrice, gasLimit, to, data, value);
    }

    protected List<EventValues> extractEventParameters(Event event, Receipt transactionReceipt) throws Exception {
        return Utils.extractEventParameters(event, transactionReceipt);
    }

    protected Observable<EventValues> filterLogs(Event event) {
        FilterManager.Filter filter = new FilterManager.Filter(event, contractAddress);
        return FilterManager.filterLogs(filter)
                .map(log -> Utils.extractEventParameters(event, log));
    }

    protected Observable<EventValues> pendingFilterLogs(Event event) {
        FilterManager.Filter filter = new FilterManager.Filter(event, contractAddress);
        return FilterManager.pendingFilterLogs(filter)
                .map(log -> Utils.extractEventParameters(event, log));
    }
}
