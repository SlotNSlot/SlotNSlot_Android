package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.slotnslot.slotnslot.R;

import butterknife.BindView;
import io.reactivex.Observable;

public class MakeSlotStepFourFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_min_range_edittext)
    EditText minRangeEditText;
    @BindView(R.id.make_slot_max_range_edittext)
    EditText maxRangeEditText;

    public void initView() {
        Observable<CharSequence> minRangeObservable = RxTextView.textChanges(minRangeEditText);
        Observable<CharSequence> maxRangeObservable = RxTextView.textChanges(maxRangeEditText);
        minRangeObservable
                .filter(cs -> isStringDouble(cs.toString()))
                .map(cs -> Double.parseDouble(cs.toString()))
                .filter(bet -> bet != null)
                .subscribe(bet -> this.slotRoom.setMinBet(bet) );
        maxRangeObservable
                .filter(cs -> isStringDouble(cs.toString()))
                .map(cs -> Double.parseDouble(cs.toString()))
                .filter(bet -> bet != null)
                .subscribe(bet -> this.slotRoom.setMaxBet(bet) );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showKeyboard();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }
}
