package com.example.flyme.notepaper;

import android.database.Cursor;

import com.example.flyme.notepaper.utils.NoteUtil;
import com.meizu.flyme.notepaper.database.NotePaper.NoteCategory;
import com.meizu.flyme.notepaper.database.NotePaper.NoteFiles;
import com.meizu.flyme.notepaper.database.NotePaper.Notes;

public class TagData {
    public static boolean FUN_ENCRYPT = true;
    public static final long GROUP_ALL_ID = -1;
    public static final long GROUP_ENCRYPT_ID = -2;
    public static final String[] TAGS_LIST = new String[]{NoteFiles.DEFAULT_SORT_ORDER, NoteUtil.JSON_FILE_NAME};
    public static final String[] TAGS_PROJECTION = new String[]{NoteFiles.DEFAULT_SORT_ORDER, Notes.UUID, NoteUtil.JSON_FILE_NAME, NoteCategory.CATEGORY_ORDER};
    public static final int TAG_COUNT = 8;
    public int mCount;
    public long mId = GROUP_ALL_ID;
    public String mName;
    public String mNewName;
    public int mOrder;
    public String mUUId;

    public TagData(TagData td) {
        super();//我加的
        this.mId = td.mId;
        this.mUUId = td.mUUId;
        this.mName = td.mName;
        this.mNewName = td.mNewName;
        this.mOrder = td.mOrder;
        this.mCount = td.mCount;
    }
//我加的，因为getTag方法需要一个无参数的构造方法
    public TagData() {
        super();
    }

    //从Cursor得到TagData
    public static TagData getTag(Cursor c) {
        if (c == null) {
            return null;
        }
        TagData data = new TagData();
        data.mId = c.getLong(c.getColumnIndex(NoteFiles.DEFAULT_SORT_ORDER));
        data.mUUId = c.getString(c.getColumnIndex(Notes.UUID));
        String string = c.getString(c.getColumnIndex(NoteUtil.JSON_FILE_NAME));
        data.mName = string;
        data.mNewName = string;
        data.mOrder = c.getInt(c.getColumnIndex(NoteCategory.CATEGORY_ORDER));
        return data;
    }
}
