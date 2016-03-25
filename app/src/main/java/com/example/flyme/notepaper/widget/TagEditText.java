package com.example.flyme.notepaper.widget;

import android.content.Context;
import android.support.v7.appcompat.BuildConfig;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.widget.EditText;

import com.example.flyme.notepaper.TagData;

public class TagEditText extends EditText {
    boolean debug = false;
    TagData mTag;
    TextWatcher mTextWatch = new TextWatcher() {
        public void afterTextChanged(Editable editable) {
            if (TagEditText.this.mTag != null) {
                TagEditText.this.mTag.mNewName = TagEditText.this.getText().toString();
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    public TagEditText(Context context) {
        super(context);
    }

    public TagEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        addTextChangedListener(this.mTextWatch);
        setFilters(new InputFilter[]{new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (TagEditText.this.debug) {
                    Log.d("TagEditText", "filter dest:" + dest.toString() + " start: " + dstart + "  end: " + dend);
                    Log.d("TagEditText", "filter source:" + source.toString() + " start: " + start + "  end: " + end);
                }
                TextPaint pt = TagEditText.this.getPaint();
                int len = dest.length();
                String s1 = dstart > 0 ? dest.subSequence(0, dstart).toString() : BuildConfig.VERSION_NAME;
                String s2 = dend < len ? dest.subSequence(dend, len).toString() : BuildConfig.VERSION_NAME;
                int w = (TagEditText.this.getWidth() - TagEditText.this.getPaddingLeft()) - TagEditText.this.getPaddingRight();
                if (w <= 0) {
                    return null;
                }
                while (end >= start && pt.measureText(s1 + source.subSequence(start, end) + s2) > ((float) w)) {
                    end--;
                }
                CharSequence result = end > start ? source.subSequence(start, end) : BuildConfig.VERSION_NAME;
                if (!TagEditText.this.debug) {
                    return result;
                }
                Log.d("TagEditText", "result:" + result.toString());
                return result;
            }
        }});
    }

    public void setTagData(TagData td) {
        this.mTag = td;
    }

    public boolean onDragEvent(DragEvent event) {
        return false;
    }
}
