package com.slotnslot.slotnslot.fragments;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.SlotType;
import com.slotnslot.slotnslot.Wheel.WheelView;
import com.slotnslot.slotnslot.Wheel.adapters.SlotAdapter;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.DrawingLine;
import com.slotnslot.slotnslot.models.PlaySlotViewModel;
import com.slotnslot.slotnslot.models.SlotResultDrawingLine;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;
import com.slotnslot.slotnslot.utils.DrawView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class AbsSlotFragment extends SlotRootFragment {

    @BindView(R.id.slot_common_slot_container)
    RelativeLayout slotContainer;
    @BindView(R.id.slot_common_slot_background)
    LinearLayout slotLayout;
    @BindView(R.id.slot_common_big_win_container)
    RelativeLayout bigWinContainer;
    @BindView(R.id.slot_common_big_win_textview)
    TextView bigWinTextView;
    @BindView(R.id.slot_common_current_balance_textview)
    TextView currentBalanceTextView;
    @BindView(R.id.slot_common_banker_stake_textview)
    TextView bankerStakeTextView;
    @BindView(R.id.slot_common_last_win_textview)
    TextView lastWinTextView;
    TextView betLineTextView;
    TextView totalBetTextView;
    TextView betETHTextView;

    protected PlaySlotViewModel viewModel;
    protected ArrayList<SlotAdapter> adapterList = new ArrayList<>();
    protected ArrayList<DrawView> payLineView = new ArrayList<>();
    private String slotRoomAddress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        slotRoomAddress = (String) getArguments().getSerializable(Constants.BUNDLE_KEY_SLOT_ROOM);
        if (TextUtils.isEmpty(slotRoomAddress)) {
            Utils.showToast("error! slot room address is empty.");
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getViewID(), container, false);
        ButterKnife.bind(this, view);

        SlotType type = (SlotType) getArguments().getSerializable(Constants.ACTIVITY_EXTRA_KEY_SLOT_TYPE);

        betLineTextView = view.findViewById(type == SlotType.BANKER ? R.id.slot_banker_bet_line_textview : R.id.play_bet_line_textview);
        totalBetTextView = view.findViewById(type == SlotType.BANKER ? R.id.slot_banker_total_bet_textview : R.id.play_total_bet_textview);
        betETHTextView = view.findViewById(type == SlotType.BANKER ? R.id.slot_banker_bet_eth_textview : R.id.play_bet_eth_textview);

        Shader textShader = new LinearGradient(0, 0, 0, 20,
                new int[]{ContextCompat.getColor(getContext(), R.color.big_win_start_color), ContextCompat.getColor(getContext(), R.color.pink1)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        bigWinTextView.getPaint().setShader(textShader);

        viewModel = new PlaySlotViewModel(slotRoomAddress);
        addSlot(0, false);
        addSlot(1, false);
        addSlot(2, false);
        addSlot(3, false);
        addSlot(4, false);

        subscribeTextChange();
        setClickEvents();

        if ("test".equals(viewModel.getRxSlotRoom().getSlotAddress())) { // if test
            return view;
        }
        Double deposit = (Double) getArguments().getSerializable(Constants.BUNDLE_KEY_SLOT_ROOM_DEPOSIT);

        // below do not need for test
        viewModel.onCreate();
        viewModel.gameOccupy(deposit);

        viewModel.getRxSlotRoom().updateBalance();
        viewModel.seedReadySubject.subscribe(ready -> loadingViewSetVisible(!ready));
        viewModel.drawResultSubject.subscribe(this::drawResult);
//        viewModel.toastSubject.subscribe(msg -> Utils.showToast(msg));
        viewModel.startSpin.subscribe(bool -> tapSpin());
        viewModel.stopSpin.subscribe(bool -> {
            removePayLines();
            tapStop(false);
        });

        return view;
    }

    protected abstract void setClickEvents();

    protected void subscribeTextChange() {
        viewModel.lastWinSubject.distinctUntilChanged()
                .subscribe(lastWin -> lastWinTextView.setText(String.format(Constants.PLAY_LAST_WIN_TEXT_FFORMAT, lastWin)));
        viewModel.playerBalanceObservable.distinctUntilChanged()
                .subscribe(balance -> currentBalanceTextView.setText(String.valueOf(Convert.fromWei(balance, Convert.Unit.ETHER))));
        viewModel.bankerBalanceObservable.distinctUntilChanged()
                .subscribe(balance -> bankerStakeTextView.setText(String.valueOf(Convert.fromWei(balance, Convert.Unit.ETHER))));
        viewModel.totalBetSubject.distinctUntilChanged()
                .subscribe(total -> totalBetTextView.setText(String.format(Constants.PLAY_BET_TOTAL_BET_FORMAT, total)));
        viewModel.betLineSubject.distinctUntilChanged()
                .subscribe(lines -> betLineTextView.setText(String.format(Constants.PLAY_BET_LINES_TEXT_FORMAT, lines)));
        viewModel.betEthSubject.distinctUntilChanged()
                .subscribe(betEth -> betETHTextView.setText(String.format(Constants.PLAY_BET_ETH_TEXT_FORMAT, betEth)));
    }

    protected abstract int getViewID();

    private void addSlot(int id, boolean setListener) {
        SlotAdapter slotAdapter = new SlotAdapter();
        adapterList.add(slotAdapter);

        WheelView wheel = /* getWheel(id); */addWheel(id);
        wheel.setViewAdapter(slotAdapter);
        wheel.setCurrentItem((int) (Math.random() * 10));
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        slotLayout.addView(wheel, new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    private WheelView addWheel(int id) {
        WheelView wheelView = new WheelView(getContext());
        wheelView.setId(id);
        return wheelView;
    }

    public void tapSpin() {
        Completable complete = Completable.complete();
        complete.delay(0, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> removePayLines());
        complete.delay(0, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> spin(0));
        complete.delay(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> spin(1));
        complete.delay(400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> spin(2));
        complete.delay(600, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> spin(3));
        complete.delay(800, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> spin(4));

//        complete.delay(2000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> drawResult(40));
    }

    private void spin(int index) {
        WheelView view = (WheelView) slotLayout.getChildAt(index);
        view.startScrolling();
    }

    protected void tapStop(boolean isDelay) {
        int delay = isDelay ? 2000 : 0;

        Completable complete = Completable.complete();
        complete.delay(delay, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> stop(0));
        complete.delay(delay + 200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> stop(1));
        complete.delay(delay + 400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> stop(2));
        complete.delay(delay + 600, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> stop(3));
        complete.delay(delay + 800, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> stop(4));
    }

    private void stop(int index) {
        bigWinContainer.setVisibility(View.INVISIBLE);
        WheelView view = (WheelView) slotLayout.getChildAt(index);
        view.stopScrolling();
    }

    public void autoSpin() {
        //TODO: AUTO SPIN
        for (int i = 0; i < slotLayout.getChildCount(); i++) {
            WheelView view = (WheelView) slotLayout.getChildAt(i);
            view.stopScrolling();
        }
    }

    public void drawResult(int slotResult) {
        if (slotResult == 0) {
            drawDefeatLine(false);
            return;
        }
        SlotResultDrawingLine slotResultDrawline = Utils.getDrawLine(viewModel.getCurrentLine(), slotResult);
        if (slotResultDrawline.drawable == SlotResultDrawingLine.Drawable.DRAWABLE) {
            drawSlot(slotResultDrawline);
        } else {
            drawBigWin(slotResult);
        }
    }

    private void drawDefeatLine(boolean isBigwin) {
        Integer[][] slotLines = new Integer[5][3];
        for (int i = 0; i < slotLines.length; i++) {
            for (int j = 0; j < slotLines[i].length; j++) {
                slotLines[i][j] = Constants.UNDEFINE;
            }
        }
        drawSlot(new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.DEFEAT, slotLines, null));
        tapStop(isBigwin);
    }

    private void drawBigWin(int slotResult) {
        drawDefeatLine(true);
        bigWinContainer.setVisibility(View.VISIBLE);
        bigWinTextView.setText("+" + slotResult);
    }

    private void drawSlot(SlotResultDrawingLine slotResultDrawingLine) {
        Completable
                .complete()
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (slotResultDrawingLine.drawable == SlotResultDrawingLine.Drawable.DRAWABLE) {
                        drawLine(slotResultDrawingLine);
                    }
                });

        Integer[][] drawLine = slotResultDrawingLine.slotLines;
        ArrayList<Integer> drawSymbols = new ArrayList<>();
        for (Integer[] line : drawLine) {
            for (Integer symbol : line) {
                if (symbol != Constants.UNDEFINE) {
                    drawSymbols.add(symbol);
                }
            }
        }
        ArrayList<Integer> notWinDrawSymbol = new ArrayList<>();
        for (int i = 0; i < Constants.items.length; i++) {
            if (!drawSymbols.contains(Integer.valueOf(i))) {
                notWinDrawSymbol.add(i);
                notWinDrawSymbol.add(i);
            }
        }
        for (int i = 0; i < drawLine.length; i++) {
            notWinDrawSymbol = adapterList.get(i).setItemIndexs(drawLine[i], notWinDrawSymbol);
        }
        tapStop(false);
    }

    private void drawLine(SlotResultDrawingLine slotResultDrawingLine) {
        ArrayList<DrawingLine> drawLine = slotResultDrawingLine.drawingLines;
        for (int i = 0; i < drawLine.size(); i++) {
            Integer[] winLine = Constants.WIN_LINE[drawLine.get(i).lineNum];
            int[][] points = new int[5][2];
            for (int j = 0; j < 5; j++) {
                WheelView wheelView = (WheelView) slotLayout.getChildAt(j);
                float y = wheelView.getY() + wheelView.getMeasuredHeight() / (3 - winLine[j]) - (winLine[j] == 1 ? 0 : (wheelView.getItemHeight() / 2));
                float x = wheelView.getX() + (wheelView.getWidth() / 2);
                points[j][0] = (int) x;
                points[j][1] = (int) y;
            }

            DrawView payLine = new DrawView(getContext(), points);
            payLineView.add(payLine);

            slotContainer.addView(payLine, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, slotContainer.getMeasuredHeight()));
        }
        slotLayout.bringToFront();
    }

    private void removePayLines() {
        for (DrawView view : payLineView) {
            slotContainer.removeView(view);
        }
        slotContainer.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
    }
}
