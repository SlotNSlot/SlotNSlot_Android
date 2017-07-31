package com.slotnslot.slotnslot.geth;

import org.ethereum.geth.Address;
import org.ethereum.geth.Addresses;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.FilterLogsHandler;
import org.ethereum.geth.FilterQuery;
import org.ethereum.geth.Log;
import org.ethereum.geth.Logs;
import org.ethereum.geth.Subscription;
import org.ethereum.geth.Topics;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FilterManager {

    private static final int POLLING_PERIOD = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 16;

    private FilterManager() {
    }

    public static Observable<Log> pendingFilterLogs(Filter filter) {
        // pending block setup
        filter.setFromBlock(GethConstants.PENDING_BLOCK);
        filter.setToBlock(GethConstants.PENDING_BLOCK);

        return Observable
                .<Log>create(e -> {
                    FilterPoller poller = new FilterPoller(e, filter);
                    poller.poll();
                })
                .distinct(log -> log.getTxHash().getHex()) // prevent duplicated event occurring
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Log> filterLogs(Filter filter) {
        return Observable
                .<Log>create(emitter -> {
                    FilterSubscriber subscriber = new FilterSubscriber(emitter, filter);
                    subscriber.subscribe();
                })
                .distinct(log -> log.getTxHash().getHex())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static class FilterPoller {
        private final Context context;
        private EthereumClient client;
        private ObservableEmitter<Log> emitter;
        private BigInt filter;

        private Disposable disposable;

        FilterPoller(ObservableEmitter<Log> emitter, Filter filter) throws Exception {
            this.client = GethManager.getClient();
            this.context = GethManager.getMainContext();
            this.emitter = emitter;
            this.filter = this.client.getNewFilter(this.context, filter.query);
        }

        void poll() {
            disposable = Observable.interval(POLLING_PERIOD, TimeUnit.MILLISECONDS)
                    .flatMap(n -> {
                        Logs logs = client.getFilterChanges(context, filter);

                        ArrayList<Log> list = new ArrayList<>();
                        for (int i = 0; i < logs.size(); i++) {
                            list.add(logs.get(i));
                        }
                        return Observable.fromIterable(list);
                    })
                    .subscribe(log -> {
                        if (emitter.isDisposed()) {
                            dispose();
                            return;
                        }
                        emitter.onNext(log);
                    });
        }

        private void dispose() throws Exception {
            client.uninstallFilter(context, filter);
            disposable.dispose();
        }

    }

    private static class FilterSubscriber {

        private final Context context;
        private EthereumClient client;
        private ObservableEmitter<Log> emitter;
        private Filter filter;
        private Subscription subscription;

        FilterSubscriber(ObservableEmitter<Log> emitter, Filter filter) {
            this.client = GethManager.getClient();
            this.context = GethManager.getMainContext();
            this.emitter = emitter;
            this.filter = filter;
        }

        public void subscribe() throws Exception {
            FilterLogsHandler handler = new FilterLogsHandler() {
                @Override
                public void onError(String e) {
//                    if (emitter.isDisposed()) {
//                        subscription.unsubscribe();
//                        return;
//                    }
                    if (!emitter.isDisposed()) {
                        emitter.onError(new GethException(e));
                    }
                }

                @Override
                public void onFilterLogs(Log log) {
//                    if (emitter.isDisposed()) {
//                        subscription.unsubscribe();
//                        return;
//                    }
                    if (!emitter.isDisposed()) {
                        emitter.onNext(log);
                    }

                }
            };

            subscription = client.subscribeFilterLogs(
                    context,
                    filter.query,
                    handler,
                    DEFAULT_BUFFER_SIZE);
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
