package com.meizu.flyme.notepaper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class EditTextCloud extends EditText {
    private OnKeyPreImeListener mOnKeyPreImeListener;

    public interface OnKeyPreImeListener {
        boolean onKeyPreIme(View view, int i, KeyEvent keyEvent);
    }

    public EditTextCloud(Context context) {
        super(context);
    }

    public EditTextCloud(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextCloud(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (this.mOnKeyPreImeListener == null || !this.mOnKeyPreImeListener.onKeyPreIme(this, keyCode, event)) {
            return super.onKeyPreIme(keyCode, event);
        }
        return true;
    }

    public void setOnKeyPreImeListener(OnKeyPreImeListener l) {
        this.mOnKeyPreImeListener = l;
    }
}
