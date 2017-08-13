package com.slotnslot.slotnslot.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SlotBankerFragment extends AbsSlotFragment {

    @BindView(R.id.slot_banker_kick_button)
    Button kickButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        betLineTextView = view.findViewById(R.id.slot_banker_bet_line_textview);
        totalBetTextView = view.findViewById(R.id.slot_banker_total_bet_textview);
        betETHTextView = view.findViewById(R.id.slot_banker_bet_eth_textview);

        viewModel
                .drawResultSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(option -> {
                    viewModel.updateBalance();
                    this.drawResult(option.winRate);
                    viewModel.lastWinSubject.onNext(option.winRate * option.bet);
                });

        onBackPressed(view);
        return view;
    }

    private void onBackPressed(View view) {
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Leave")
                        .setMessage("Exit the watch screen?")
                        .setPositiveButton("Yes", (dialog, id) -> getActivity().finish())
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
        });
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
