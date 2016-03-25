package com.example.leixiao.notepaper.widget;

import android.view.View;

public class ListDragLocalState {
    private View mDragView;
    private boolean mFocusFlag;

    public ListDragLocalState(View view, Boolean flag) {
        this.mDragView = view;
        this.mFocusFlag = flag.booleanValue();
    }

    public View getDragView() {
        return this.mDragView;
    }

    public boolean getFocusFlag() {
        return this.mFocusFlag;
    }
}
