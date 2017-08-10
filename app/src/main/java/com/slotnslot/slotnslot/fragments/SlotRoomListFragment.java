package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slotnslot.slotnslot.ListType;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.adapters.SlotListAdapter;
import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.RxSlotRooms;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.SlotUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class SlotRoomListFragment extends SlotRootFragment {

    @BindView(R.id.swipe_reffresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private SlotListAdapter adapter;
    private ArrayList<SlotRoomViewModel> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slot_room_list, container, false);
        ButterKnife.bind(this, view);

        adapter = new SlotListAdapter(items, (ListType) getArguments().get(Constants.BUNDLE_KEY_LIST_TYPE), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpacesItemDecoration(SlotUtil.convertDpToPixel(7f, getContext())));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::refreshItems);
        swipeRefreshLayout.setColorSchemeResources(R.color.pink1);

        setItemList();
        return view;
    }

    void refreshItems() {
        RxSlotRooms.updatePlaySlotMachines();
        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((i) -> onItemsLoadComplete());
    }

    void onItemsLoadComplete() {
        swipeRefreshLayout.setRefreshing(false);
    }

    abstract void setItemList();

    public SlotListAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<SlotRoomViewModel> getItems() {
        return items;
    }
}
