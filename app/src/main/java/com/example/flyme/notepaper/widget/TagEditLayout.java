package com.example.flyme.notepaper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TagEditLayout extends LinearLayout {
    boolean mChanged = false;

    public TagEditLayout(Context context) {
        super(context);
    }

    public TagEditLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagEditLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean getChanged() {
        return this.mChanged;
    }

    public void setChanged(boolean changed) {
        this.mChanged = changed;
    }
}
