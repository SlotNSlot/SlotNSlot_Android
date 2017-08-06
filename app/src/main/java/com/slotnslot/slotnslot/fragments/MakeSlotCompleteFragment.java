package com.slotnslot.slotnslot.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.GethException;
import com.slotnslot.slotnslot.geth.TransactionManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.SlotRoom;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRooms;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;

import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class MakeSlotCompleteFragment extends SlotRootFragment {
    private static final String TAG = MakeSlotCompleteFragment.class.getSimpleName();

    private static final String HIT_RATIO = "HIT RATIO : %.1f %%";
    private static final String BET_RANGE = "BET RANGE : %.3f-%.3f ETH";
    private static final String TOTAL_STKE = "TOTAL STAKE : %.3f ETH";
    private static final String MAX_PRIZE = "MAX PRIZE : x %d ";

    @BindView(R.id.make_slot_complete_ratio_textview)
    TextView hitRatioTextView;
    @BindView(R.id.make_slot_complete_bet_range_textview)
    TextView betRangeTextView;
    @BindView(R.id.make_slot_complete_stake_textview)
    TextView stakeTextView;
    @BindView(R.id.make_slot_complete_max_prize_textview)
    TextView maxPrizeTextView;
    @BindView(R.id.make_slot_complete_room_name_edittext)
    EditText roomNameEditText;
    @BindView(R.id.make_slot_back_button)
    Button backButton;
    @BindView(R.id.make_slot_next_button)
    Button nextButton;

    protected SlotRoom slotRoom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.slotRoom = (SlotRoom) getArguments().getSerializable(Constants.BUNDLE_KEY_SLOT_ROOM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_complete, container, false);
        ButterKnife.bind(this, view);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        hitRatioTextView.setText(String.format(HIT_RATIO, slotRoom.getHitRatio()));
        betRangeTextView.setText(String.format(BET_RANGE, slotRoom.getMinBet(), slotRoom.getMaxBet()));
        stakeTextView.setText(String.format(TOTAL_STKE, Convert.fromWei(slotRoom.getBankerBalance(), Convert.Unit.ETHER)));
        maxPrizeTextView.setText(String.format(MAX_PRIZE, slotRoom.getMaxWinPrize()));

        nextButton.setText("CONFIRM");
        nextButton.setBackgroundResource(R.color.complete_confirm);
        nextButton.setOnClickListener(view1 -> this.next());
        backButton.setBackgroundResource(R.color.complete_cancel);
        backButton.setOnClickListener(view1 -> this.back());
        return view;
    }

    private void next() {
        SlotMachineManager slotMachineManager = SlotMachineManager.load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS);

        slotMachineManager.createSlotMachine(
                new Uint16((int) (slotRoom.getHitRatio() * 10)),
                new Uint256(Convert.toWei(slotRoom.getMinBet(), Convert.Unit.ETHER)),
                new Uint256(Convert.toWei(slotRoom.getMaxBet(), Convert.Unit.ETHER)),
                new Uint16(slotRoom.getMaxWinPrize()),
                Utils.stringToBytes16(roomNameEditText.getText().toString()))
                .map(slotMachineManager::getSlotMachineCreatedEvents)
                .flatMap(responses -> {
                    if (responses.isEmpty()) {
                        Log.e(TAG, "event is empty.");
                        return Observable.error(new GethException("event is empty"));
                    }
                    Log.i(TAG, "slot created banker : " + responses.get(0)._banker.toString());
                    Log.i(TAG, "slot created decider : " + responses.get(0)._decider.getValue());
                    Log.i(TAG, "slot created min bet : " + responses.get(0)._minBet.getValue());
                    Log.i(TAG, "slot created max bet : " + responses.get(0)._maxBet.getValue());
                    Log.i(TAG, "slot created total num : " + responses.get(0)._totalNum.getValue());

                    String slotAddress = responses.get(0)._slotAddr.toString();
                    Log.i(TAG, "slot created slot addr : " + slotAddress);

                    this.slotRoom.setTitle(roomNameEditText.getText().toString());
                    this.slotRoom.setAddress(slotAddress);

                    SlotRoom slotRoom = new SlotRoom(
                            slotAddress,
                            this.slotRoom.getTitle(),
                            responses.get(0)._decider.getValue().intValue() / 1000.0,
                            responses.get(0)._maxPrize.getValue().intValue(),
                            Convert.fromWei(responses.get(0)._minBet.getValue(), Convert.Unit.ETHER).doubleValue(),
                            Convert.fromWei(responses.get(0)._maxBet.getValue(), Convert.Unit.ETHER).doubleValue(),
                            AccountProvider.getAccount().getAddressHex(),
                            this.slotRoom.getBankerBalance()
                    );
                    RxSlotRooms.addMakeSlot(slotRoom);

                    return TransactionManager.sendFunds(slotAddress, this.slotRoom.getBankerBalance());
                })
                .subscribe(
                        hash -> Log.i(TAG, "fund sent. hash : " + hash.getHex()),
                        Throwable::printStackTrace
                );

        getActivity().finish();
    }

    private void back() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
