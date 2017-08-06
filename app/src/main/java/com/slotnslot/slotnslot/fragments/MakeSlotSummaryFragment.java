package com.slotnslot.slotnslot.fragments;

import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;

import butterknife.BindView;

public class MakeSlotSummaryFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_summary_hit_ratio_textview)
    TextView hitRatioTextView;
    @BindView(R.id.make_slot_summary_bet_range_textview)
    TextView betRangeTextView;
    @BindView(R.id.make_slot_summary_max_prize_textview)
    TextView maxPrizeTextView;
    @BindView(R.id.make_slot_summary_total_stake_textview)
    TextView totalStakeTextView;

    public void initView() {
        hitRatioTextView.setText(String.format(Constants.HIT_RATIO_TEXT_FORMAT, slotRoom.getHitRatio()));
        betRangeTextView.setText(String.format(Constants.BET_RANGE_TEXT_FORMAT, slotRoom.getMinBet(), slotRoom.getMaxBet()));
        totalStakeTextView.setText(String.format(Constants.TOTAL_STAKE_TEXT_FORMAT, Convert.fromWei(slotRoom.getBankerBalance(), Convert.Unit.ETHER)));
        maxPrizeTextView.setText(String.format(Constants.MAX_PRIZE_TEXT_FORMAT, slotRoom.getMaxWinPrize()));
    }
}
