package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.utils.DrawView;

import butterknife.BindView;

public class SlotPlayerFragment extends AbsSlotFragment {

    @BindView(R.id.play_bet_line_plus_button)
    ImageButton linePlusButton;
    @BindView(R.id.play_bet_line_minus_button)
    ImageButton lineMinusButton;
    @BindView(R.id.play_bet_eth_plus_button)
    ImageButton ethPlusButton;
    @BindView(R.id.play_bet_eth_minus_button)
    ImageButton ethMinusButton;
    @BindView(R.id.play_max_bet_button)
    RelativeLayout maxBetButton;
    @BindView(R.id.play_auto_button)
    Button autoButton;
    @BindView(R.id.play_spin_button)
    Button spinButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        betLineTextView = view.findViewById(R.id.play_bet_line_textview);
        totalBetTextView = view.findViewById(R.id.play_total_bet_textview);
        betETHTextView = view.findViewById(R.id.play_bet_eth_textview);
        return view;
    }

    @Override
    protected void setClickEvents() {
        RxView.clicks(spinButton).subscribe(o -> {
//            spinButton.setEnabled(false);
            tapSpin();
            for (DrawView view : payLineView) {
                slotContainer.removeView(view);
            }
            slotContainer.invalidate();
            if ("test".equals(viewModel.getRxSlotRoom().getSlotAddress())) {
                return;
            }
            viewModel.initGame();
        });
        RxView.clicks(autoButton).subscribe(o -> tapStop(false));
        RxView.clicks(linePlusButton).subscribe(o -> viewModel.linePlus());
        RxView.clicks(lineMinusButton).subscribe(o -> viewModel.lineMinus());
        RxView.clicks(ethPlusButton).subscribe(o -> viewModel.betPlus());
        RxView.clicks(ethMinusButton).subscribe(o -> viewModel.betMinus());
        RxView.clicks(maxBetButton).subscribe(o -> viewModel.maxBet());
    }

    @Override
    protected int getViewID() {
        return R.layout.fragment_slot_player;
    }
}