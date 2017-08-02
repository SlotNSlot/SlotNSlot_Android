package com.slotnslot.slotnslot.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.models.SlotRoomViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class SlotViewHolder extends RecyclerView.ViewHolder {
    private static final String HIT_RATIO = "%d %%";
    private static final String BET_RANGE = "BET Range : %.3f-%.3f ETH";
    private static final String TOTAL_STKE = "%.3f ETH";
    private static final String MAX_PRIZE = "x %d ";
    private static final String PLAY_TIME = "Played %d times";

    @BindView(R.id.slot_title_textview)
    TextView titleTextView;
    @BindView(R.id.slot_total_value_text)
    TextView stackTextView;
    @BindView(R.id.slot_max_win_prize_value_text)
    TextView maxWinPrizeTextView;
    @BindView(R.id.slot_hit_ratio_value_text)
    TextView hitRatioTextView;
    @BindView(R.id.slot_play_time_text)
    TextView playTimeTextView;
    @BindView(R.id.slot_bet_range_textview)
    TextView betRangeTextView;
    @BindView(R.id.slot_delete_button)
    TextView deleteButton;
    @BindView(R.id.slot_more_button)
    ImageButton moreButton;

    private ArrayList<Disposable> disposables = new ArrayList<>();

    public SlotViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void onBindView(SlotRoomViewModel viewModel) {
        deleteButton.setVisibility(View.GONE);
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
        disposables.add(viewModel.stake.subscribe(stake -> stackTextView.setText(String.format(TOTAL_STKE, stake))));
        disposables.add(viewModel.playTime.subscribe(playTime -> playTimeTextView.setText(String.format(PLAY_TIME, playTime))));

        titleTextView.setText(viewModel.getRxSlotRoom().getSlotRoom().getTitle());
        maxWinPrizeTextView.setText(String.format(MAX_PRIZE, viewModel.getRxSlotRoom().getSlotRoom().getMaxWinPrize()));
        hitRatioTextView.setText(String.format(HIT_RATIO, viewModel.getRxSlotRoom().getSlotRoom().getHitRatio()));
        betRangeTextView.setText(String.format(BET_RANGE, viewModel.getRxSlotRoom().getSlotRoom().getMinBet(), viewModel.getRxSlotRoom().getSlotRoom().getMaxBet()));
    }

    public ImageButton getMoreButton() {
        return moreButton;
    }
}

