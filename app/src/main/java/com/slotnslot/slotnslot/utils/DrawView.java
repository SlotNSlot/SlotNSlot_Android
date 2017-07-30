package com.slotnslot.slotnslot.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.slotnslot.slotnslot.R;

public class DrawView extends View {
    Paint paint = new Paint();
    private int [][]points;
    public DrawView(Context context, int[][] points) {
        super(context);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.pay_line_color));
        paint.setStrokeWidth(SlotUtil.convertDpToPixel(2f, getContext()));
        paint.setAntiAlias(true);
        this.points = points;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i=0; i<4; i++) {
            canvas.drawLine(points[i][0], points[i][1], points[i+1][0], points[i+1][1], paint);
        }
    }
}