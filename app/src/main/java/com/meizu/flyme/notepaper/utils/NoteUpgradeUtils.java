package com.meizu.flyme.notepaper.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.appcompat.BuildConfig;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.flyme.notepaper.database.NotePaper.NoteFiles;
import com.meizu.flyme.notepaper.database.NotePaper.Notes;
import com.meizu.flyme.notepaper.database.NotePaperProvider;
import com.meizu.flyme.notepaper.reflect.ReflectUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class NoteUpgradeUtils {
    public static final String ENCODING = "UTF-8";
    public static final int IMAGE_WIDTH = 800;
    public static final String JSON_FILE_NAME = "name";
    public static final String JSON_IMAGE_HEIGHT = "height";
    public static final String JSON_IMAGE_WIDTH = "width";
    public static final String JSON_MTIME = "mtime";
    public static final String JSON_SPAN = "span";
    public static final String JSON_STATE = "state";
    public static final String JSON_TEXT = "text";
    public static final int STATE_CHECK_OFF = 1;
    public static final int STATE_CHECK_ON = 2;
    public static final int STATE_COMMON = 0;
    public static final int STATE_IMAGE = 3;
    public static final int STATE_RECORD = 4;
    static final String TAG = "NoteUpgradeUtils";

    String generateUUId() {
        return UUID.randomUUID().toString();
    }

    public long add(ContentValues cv, SQLiteDatabase db) {
        ArrayList<ContentValues> fileList = new ArrayList();
        upgradeContentValue(cv, fileList);
        long id = db.insert(NotePaperProvider.NOTES_TABLE_NAME, Notes.NOTE, cv);
        if (!(id == -1 || fileList == null)) {
            Iterator i$ = fileList.iterator();
            while (i$.hasNext()) {
                ContentValues filecv = (ContentValues) i$.next();
                if (db.insert(NotePaperProvider.FILES_TABLE_NAME, null, filecv) == -1) {
                    Log.d("NotePaperProvider", "insert file table fail: " + filecv.getAsString(JSON_FILE_NAME));
                }
            }
        }
        return id;
    }

    int getPaper(String color) {
        int paper = -1;
        if (TextUtils.isEmpty(color)) {
            return -1;
        }
        switch (color.toLowerCase().charAt(STATE_COMMON)) {
            case 'b':
                paper = STATE_RECORD;
                break;
            case 'g':
                paper = 7;
                break;
            case 'p':
                paper = 5;
                break;
            case 'w':
                paper = STATE_IMAGE;
                break;
            case 'y':
                paper = -1;
                break;
        }
        return paper;
    }

    public long upgradeContentValue(ContentValues cv, ArrayList<ContentValues> fileCvList) {
        try {
            int index;
            String fileName;
            String uuid = cv.getAsString(Notes.UUID);
            if (uuid == null || TextUtils.isEmpty(uuid)) {
                uuid = generateUUId();
                cv.put(Notes.UUID, uuid);
                Log.d(TAG, "generateUUId: " + uuid);
            }
            String noteText = cv.getAsString(Notes.NOTE);
            if (noteText != null) {
                noteText = noteText.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
            }
            Long now = Long.valueOf(System.currentTimeMillis());
            Long modify = cv.getAsLong(Notes.MODIFIED_DATE);
            if (modify == null) {
                cv.put(Notes.MODIFIED_DATE, now);
                modify = now;
            }
            if (!cv.containsKey(Notes.CREATE_TIME)) {
                cv.put(Notes.CREATE_TIME, modify);
            }
            if (!cv.containsKey(Notes.DIRTY)) {
                cv.put(Notes.DIRTY, Boolean.valueOf(true));
            }
            int paper = -1;
            String color = cv.getAsString(Notes.COLOR);
            if (color != null) {
                Log.d(TAG, "get color: " + color);
                cv.remove(Notes.COLOR);
                paper = getPaper(color);
            }
            if (paper == -1) {
                paper = STATE_COMMON;
                Integer paperInter = cv.getAsInteger(Notes.PAPER);
                if (paperInter != null) {
                    paper = paperInter.intValue();
                    Log.d(TAG, "get paper: " + paper);
                }
            }
            String filesStr = cv.getAsString("filelist");
            ArrayList<String> fileList = new ArrayList();
            if (filesStr != null) {
                Log.d(TAG, "filesStr: " + filesStr);
                String[] arr$ = filesStr.split(",");
                int len$ = arr$.length;
                for (int i$ = STATE_COMMON; i$ < len$; i$ += STATE_CHECK_OFF) {
                    String path = arr$[i$];
                    String newPath;
                    if (path.endsWith(".jpg")) {
                        newPath = addPicFile(uuid, path);
                        if (newPath != null) {
                            fileList.add(newPath);
                        }
                    } else if (path.endsWith(".svg") || path.endsWith(".xml")) {
                        newPath = addDrawingFile(uuid, path, paper);
                        if (newPath != null) {
                            fileList.add(newPath);
                        }
                    }
                }
            }
            JSONArray ja = new JSONArray();
            JSONObject jo = new JSONObject();
            jo.put(JSON_STATE, STATE_COMMON);
            jo.put(JSON_TEXT, noteText);
            jo.put(JSON_SPAN, null);
            ja.put(jo);
            for (index = STATE_COMMON; index < fileList.size(); index += STATE_CHECK_OFF) {
                jo = new JSONObject();
                jo.put(JSON_STATE, STATE_IMAGE);
                Rect rect = new Rect();
                fileName = (String) fileList.get(index);
                getImageSizeRect(getFilePath(uuid, fileName), rect);
                jo.put(JSON_IMAGE_HEIGHT, rect.bottom);
                jo.put(JSON_IMAGE_WIDTH, rect.right);
                jo.put(JSON_FILE_NAME, fileName);
                ja.put(jo);
                if (index == 0) {
                    cv.put(Notes.FIRST_IMAGE, jo.toString());
                }
            }
            cv.put(Notes.NOTE, ja.toString());
            cv.put(Notes.FILE_LIST, getFileListString(fileList));
            int[] iArr = new int[10];
            iArr = new int[]{STATE_CHECK_OFF, STATE_COMMON, 5, STATE_COMMON, 7, 6, 6, STATE_IMAGE, STATE_COMMON, 5};
            if (paper < 0 || paper >= iArr.length) {
                paper = STATE_COMMON;
            } else {
                paper = iArr[paper];
            }
            cv.put(Notes.PAPER, Integer.valueOf(paper));
            for (index = STATE_COMMON; index < fileList.size(); index += STATE_CHECK_OFF) {
                ContentValues fileCv = new ContentValues();
                fileCv.put(NoteFiles.NOTE_UUID, uuid);
                fileCv.put(Constants.JSON_KEY_TYPE, Integer.valueOf(STATE_COMMON));
                fileCv.put(JSON_FILE_NAME, (String) fileList.get(index));
                fileName = getFilePath(uuid, (String) fileList.get(index));
                fileCv.put(NoteFiles.MD5, md5sum(fileName));
                fileCv.put(JSON_MTIME, Long.valueOf(new File(fileName).lastModified()));
                fileCv.put(NoteFiles.DIRTY, Boolean.valueOf(true));
                fileCvList.add(fileCv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cv != null) {
            cv.remove("filelist");
        }
        return -1;
    }

    private void getImageSizeRect(String fileName, Rect rect) {
        Exception e;
        try {
            Options opts = new Options();
            try {
                opts.inJustDecodeBounds = true;
                Bitmap bm = BitmapFactory.decodeFile(fileName, opts);
                if (opts != null) {
                    rect.set(STATE_COMMON, STATE_COMMON, opts.outWidth, opts.outHeight);
                }
                if (bm != null) {
                    bm.recycle();
                }
            } catch (Exception e2) {
                e = e2;
                Options options = opts;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
        }
    }

    String getFileListString(ArrayList<String> list) {
        if (list.size() <= 0) {
            return null;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        Iterator i$ = list.iterator();
        while (i$.hasNext()) {
            String name = (String) i$.next();
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(name);
        }
        return sb.toString();
    }

    String getFilePath(String uuid, String name) {
        return NoteUtil.getFileName(uuid, name);
    }

    File createNewPictureFile(String uuid, String ext) {
        File pDataDir = new File(NoteUtil.FILES_ANDROID_DATA);
        if (pDataDir == null || !pDataDir.exists()) {
            Log.d(TAG, "Android data dir not exist.");
            return null;
        }
        Date date = new Date(System.currentTimeMillis());
        String prefix = String.format("img_%d%02d%02d_%02d%02d%02d_%d", new Object[]{Integer.valueOf(date.getYear() + LunarCalendar.MIN_YEAR), Integer.valueOf(date.getMonth() + STATE_CHECK_OFF), Integer.valueOf(date.getDate()), Integer.valueOf(date.getHours()), Integer.valueOf(date.getMinutes()), Integer.valueOf(date.getSeconds()), Long.valueOf(date.getTime() % 1000)});
        File file = null;
        int index = STATE_COMMON;
        while (index < ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
            String indexStr;
            if (index == 0) {
                indexStr = BuildConfig.VERSION_NAME;
            } else {
                indexStr = "_" + String.valueOf(index);
            }
            String fileName = prefix + indexStr + ext;
            file = new File(getFilePath(uuid, fileName));
            if (file.exists()) {
                index += STATE_CHECK_OFF;
            } else if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                try {
                    if (file.createNewFile()) {
                        return file;
                    }
                    Log.d(TAG, "createNewFile fail: " + fileName);
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                Log.d(TAG, "mkdirs fail: " + fileName);
                return null;
            }
        }
        return file;
    }

    String md5sum(String filename) {
        IOException e;
        Throwable th;
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(filename);
            try {
                String md5sum = md5sum(is2);
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e2) {
                    }
                }
                is = is2;
                return md5sum;
            } catch (IOException e3) {
                e = e3;
                is = is2;
                try {
                    Log.e(TAG, e.toString());
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e4) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                if (is != null) {
                    is.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Log.e(TAG, e.toString());
            if (is != null) {
                is.close();
            }
            return null;
        }
    }

    String md5sum(InputStream is) {
        try {
            byte[] buffer = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            while (true) {
                int numRead = is.read(buffer);
                if (numRead <= 0) {
                    return toHexString(md5.digest());
                }
                md5.update(buffer, STATE_COMMON, numRead);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static String toHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length * STATE_CHECK_ON);
        byte[] arr$ = buffer;
        int len$ = arr$.length;
        for (int i$ = STATE_COMMON; i$ < len$; i$ += STATE_CHECK_OFF) {
            Object[] objArr = new Object[STATE_CHECK_OFF];
            objArr[STATE_COMMON] = Byte.valueOf(arr$[i$]);
            String xchar = String.format("%X", objArr);
            if (xchar.length() == STATE_CHECK_OFF) {
                sb.append("0" + xchar);
            } else {
                sb.append(xchar);
            }
        }
        return sb.toString();
    }

    public String addPicFile(String uuid, String path) {
        File file = createNewPictureFile(uuid, ".jpg");
        if (file == null) {
            Log.d(TAG, "createNewPictureFile fail: " + path);
            return null;
        }
        File oldFile = new File(path);
        if (!oldFile.exists()) {
            Log.d(TAG, "file not exist: " + oldFile.getPath());
            return null;
        } else if (ReflectUtils.copyFile(oldFile, file)) {
            return file.getName();
        } else {
            Log.d(toString(), "copyFile fail: " + path);
            return null;
        }
    }

    public String addDrawingFile(String uuid, String path, int paper) {
        IOException e;
        Throwable th;
        FileNotFoundException e2;
        int i = 10;
        int[] colors = new int[]{-330026, -657931, -662823, -921105, -3216909, -662553, -1384232, -3477536, -921103, -860472};
        int color = colors[STATE_CHECK_OFF];
        if (paper > 0 && paper < colors.length) {
            color = colors[paper];
        }
        File file = createNewPictureFile(uuid, ".jpg");
        if (file == null) {
            Log.d(TAG, "createNewPictureFile fail: " + path);
            return null;
        }
        SketchPadStack stack = new SketchPadStack();
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(path);
            try {
                stack.load(fis2);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        fis = fis2;
                    }
                }
                if (stack == null || stack.isEmpty()) {
                    file.delete();
                    return null;
                }
                int height = stack.getHeight(true) + 40;
                if (height < 640) {
                    height = 640;
                }
                Bitmap src = Bitmap.createBitmap(IMAGE_WIDTH, height, Config.ARGB_8888);
                src.eraseColor(color);
                stack.draw(new Canvas(src), new Rect(STATE_COMMON, STATE_COMMON, IMAGE_WIDTH, height), null);
                FileOutputStream out = null;
                try {
                    FileOutputStream out2 = new FileOutputStream(file);
                    try {
                        src.compress(CompressFormat.JPEG, 80, out2);
                        src.recycle();
                        if (out2 != null) {
                            try {
                                out2.close();
                            } catch (IOException e3) {
                            }
                        }
                        if (stack != null) {
                            stack.clear();
                        }
                        return file.getName();
                    } catch (IOException e4) {
                        e = e4;
                        out = out2;
                        try {
                            Log.e(TAG, e.toString());
                            file.delete();
                            if (out != null) {
                                return null;
                            }
                            try {
                                out.close();
                                return null;
                            } catch (IOException e5) {
                                return null;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e6) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        out = out2;
                        if (out != null) {
                            out.close();
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    e = e7;
                    Log.e(TAG, e.toString());
                    file.delete();
                    if (out != null) {
                        return null;
                    }
                    out.close();
                    return null;
                }
            } catch (FileNotFoundException e8) {
                e2 = e8;
                fis = fis2;
                try {
                    e2.printStackTrace();
                    Log.d(toString(), "input stream fail: " + path);
                    file.delete();
                    if (fis != null) {
                        return null;
                    }
                    try {
                        fis.close();
                        return null;
                    } catch (IOException e12) {
                        e12.printStackTrace();
                        return null;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e122) {
                            e122.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                fis = fis2;
                if (fis != null) {
                    fis.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e2 = e9;
            e2.printStackTrace();
            Log.d(toString(), "input stream fail: " + path);
            file.delete();
            if (fis != null) {
                return null;
            }
            fis.close();
            return null;
        }
    }
}
