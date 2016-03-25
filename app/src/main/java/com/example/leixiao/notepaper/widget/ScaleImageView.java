package com.example.leixiao.notepaper.widget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.example.leixiao.notepaper.NoteEditActivity;
import com.example.leixiao.notepaper.materialdrawertest.R;
import com.example.leixiao.notepaper.utils.ImageUtil;
import com.example.leixiao.notepaper.utils.NoteUtil;
import com.example.leixiao.notepaper.utils.ReflectUtils;

import java.io.File;

//有很大的问题，反编译不正确
public class ScaleImageView extends ImageView {
    private final String TAG = "ScaleImageView";
    public String mFileName;
    public String mUUID;
    public long mMTime;
    public int mHeight;
    public int mWidth;

    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    void init() {
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScaleImageView.this.onClick();
            }
        });
        setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                View view = (View) v.getParent().getParent();
                if (view instanceof RichFrameLayout) {
                    ((RichFrameLayout) view).onFocus();
                }
                return true;
            }
        });
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        boolean scaleToWidth = false;
        if (widthMode == 1073741824 || widthMode == ExploreByTouchHelper.INVALID_ID) {
            scaleToWidth = true;
        }
        if (!(width == 0 || !scaleToWidth || this.mWidth == 0)) {
            int picHeight = this.mHeight;
            int picWidth = this.mWidth;
            if (!NoteUtil.getFile(this.mUUID, this.mFileName).exists()) {
                picWidth = 950;
                picHeight = 240;
            }
            int height = (((((((width - getPaddingLeft()) - getPaddingRight()) * picHeight) + picWidth) - 1) / picWidth) + getPaddingTop()) + getPaddingBottom();
            LayoutParams lp = getLayoutParams();
            if (lp.height != height) {
                lp.height = height;
            }
            int maxHeight = getMaxHeight();
            if (height > maxHeight && maxHeight > 0) {
                height = maxHeight;
                lp.height = height;
            }
            setLayoutParams(lp);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setUUIDandName(String uuid, String name) {
        this.mUUID = uuid;
        this.mFileName = name;
        File file = NoteUtil.getFile(this.mUUID, this.mFileName);
        if (file.exists()) {
            setImageFile(NoteUtil.getFileName(uuid, name));
            this.mMTime = file.lastModified();
            Rect rect = new Rect();
            ImageUtil.getImageSizeRect(file.getPath(), rect);
            this.mWidth = rect.right;
            this.mHeight = rect.bottom;
            requestLayout();
            return;
        }
        setImageResource(R.drawable.unknown_image);
        if (this.mWidth == 0) {
            this.mWidth = 950;
            this.mHeight = 240;
        }
        setOnClickListener(null);
        requestLayout();
    }

    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    void setImageFile(String imageFileName) {

    }

    public void onClick() {
        Point pt = new Point();
        ReflectUtils.getLastTouchPoint(this, pt);
        Rect r = new Rect();
        getGlobalVisibleRect(r);
        View view;
        if (pt.x < r.left + 40 || pt.x > r.right - 40) {
            view = (View) getParent().getParent();
            if (view instanceof RichFrameLayout) {
                ((RichFrameLayout) view).onFocus();
                return;
            }
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        File file = NoteUtil.getFile(this.mUUID, this.mFileName);
        if (!file.exists()) {
            return;
        }
/*        if (getContext() instanceof NoteFloatViewService) {
            view = (View) getParent().getParent();
            if (view instanceof RichFrameLayout) {
                ((RichFrameLayout) view).onFocus();
                return;
            }
            return;
        }*/
        intent.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getPath().substring(file.getPath().lastIndexOf(".") + 1)));
        String dataString = intent.getData().getPath();
        intent.addFlags(AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END);
        intent.putExtra("notepaper_custom_view", true);
        try {
            ((Activity) getContext()).startActivity(intent);
            view = (View) getParent().getParent();
            if (view instanceof RichFrameLayout) {
                ((NoteEditActivity) getContext()).viewImage((RichFrameLayout) view);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable dr = getDrawable();
        setImageDrawable(null);
        setBackground(null);
        if (dr != null && (dr instanceof BitmapDrawable)) {
            Bitmap bm = ((BitmapDrawable) dr).getBitmap();
            if (bm != null && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }
}
