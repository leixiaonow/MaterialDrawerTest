package com.example.leixiao.notepaper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.example.leixiao.notepaper.materialdrawertest.R;

public class ColorView extends View {
    int mColor = -1;
    int mExradius;
    Drawable mFrameDrawble;
    int mInradius;
    boolean mSelected;
    int mStrokeWidth;

    public ColorView(Context context) {
        super(context);
        this.mInradius = context.getResources().getDimensionPixelOffset(R.dimen.color_cell_inradius);
        this.mExradius = context.getResources().getDimensionPixelOffset(R.dimen.color_cell_exradius);
        this.mStrokeWidth = context.getResources().getDimensionPixelOffset(R.dimen.color_cell_outer_stroke);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setColor(int color) {
        this.mColor = color;
        invalidate();
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        int vwidth = (getWidth() - getPaddingLeft()) - getPaddingRight();
        int vheight = (getHeight() - getPaddingTop()) - getPaddingBottom();
        Paint paint = new Paint(1);
        paint.setStyle(Style.FILL);
        paint.setColor(this.mColor);
        canvas.drawCircle(((float) vwidth) / 2.0f, ((float) vheight) / 2.0f, (float) this.mInradius, paint);
        paint.setStyle(Style.STROKE);
        //引用了com.android.volley.DefaultRetryPolicy，可不可以改？
        paint.setStrokeWidth(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        paint.setColor(637534208);
        canvas.drawCircle(((float) vwidth) / 2.0f, ((float) vheight) / 2.0f, (float) this.mInradius, paint);
        if (this.mSelected) {
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth((float) this.mStrokeWidth);
            paint.setColor(this.mColor);
            canvas.drawCircle(((float) vwidth) / 2.0f, ((float) vheight) / 2.0f, (float) (this.mExradius + this.mStrokeWidth), paint);
        }
        canvas.restoreToCount(saveCount);
    }
}
