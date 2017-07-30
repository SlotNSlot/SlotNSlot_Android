package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.slotnslot.slotnslot.R;

import butterknife.BindView;
import io.reactivex.Observable;

public class MakeSlotStepTwoFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_stake_edittext)
    EditText editText;
    @BindView(R.id.make_slot_current_balance_textview)
    TextView balanceTextView;

    public void initView() {
        Observable<CharSequence> observable = RxTextView.textChanges(editText);
        observable.filter(cs -> isStringDouble(cs.toString()))
                .map(cs -> Double.parseDouble(cs.toString()))
                .filter(stake -> stake != null)
                .subscribe(stake -> this.slotRoom.setStake(stake));
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
