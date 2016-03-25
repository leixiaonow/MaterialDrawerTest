package com.example.leixiao.notepaper.widget;

import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;

public class NoteRelativeSizeSpan extends RelativeSizeSpan {
    public NoteRelativeSizeSpan(float proportion) {
        super(proportion);
    }

    public void updateDrawState(TextPaint ds) {
        ds.setTextSize(getTextSize(ds.getTextSize()));
    }

    float getTextSize(float paintSize) {
        float size = paintSize * getSizeChange();
        if (size > 160.21729f) {
            return 160.21729f;
        }
        if (size < 11.010048f) {
            return 11.010048f;
        }
        return size;
    }

    public void updateMeasureState(TextPaint ds) {
        ds.setTextSize(getTextSize(ds.getTextSize()));
    }
}
