package com.slotnslot.slotnslot.fragments;

import android.view.View;
import android.widget.Button;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.models.SlotRoom;

import butterknife.BindView;
import butterknife.OnClick;

public class MakeSlotStepOneFragment extends MakeSlotStepFragment {

    @BindView(R.id.make_slot_select_1_button)
    Button selectButton1;
    @BindView(R.id.make_slot_select_2_button)
    Button selectButton2;
    @BindView(R.id.make_slot_select_3_button)
    Button selectButton3;

    static final private String BUTTON1_TEXT = "10%";
    static final private String BUTTON2_TEXT = "12.5%";
    static final private String BUTTON3_TEXT = "15%";

    public void initView() {
        slotRoom = new SlotRoom();
        backButton.setVisibility(View.GONE);

        selectButton1.setText(BUTTON1_TEXT);
        selectButton2.setText(BUTTON2_TEXT);
        selectButton3.setText(BUTTON3_TEXT);

        onClick(selectButton1);
    }

    @OnClick({R.id.make_slot_select_1_button, R.id.make_slot_select_2_button, R.id.make_slot_select_3_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.make_slot_select_1_button:
                slotRoom.setHitRatio(0.10);
                break;
            case R.id.make_slot_select_2_button:
                slotRoom.setHitRatio(0.125);
                break;
            case R.id.make_slot_select_3_button:
                slotRoom.setHitRatio(0.15);
                break;
        }

        Button[] buttons = {selectButton1, selectButton2, selectButton3};
        for (Button button: buttons) {
            button.setSelected(view.equals(button));
        }
    }
}
