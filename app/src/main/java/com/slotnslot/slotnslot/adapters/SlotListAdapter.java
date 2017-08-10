package com.slotnslot.slotnslot.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.baoyz.actionsheet.ActionSheet;
import com.slotnslot.slotnslot.ListType;
import com.slotnslot.slotnslot.MainApplication;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.SlotType;
import com.slotnslot.slotnslot.activities.MakeSlotActivity;
import com.slotnslot.slotnslot.activities.SlotGameActivity;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.SlotRoomViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRooms;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.views.SlotDescriptionViewHolder;
import com.slotnslot.slotnslot.views.SlotMakeViewHolder;
import com.slotnslot.slotnslot.views.SlotViewHolder;
import com.trello.rxlifecycle2.components.support.RxFragment;

import org.web3j.abi.datatypes.Address;

import java.util.ArrayList;

public class SlotListAdapter extends RecyclerView.Adapter {
    public static final String TAG = SlotListAdapter.class.getSimpleName();

    static final int SLOT_ITEM_VIEWTYPE_ROOM = 0;
    static final int SLOT_ITEM_VIEWTYPE_ROOM_MAKE = 1;
    static final int SLOT_ITEM_VIEWTYPE_IMGAE = 2;
    static final int SLOT_ITEM_VIEWTYPE_BETA_DESCRIPTION = 3;

    private ArrayList<SlotRoomViewModel> items = new ArrayList<>();
    private ListType type;
    private RxFragment fragment;
    private Double deposit;

    public SlotListAdapter(ArrayList<SlotRoomViewModel> items, ListType type, RxFragment fragment) {
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
                return new SlotDescriptionViewHolder(itemView);
            case SLOT_ITEM_VIEWTYPE_BETA_DESCRIPTION:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.slot_recycler_beta_description, parent, false);
                return new SlotDescriptionViewHolder(itemView);
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
                                    SlotMachineManager slotMachineManager = SlotMachineManager.load(GethConstants.getManagerAddress());
                                    Address address = new Address(items.get(position - (type == ListType.PLAY ? 1 : 0)).getSlotAddress());
                                    slotMachineManager.removeSlotMachine(address)
                                            .compose(fragment.bindToLifecycle())
                                            .map(slotMachineManager::getSlotMachineRemovedEvents)
                                            .subscribe(responses -> {
                                                if (responses.isEmpty()) {
                                                    Log.e(TAG, "event is empty.");
                                                    return;
                                                }
                                                Log.i(TAG, "slot removed: banker address: " + responses.get(0)._banker.toString());
                                                Log.i(TAG, "slot removed: number of remaining slots: " + responses.get(0)._totalNum.getValue());
                                                String slotAddress = responses.get(0)._slotAddr.toString();
                                                Log.i(TAG, "slot removed: removed address: " + slotAddress);
                                                RxSlotRooms.removeMakeSlot(slotAddress);
                                            }, Throwable::printStackTrace);
                                    actionSheet.dismiss();
                                }
                            }).show();
                });
                holder.itemView.setOnClickListener(view -> {
                    SlotRoomViewModel slotRoomViewModel = items.get(position - (type == ListType.PLAY ? 1 : 0));
                    if (type == ListType.PLAY) {
                        setDeposit(slotRoomViewModel);
                    } else {
                        enterSlotRoom(slotRoomViewModel);
                    }
                });
        }
    }

    private void setDeposit(SlotRoomViewModel viewModel) {
        Context context = MainApplication.getContext();

        EditText editText = new EditText(context);
        editText.setTextColor(ContextCompat.getColor(context, R.color.pink1));
        editText.getBackground().setColorFilter(context.getResources().getColor(R.color.pink1), PorterDuff.Mode.SRC_ATOP);
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.input_deposite_margin);
        params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.input_deposite_margin);
        editText.setLayoutParams(params);

        FrameLayout container = new FrameLayout(context);
        container.addView(editText);

        AlertDialog dialog = new AlertDialog.Builder(fragment.getActivity())
                .setView(container)
                .setTitle("Please enter your initial deposit. (ether)")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    this.deposit = Double.parseDouble(editText.getText().toString());
                    enterSlotRoom(viewModel);
                })
                .setNegativeButton("CANCEL", null)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void enterSlotRoom(SlotRoomViewModel viewModel) {
        if ("test".equals(viewModel.getSlotAddress())) {
            Intent intent = new Intent(fragment.getContext(), SlotGameActivity.class);
            Bundle bundle = new Bundle();
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
                            boolean isBanker = AccountProvider.identical(viewModel.getRxSlotRoom().getSlotRoom().getBankerAddress());
                            bundle.putSerializable(Constants.ACTIVITY_EXTRA_KEY_SLOT_TYPE, isBanker ? SlotType.BANKER : SlotType.PLAYER);
                            bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM, viewModel.getSlotAddress());
                            if (type == ListType.PLAY && !viewModel.getRxSlotRoom().getSlotAddress().equals("test")) {
                                bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM_DEPOSIT, this.deposit);
                            }
                            intent.putExtras(bundle);
                            fragment.getContext().startActivity(intent);
                        },
                        Throwable::printStackTrace);
    }

    @Override
    public int getItemCount() {
        return items.size() + 1 + (type == ListType.MAKE ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        switch (type) {
            case PLAY:
                return position == 0 ? SLOT_ITEM_VIEWTYPE_IMGAE : SLOT_ITEM_VIEWTYPE_ROOM;
            case MAKE:
                if (items.size() > 0) {
                    if (position == 0 )
                        return SLOT_ITEM_VIEWTYPE_BETA_DESCRIPTION;
                    else if (position <= items.size())
                        return SLOT_ITEM_VIEWTYPE_ROOM;
                    else
                        return SLOT_ITEM_VIEWTYPE_ROOM_MAKE;
                } else {
                    if (position == 0)
                        return SLOT_ITEM_VIEWTYPE_BETA_DESCRIPTION;
                    else
                        return SLOT_ITEM_VIEWTYPE_ROOM_MAKE;
                }
        }
        return super.getItemViewType(position);
    }
}
