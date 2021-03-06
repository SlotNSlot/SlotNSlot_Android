package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.utils.Convert;

import butterknife.BindView;
import io.reactivex.Observable;

public class MakeSlotStepTwoFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_stake_edittext)
    EditText stakeTxt;
    @BindView(R.id.make_slot_current_balance_textview)
    TextView balanceTextView;

    public void initView() {
        Observable<CharSequence> observable = RxTextView.textChanges(stakeTxt);
        observable
                .filter(cs -> !Utils.isEmpty(cs))
                .map(cs -> Double.parseDouble(cs.toString()))
                .subscribe(stake -> this.slotRoom.setBankerBalance(Convert.toWei(stake, Convert.Unit.ETHER)));
        balanceTextView.setText("Current balance : " + Convert.fromWei(AccountProvider.account.getBalance(), Convert.Unit.ETHER) + " ETH");
    }

    @Override
    Observable<Boolean> verify() {
        if (Utils.isEmpty(stakeTxt.getText().toString())) {
            Utils.showDialog(getActivity(), null, "Stake MUST be entered.", "ok");
            return Observable.just(false);
        }
        if (Double.parseDouble(stakeTxt.getText().toString()) <= 0) {
            Utils.showDialog(getActivity(), null, "Stake MUST be higher than 0 ETH.", "ok");
            return Observable.just(false);
        }
        return AccountProvider.getBalance()
                .flatMap(balance -> {
                    if (Double.parseDouble(stakeTxt.getText().toString()) > Convert.fromWei(balance, Convert.Unit.ETHER).doubleValue() - 0.5) {
                        Utils.showDialog(getActivity(), null, "Please leave a certain amount of gas fee for the game. (at least 0.5 ETH)", "ok");
                        return Observable.just(false);
                    }
                    return Observable.just(true);
                });
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
