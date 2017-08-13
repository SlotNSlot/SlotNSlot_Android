package com.slotnslot.slotnslot.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.models.SlotRoom;
import com.slotnslot.slotnslot.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public abstract class MakeSlotStepFragment extends SlotRootFragment {
    private int stepIndex;

    @BindView(R.id.make_slot_step_textview)
    TextView stepTextView;
    @BindView(R.id.make_slot_step_title_textview)
    TextView stepTitleTextView;
    @BindView(R.id.make_slot_step_description_textview)
    TextView stepDescriptionTextView;
    @BindView(R.id.make_slot_back_button)
    Button backButton;
    @BindView(R.id.make_slot_next_button)
    Button nextButton;

    protected SlotRoom slotRoom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.stepIndex = getArguments().getInt(Constants.BUNDLE_KEY_STEP_INDEX);
        this.slotRoom = (SlotRoom) getArguments().getSerializable(Constants.BUNDLE_KEY_SLOT_ROOM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        ButterKnife.bind(this, view);

        stepTextView.setText(stepIndex < 5 ? String.format("STEP %d of 4", stepIndex) : Constants.MAKE_SLOT_STEP_TITLE_SUMMARY);
        stepTitleTextView.setText(getStepTitle());
        stepDescriptionTextView.setText(getStepDescription());
        stepDescriptionTextView.setVisibility(stepIndex < 5 ? View.VISIBLE : View.GONE);
        nextButton.setOnClickListener(view1 -> this.next());
        backButton.setOnClickListener(view1 -> this.back());

        initView();
        return view;
    }

    abstract Observable<Boolean> verify();

    private void next() {
        verify().subscribe(verified -> {
            if (!verified) {
                return;
            }
            Fragment frag = getStepFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_KEY_STEP_INDEX, this.stepIndex + 1);
            bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM, this.slotRoom);
            frag.setArguments(bundle);
            FragmentManager fmanager = getActivity().getSupportFragmentManager();
            FragmentTransaction ftrans = fmanager.beginTransaction();
            ftrans.replace(R.id.fragment_framelayout, frag);
            ftrans.addToBackStack(null);
            ftrans.commit();
        }, Throwable::printStackTrace);
    }

    private void back() {
        getFragmentManager().popBackStack();
    }

    private Fragment getStepFragment() {
        switch (stepIndex) {
            case 1:
                return new MakeSlotStepTwoFragment();
            case 2:
                return new MakeSlotStepThreeFragment();
            case 3:
                return new MakeSlotStepFourFragment();
            case 4:
                return new MakeSlotSummaryFragment();
            case 5:
                return new MakeSlotCompleteFragment();
        }
        return null;
    }

    public abstract void initView();

    private int getLayout() {
        switch (stepIndex) {
            case 1:
                return R.layout.fragment_make_step_1;
            case 2:
                return R.layout.fragment_make_step_2;
            case 3:
                return R.layout.fragment_make_step_3;
            case 4:
                return R.layout.fragment_make_step_4;
            case 5:
                return R.layout.fragment_make_summary;
        }
        return 0;
    }

    private String getStepTitle() {
        int string = 0;
        switch (stepIndex) {
            case 1:
                string = R.string.step1_title;
                break;
            case 2:
                string = R.string.step2_title;
                break;
            case 3:
                string = R.string.step3_title;
                break;
            case 4:
                string = R.string.step4_title;
            case 5:
                string = R.string.summary_title;
        }
        return getString(string);
    }

    private String getStepDescription() {
        switch (stepIndex) {
            case 1:
                return getString(R.string.step1_description);
            case 2:
                return getString(R.string.step2_description);
            case 3:
                return getString(R.string.step3_description);
            case 4:
                return getString(R.string.step4_description);
            default:
                return Constants.EMPTY_STRING;
        }
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
