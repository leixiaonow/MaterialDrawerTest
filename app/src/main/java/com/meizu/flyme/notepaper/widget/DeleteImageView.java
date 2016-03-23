package com.meizu.flyme.notepaper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.meizu.flyme.notepaper.NoteEditActivity;


//删除按钮
public class DeleteImageView extends ImageView {

    public DeleteImageView(Context context) {
        super(context);
        init();
    }

    public DeleteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //引用NoteEditActivity，耦合高，可以改
    void init() {
        OnClickListener l = null;
        //在使用的地方设置监听，这样就可以不再依赖NoteEditActivity
        if (getContext() instanceof NoteEditActivity) {
            l = ((NoteEditActivity) getContext()).getDeleteClickListener();
        }
        setOnClickListener(l);
    }

    protected void onDraw(Canvas canvas) {
        if (!(getContext() instanceof NoteEditActivity) || !((NoteEditActivity) getContext()).getCaptureState()) {
            super.onDraw(canvas);
        }
        super.onDraw(canvas);
    }

    public void setImageType(int type) {
        switch (type) {
            case 0 /*0*/:
                setVisibility(GONE);
                return;
            default:
                return;
        }
    }
}
