package com.example.flyme.notepaper.widget;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;

import com.example.flyme.notepaper.utils.ReflectUtils;
import com.example.leixiao.materialdrawertest.R;


public class DragShadowBuilderMz extends DragShadowBuilder {
    public static final int STATE_ENTER_NORMAL = 0;
    public static final int STATE_ENTER_WARNING = 1;
    public static final int STATE_IDLE = -1;
    private Drawable mBackground;
    private Rect mBackgroundPadding;
    int mDragOffsetX;
    int mDragOffsetY;
    private int mDragViewBackground;
    private int mDragViewBackgroundDelete;
    private int mDragViewBackgroundFilter;
    private int mHeight;
    private Drawable mHightLightNormal;
    private Drawable mHightLightWarning;
    private boolean mNeedBackground;
    private Point mShowPoint;
    private int mState;
    private int mWidth;

    public DragShadowBuilderMz(View view) {
        this(view, true, null);
    }

    public DragShadowBuilderMz(View view, boolean needBg, Point shadowTouchPoint) {
        super(view);
        this.mNeedBackground = true;
        this.mShowPoint = null;
        this.mDragViewBackground = R.drawable.mz_list_selector_background_long_pressed;
        this.mDragViewBackgroundFilter = R.drawable.mz_list_selector_background_filter;
        this.mDragViewBackgroundDelete = R.drawable.mz_list_selector_background_delete;
        this.mState = STATE_IDLE;
        this.mNeedBackground = needBg;
        this.mShowPoint = shadowTouchPoint;
        if (view != null) {
            if (needBg) {
                this.mBackground = view.getContext().getResources().getDrawable(this.mDragViewBackground);
                this.mBackgroundPadding = new Rect();
                this.mBackground.getPadding(this.mBackgroundPadding);
                Rect padding = this.mBackgroundPadding;
                int width = view.getWidth();
                int height = view.getHeight();
                this.mWidth = (padding.left + width) + padding.right;
                this.mHeight = (padding.top + height) + padding.bottom;
                this.mBackground.setBounds(STATE_ENTER_NORMAL, STATE_ENTER_NORMAL, this.mWidth, this.mHeight);
                this.mHightLightNormal = view.getContext().getResources().getDrawable(this.mDragViewBackgroundFilter);
                this.mHightLightNormal.setBounds(STATE_ENTER_NORMAL, STATE_ENTER_NORMAL, width, height);
                this.mHightLightWarning = view.getContext().getResources().getDrawable(this.mDragViewBackgroundDelete);
                this.mHightLightWarning.setBounds(STATE_ENTER_NORMAL, STATE_ENTER_NORMAL, width, height);
            } else {
                this.mWidth = view.getWidth();
                this.mHeight = view.getHeight();
            }
            Rect frame = new Rect();
            view.getGlobalVisibleRect(frame);
            Point pt = new Point();
            ReflectUtils.getLastTouchPoint(view, pt);
            this.mDragOffsetX = Math.max(STATE_ENTER_NORMAL, pt.x - frame.left);
            this.mDragOffsetY = Math.max(STATE_ENTER_NORMAL, pt.y - frame.top);
        }
    }

    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        shadowSize.set(this.mWidth, this.mHeight);
        if (this.mNeedBackground) {
            shadowTouchPoint.set(this.mDragOffsetX + this.mBackgroundPadding.left, this.mDragOffsetY + this.mBackgroundPadding.top);
        } else {
            shadowTouchPoint.set(this.mDragOffsetX, this.mDragOffsetY);
        }
    }

    public void onDrawShadow(Canvas canvas) {
        if (this.mNeedBackground) {
            if (this.mState == 0) {
                this.mHightLightNormal.draw(canvas);
            } else if (this.mState == STATE_ENTER_WARNING) {
                this.mHightLightWarning.draw(canvas);
            } else {
                this.mBackground.draw(canvas);
            }
            canvas.save();
            canvas.translate((float) this.mBackgroundPadding.left, (float) this.mBackgroundPadding.top);
            super.onDrawShadow(canvas);
            canvas.restore();
            return;
        }
        super.onDrawShadow(canvas);
    }

    public void setDragingState(int state) {
        this.mState = state;
    }

    public void setDragItemBackgroundResources(int[] bgres) {
        if (bgres != null) {
            if (bgres.length > 0) {
                this.mDragViewBackground = bgres[STATE_ENTER_NORMAL];
            }
            if (bgres.length > STATE_ENTER_WARNING) {
                this.mDragViewBackgroundFilter = bgres[STATE_ENTER_WARNING];
            }
            if (bgres.length > 2) {
                this.mDragViewBackgroundDelete = bgres[2];
            }
        }
    }
}
