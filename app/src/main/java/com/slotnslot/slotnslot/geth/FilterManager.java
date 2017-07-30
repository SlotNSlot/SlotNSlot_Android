package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Address;
import org.ethereum.geth.Addresses;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.FilterLogsHandler;
import org.ethereum.geth.FilterQuery;
import org.ethereum.geth.Log;
import org.ethereum.geth.Subscription;
import org.ethereum.geth.Topics;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FilterManager {

    private static final int DEFAULT_BUFFER_SIZE = 16;

    private FilterManager() {
    }

    public static Observable<Log> filterLogs(Filter filter) {
        return Observable
                .<Log>create(emitter -> {
                    FilterListener filterListener = new FilterListener(emitter);
                    filterListener.subscription = GethManager.getClient().subscribeFilterLogs(
                            GethManager.getMainContext(),
                            filter.query,
                            filterListener,
                            DEFAULT_BUFFER_SIZE);
                })
                .throttleFirst(10, TimeUnit.SECONDS) // sometimes it emits event several times
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static class FilterListener implements FilterLogsHandler {

        ObservableEmitter<Log> emitter;
        Subscription subscription;

        public FilterListener(ObservableEmitter<Log> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onError(String e) {
//            if (emitter.isDisposed()) {
//                subscription.unsubscribe();
//                return;
//            }

            if (!emitter.isDisposed()) {
                emitter.onError(new GethException(e));
            }
        }

        @Override
        public void onFilterLogs(Log log) {
//            if (emitter.isDisposed()) {
//                subscription.unsubscribe();
//                return;
//            }
            if (!emitter.isDisposed()) {
                emitter.onNext(log);
            }
        }
    }

    public static class Filter {
        private FilterQuery query = new FilterQuery();

        public Filter() {
        }

        public Filter(Event event, String address) {
            addTopic(EventEncoder.encode(event));
            addAddress(address);
        }

        public Filter addTopics(List<String> topicList) {
            Topics topics = query.getTopics();
            topics.append(Utils.listToHashes(topicList));
            query.setTopics(topics);
            return this;
        }

        public Filter addTopic(String topic) {
            System.out.println("event topic hash : " + topic);
            return addTopics(Collections.singletonList(topic));
        }

        public Filter addAddress(Address address) {
            Addresses addresses = query.getAddresses();
            addresses.append(address);
            query.setAddresses(addresses);
            return this;
        }

        public Filter setFromBlock(int block) {
            query.setFromBlock(new BigInt(block));
            return this;
        }

        public Filter setToBlock(int block) {
            query.setToBlock(new BigInt(block));
            return this;
        }

        public Filter addAddress(String address) {
            return addAddress(new Address(address));
        }

        public FilterQuery getQuery() {
            return query;
        }
    }
}
