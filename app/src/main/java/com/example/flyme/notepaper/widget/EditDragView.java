package com.example.flyme.notepaper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.flyme.notepaper.NoteEditActivity;
import com.example.leixiao.materialdrawertest.R;

public class EditDragView extends ImageView {
    int mActivePointerId;
    boolean mBeginDrag;
    int mLastTouchX;
    int mLastTouchY;
    LongPressedRunnable mPendingLongPress;
    int mTouchSlopSquare;

    public EditDragView(Context context) {
        super(context);
    }

    public EditDragView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditDragView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    void init() {
        int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mTouchSlopSquare = touchSlop * touchSlop;
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getActionMasked()) {
                    case 0 /*0*/:
                        EditDragView.this.mLastTouchX = (int) ev.getX();
                        EditDragView.this.mLastTouchY = (int) ev.getY();
                        int actionIndex = ev.getActionIndex();
                        EditDragView.this.mActivePointerId = ev.getPointerId(actionIndex);
                        EditDragView.this.mBeginDrag = false;
                        ((ViewGroup) EditDragView.this.getParent()).requestDisallowInterceptTouchEvent(true);
                        if (EditDragView.this.mPendingLongPress == null) {
                            EditDragView.this.mPendingLongPress = new LongPressedRunnable();
                        }
                        EditDragView.this.postDelayed(EditDragView.this.mPendingLongPress, (long) ViewConfiguration.getLongPressTimeout());
                        break;
                    case 2 /*2*/:
                        if (ev.findPointerIndex(EditDragView.this.mActivePointerId) < 0) {
                            Log.e(toString(), "onInterceptTouchEvent could not find pointer with id " + EditDragView.this.mActivePointerId);
                            return false;
                        } else if (EditDragView.this.mBeginDrag) {
                            return true;
                        } else {
                            float x = ev.getX();
                            int deltaX = (int) (x - ((float) EditDragView.this.mLastTouchX));
                            int deltaY = (int) (ev.getY() - ((float) EditDragView.this.mLastTouchY));
                            if ((deltaX * deltaX) + (deltaY * deltaY) > EditDragView.this.mTouchSlopSquare) {
                                if (EditDragView.this.mPendingLongPress != null) {
                                    EditDragView.this.removeCallbacks(EditDragView.this.mPendingLongPress);
                                    EditDragView.this.mPendingLongPress = null;
                                }
                                ((NoteEditActivity) EditDragView.this.getContext()).onDragList(v);
                                EditDragView.this.mBeginDrag = true;
                                return true;
                            }
                        }
                        break;
                    default:
                        if (EditDragView.this.mPendingLongPress != null) {
                            EditDragView.this.removeCallbacks(EditDragView.this.mPendingLongPress);
                            EditDragView.this.mPendingLongPress = null;
                        }
                        ((ViewGroup) EditDragView.this.getParent()).requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return true;
            }
        });
    }

    protected void onDraw(Canvas canvas) {
        if (!(getContext() instanceof NoteEditActivity) || !((NoteEditActivity) getContext()).getCaptureState()) {
            super.onDraw(canvas);
        }
    }

    public void setImageType(int type) {
        switch (type) {
            case 0 /*0*/:
                setImageDrawable(null);
                setVisibility(VISIBLE);
                return;
            case 1 /*1*/:
                setImageResource(R.drawable.ic_sequence);
                setVisibility(VISIBLE);
                return;
            case 2 /*2*/:
                setImageResource(R.drawable.ic_sequence);
                setVisibility(VISIBLE);
                return;
            default:
                return;
        }
    }

    class LongPressedRunnable implements Runnable {
        LongPressedRunnable() {
        }

        public void run() {
            ((NoteEditActivity) EditDragView.this.getContext()).onDragList(EditDragView.this);
            EditDragView.this.mBeginDrag = true;
        }
    }
}
