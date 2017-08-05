package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;

import butterknife.BindView;

public class SlotBankerFragment extends AbsSlotFragment {

    @BindView(R.id.slot_banker_kick_button)
    Button kickButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        betLineTextView = view.findViewById(R.id.slot_banker_bet_line_textview);
        totalBetTextView = view.findViewById(R.id.slot_banker_total_bet_textview);
        betETHTextView = view.findViewById(R.id.slot_banker_bet_eth_textview);

        viewModel.drawResultSubject.subscribe(this::drawResult);
        return view;
    }

    @Override
    protected void setClickEvents() {
        RxView.clicks(kickButton).subscribe(o -> viewModel.kickPlayer());
    }

    @Override
    protected int getViewID() {
        return R.layout.fragment_slot_banker;
    }
}
