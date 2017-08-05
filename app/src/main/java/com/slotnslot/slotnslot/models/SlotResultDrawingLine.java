package com.slotnslot.slotnslot.models;

import java.util.ArrayList;

public class SlotResultDrawingLine {
    public enum Drawable {
        DRAWABLE, BIGWIN, DEFEAT
    }
    public final Drawable drawable;
    public final Integer[][] slotLines;
    public final ArrayList<DrawingLine> drawingLines;

    public SlotResultDrawingLine(Drawable drawable, Integer[][] slotLines, ArrayList<DrawingLine> drawingLines) {
        this.drawable = drawable;
        this.slotLines = slotLines;
        this.drawingLines = drawingLines;
    }
}
