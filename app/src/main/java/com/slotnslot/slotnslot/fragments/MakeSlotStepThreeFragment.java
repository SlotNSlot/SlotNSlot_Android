package com.slotnslot.slotnslot.fragments;

import android.view.View;
import android.widget.Button;

import com.slotnslot.slotnslot.R;

import butterknife.BindView;
import butterknife.OnClick;

public class MakeSlotStepThreeFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_select_1_button)
    Button selectButton1;
    @BindView(R.id.make_slot_select_2_button)
    Button selectButton2;
    @BindView(R.id.make_slot_select_3_button)
    Button selectButton3;

    static final private String BUTTON1_TEXT = "1000";
    static final private String BUTTON2_TEXT = "1500";
    static final private String BUTTON3_TEXT = "2000";

    @Override
    boolean verify() {
        return true;
    }

    public void initView() {
        selectButton1.setText(BUTTON1_TEXT);
        selectButton2.setText(BUTTON2_TEXT);
        selectButton3.setText(BUTTON3_TEXT);

        onClick(selectButton1);
    }

    @OnClick({R.id.make_slot_select_1_button, R.id.make_slot_select_2_button, R.id.make_slot_select_3_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.make_slot_select_1_button:
                slotRoom.setMaxWinPrize(1000);
                break;
            case R.id.make_slot_select_2_button:
                slotRoom.setMaxWinPrize(1500);
                break;
            case R.id.make_slot_select_3_button:
                slotRoom.setMaxWinPrize(2000);
                break;
        }

        Button[] buttons = {selectButton1, selectButton2, selectButton3};
        for (Button button: buttons) {
            button.setSelected(view.equals(button));
        }
    }
}
