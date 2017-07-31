package com.slotnslot.slotnslot.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baoyz.actionsheet.ActionSheet;
import com.slotnslot.slotnslot.ListType;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.SlotType;
import com.slotnslot.slotnslot.activities.MakeSlotActivity;
import com.slotnslot.slotnslot.activities.SlotGameActivity;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.views.SlotImageViewHolder;
import com.slotnslot.slotnslot.views.SlotMakeViewHolder;
import com.slotnslot.slotnslot.views.SlotViewHolder;

import java.util.ArrayList;

public class SlotListAdapter extends RecyclerView.Adapter {
    public static final String TAG = SlotListAdapter.class.getSimpleName();

    static final int SLOT_ITEM_VIEWTYPE_ROOM = 0;
    static final int SLOT_ITEM_VIEWTYPE_ROOM_MAKE = 1;
    static final int SLOT_ITEM_VIEWTYPE_IMGAE = 2;

    private ArrayList<SlotRoomViewModel> items = new ArrayList<>();
    private ListType type;
    private Fragment fragment;

    public SlotListAdapter(ArrayList<SlotRoomViewModel> items, ListType type, Fragment fragment) {
        this.items = items;
        this.type = type;
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case SLOT_ITEM_VIEWTYPE_ROOM:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.slot_recycler_item, parent, false);
                return new SlotViewHolder(itemView);
            case SLOT_ITEM_VIEWTYPE_ROOM_MAKE:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.slot_recycler_make, parent, false);
                itemView.setOnClickListener(view1 -> {
                    Intent intent = new Intent(fragment.getContext(), MakeSlotActivity.class);
                    fragment.getContext().startActivity(intent);
                });
                return new SlotMakeViewHolder(itemView);
            case SLOT_ITEM_VIEWTYPE_IMGAE:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.slot_recycler_image, parent, false);
                return new SlotImageViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case SLOT_ITEM_VIEWTYPE_ROOM:
                SlotRoomViewModel viewModel = items.get(position - (type == ListType.PLAY ? 1 : 0));
                ((SlotViewHolder) holder).onBindView(viewModel);
                ((SlotViewHolder) holder).getMoreButton().setOnClickListener(v -> {
                    ActionSheet.createBuilder(fragment.getContext(), fragment.getFragmentManager())
                            .setCancelButtonTitle("Cancel")
                            .setOtherButtonTitles("Delete")
                            .setCancelableOnTouchOutside(true)
                            .setListener(new ActionSheet.ActionSheetListener() {
                                @Override
                                public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

                                }

                                @Override
                                public void onOtherButtonClick(ActionSheet actionSheet, int index) {
                                    //TODO DELETE
                                    actionSheet.dismiss();
                                }
                            }).show();
                });
                holder.itemView.setOnClickListener(view -> {
                    SlotRoomViewModel slotRoomViewModel = items.get(position - (type == ListType.PLAY ? 1 : 0));
                    enterSlotRoom(slotRoomViewModel);
                });
        }
    }

    private void enterSlotRoom(SlotRoomViewModel viewModel) {
        if ("test".equals(viewModel.getSlotAddress())) {
            Intent intent = new Intent(fragment.getContext(), SlotGameActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.ACTIVITY_EXTRA_KEY_SLOT_TYPE, type == ListType.PLAY ? SlotType.PLAYER : SlotType.BANKER);
            bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM, viewModel.getRxSlotRoom().getSlotAddress());
            intent.putExtras(bundle);
            fragment.getContext().startActivity(intent);
            return;
        }

        viewModel
                .getRxSlotRoom()
                .updatePlayerObservable()
                .map(player -> {
                    if (AccountProvider.identical(viewModel.getBankerAddress())) {
                        return true; // banker can enter the room
                    }
                    if (!Utils.isValidAddress(player.toString())) {
                        return true; // if player address not exist, user can enter
                    }
                    if (AccountProvider.identical(player.toString())) {
                        return true; // if player address equals user address, user can re-enter
                    }
                    return false;
                })
                .subscribe(
                        allowedToEnter -> {
                            if (!allowedToEnter) {
                                Log.i(TAG, "slot machine is already occupied by other user : " + viewModel.getPlayerAddress());
                                return;
                            }

                            Intent intent = new Intent(fragment.getContext(), SlotGameActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM, viewModel.getSlotAddress());
                            intent.putExtras(bundle);
                            fragment.getContext().startActivity(intent);
                        },
                        Throwable::printStackTrace);
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        switch (type) {
            case PLAY:
                return position == 0 ? SLOT_ITEM_VIEWTYPE_IMGAE : SLOT_ITEM_VIEWTYPE_ROOM;
            case MAKE:
                return items.size() > 0 ?
                        (position == items.size() ?
                                SLOT_ITEM_VIEWTYPE_ROOM_MAKE : SLOT_ITEM_VIEWTYPE_ROOM)
                        : SLOT_ITEM_VIEWTYPE_ROOM_MAKE;
        }
        return super.getItemViewType(position);
    }
}
