package com.example.leixiao.notepaper;

import android.database.Cursor;
import android.support.v4.view.ViewCompat;
import android.util.Log;

import com.example.leixiao.notepaper.database.NotePaper.NoteFiles;
import com.example.leixiao.notepaper.database.NotePaper.Notes;
import com.example.leixiao.notepaper.utils.NoteUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class NoteData implements Serializable {
    public static final String[] NOTES_PROJECTION = new String[]{NoteFiles.DEFAULT_SORT_ORDER, Notes.TITLE, Notes.CREATE_TIME, Notes.MODIFIED_DATE, Notes.NOTE, Notes.PAPER, Notes.UUID, Notes.FONT_COLOR, Notes.FONT_SIZE, Notes.FIRST_IMAGE, Notes.FIRST_RECORD, Notes.ENCRYPT, Notes.TAG, Notes.TOP, Notes.DESKTOP};
    public static int DEFAULT_FONT_SIZE = 18;
    public long mCreateTime;
    public int mDesktop;
    public boolean mEncrypt;
    public String mFirstImg;
    public String mFirstRecord;
    public long mId = -1;
    public long mModifyTime;
    public String mNoteData;
    public int mPaper;
    public long mTag;
    public int mTextColor = ViewCompat.MEASURED_STATE_MASK;
    public int mTextSize = DEFAULT_FONT_SIZE;
    public String mTitle;
    public long mTopTime;
    public String mUUId;

    //从数据库Cursor创建NoteData对象，静态方法
    public static NoteData getItem(Cursor cursor) {
        NoteData nd = new NoteData();
        nd.mId = cursor.getLong(cursor.getColumnIndex(NoteFiles.DEFAULT_SORT_ORDER));
        nd.mUUId = cursor.getString(cursor.getColumnIndex(Notes.UUID));
        nd.mPaper = cursor.getInt(cursor.getColumnIndex(Notes.PAPER));
        nd.mCreateTime = cursor.getLong(cursor.getColumnIndex(Notes.CREATE_TIME));
        nd.mModifyTime = cursor.getLong(cursor.getColumnIndex(Notes.MODIFIED_DATE));
        nd.mTextColor = cursor.getInt(cursor.getColumnIndex(Notes.FONT_COLOR));
        if (nd.mTextColor == 0) {
            nd.mTextColor = ViewCompat.MEASURED_STATE_MASK;
        }
        nd.mTextSize = cursor.getInt(cursor.getColumnIndex(Notes.FONT_SIZE));
        if (nd.mTextSize <= 0) {
            nd.mTextSize = DEFAULT_FONT_SIZE;
        }
        nd.mTitle = cursor.getString(cursor.getColumnIndex(Notes.TITLE));
        nd.mNoteData = cursor.getString(cursor.getColumnIndex(Notes.NOTE));
        nd.mFirstImg = cursor.getString(cursor.getColumnIndex(Notes.FIRST_IMAGE));
        nd.mFirstRecord = cursor.getString(cursor.getColumnIndex(Notes.FIRST_RECORD));
        nd.mEncrypt = cursor.getInt(cursor.getColumnIndex(Notes.ENCRYPT)) != 0;
        if (nd.mFirstImg != null && nd.mFirstImg.length() == 0) {
            nd.mFirstImg = null;
        }
        if (nd.mFirstRecord != null && nd.mFirstRecord.length() == 0) {
            nd.mFirstRecord = null;
        }
        try {
            nd.mTag = cursor.getLong(cursor.getColumnIndex(Notes.TAG));
            nd.mTopTime = cursor.getLong(cursor.getColumnIndex(Notes.TOP));
            nd.mDesktop = cursor.getInt(cursor.getColumnIndex(Notes.DESKTOP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nd;
    }

    //从JSONObject中得到NoteItem
    public static NoteItem getNoteItem(JSONObject jo) {
        try {
            Object o = jo.opt(NoteUtil.JSON_STATE);
            int state = 0;
            if (o != null && (o instanceof Integer)) {
                state = ((Integer) o).intValue();
            }
            switch (state) {
                case 0 /*0*/:
                case 1 /*1*/:
                case 2 /*2*/:
                    NoteItemText ntt = new NoteItemText();
                    ntt.mState = state;
                    o = jo.opt(NoteUtil.JSON_TEXT);
                    if (o != null && (o instanceof String)) {
                        ntt.mText = (String) o;
                    }
                    o = jo.opt(NoteUtil.NOTE_SPAN_TYPE);
                    if (!(ntt.mText == null || o == null || !(o instanceof String))) {
                        ntt.mSpan = (String) o;
                    }
                    return ntt;
                case 3 /*3*/:
                    NoteItemImage nii = new NoteItemImage();
                    nii.mState = state;
                    o = jo.opt(NoteUtil.JSON_IMAGE_HEIGHT);
                    if (o != null && (o instanceof Integer)) {
                        nii.mHeight = ((Integer) o).intValue();
                    }
                    o = jo.opt(NoteUtil.JSON_IMAGE_WIDTH);
                    if (o != null && (o instanceof Integer)) {
                        nii.mWidth = ((Integer) o).intValue();
                    }
                    o = jo.opt(NoteUtil.JSON_FILE_NAME);
                    if (o == null || !(o instanceof String)) {
                        return nii;
                    }
                    nii.mFileName = (String) o;
                    return nii;
                case 4 /*4*/:
                    NoteItemRecord nir = new NoteItemRecord();
                    nir.mState = state;
                    o = jo.opt(NoteUtil.JSON_FILE_NAME);
                    if (o != null && (o instanceof String)) {
                        nir.mFileName = (String) o;
                    }
                    return nir;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //显示笔记列表项的时候可能用到，得到第一张图片，以便于在列表项中显示
    public static NoteItemImage getFirstImage(String firstImg) {
        NoteItemImage noteItemImage = null;
        if (!(firstImg == null || firstImg.length() == 0)) {
            try {
                JSONObject jo = new JSONObject(firstImg);
                if (jo != null) {
                    noteItemImage = new NoteItemImage();
                    noteItemImage.mState = 3;
                    Object o = jo.opt(NoteUtil.JSON_IMAGE_HEIGHT);
                    if (o != null && (o instanceof Integer)) {
                        noteItemImage.mHeight = ((Integer) o).intValue();
                    }
                    o = jo.opt(NoteUtil.JSON_IMAGE_WIDTH);
                    if (o != null && (o instanceof Integer)) {
                        noteItemImage.mWidth = ((Integer) o).intValue();
                    }
                    o = jo.opt(NoteUtil.JSON_FILE_NAME);
                    if (o != null && (o instanceof String)) {
                        noteItemImage.mFileName = (String) o;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return noteItemImage;
    }

    //显示笔记列表项的时候可能用到，是否只有图片
    public static boolean isImgOnly(Cursor cursor) {
        String firsImg = cursor.getString(cursor.getColumnIndex(Notes.FIRST_IMAGE));
        if (firsImg == null || firsImg.length() <= 0) {
            return false;
        }
        String title = cursor.getString(cursor.getColumnIndex(Notes.TITLE));
        if (title != null && title.length() > 0) {
            return false;
        }
        String noteData = cursor.getString(cursor.getColumnIndex(Notes.NOTE));
        if (noteData != null) {
            try {
                JSONArray ja = new JSONArray(noteData);
                int size = ja.length();
                for (int i = 0; i < size; i++) {
                    Object obj = ja.opt(i);
                    if (obj != null && (obj instanceof JSONObject)) {
                        JSONObject jo = (JSONObject) obj;
                        Object o = jo.opt(NoteUtil.JSON_STATE);
                        int state = -1;
                        if (o != null && (o instanceof Integer)) {
                            state = ((Integer) o).intValue();
                        }
                        if (state == 0 || state == 2 || state == 1) {
                            o = jo.opt(NoteUtil.JSON_TEXT);
                            if (o != null && (o instanceof String) && ((String) o).length() > 0) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return true;
            }
        }
        Log.e("NoteData", "note content is null, but there is image.");
        return true;
    }
}
