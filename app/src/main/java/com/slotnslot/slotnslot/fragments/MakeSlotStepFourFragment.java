package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.Utils;

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
                .filter(cs -> !Utils.isEmpty(cs))
                .map(cs -> Double.parseDouble(cs.toString()))
                .subscribe(bet -> this.slotRoom.setMinBet(bet) );
        maxRangeObservable
                .filter(cs -> !Utils.isEmpty(cs))
                .map(cs -> Double.parseDouble(cs.toString()))
                .subscribe(bet -> this.slotRoom.setMaxBet(bet) );
    }

    @Override
    Observable<Boolean> verify() {
        if (Utils.isEmpty(minRangeEditText.getText().toString())
            || Utils.isEmpty(maxRangeEditText.getText().toString())) {
            Utils.showDialog(getActivity(), null, "Please enter min/max range.", "ok");
            return Observable.just(false);
        }
        if (Double.parseDouble(minRangeEditText.getText().toString()) <= 0) {
            Utils.showDialog(getActivity(), null, "Min range MUST be higher than 0 ETH.", "ok");
            return Observable.just(false);
        }
        if (Double.parseDouble(minRangeEditText.getText().toString()) > Double.parseDouble(maxRangeEditText.getText().toString())) {
            Utils.showDialog(getActivity(), null, "Max range MUST be higher than Min range.", "ok");
            return Observable.just(false);
        }
        return Observable.just(true);
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
