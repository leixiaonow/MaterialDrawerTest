package com.example.leixiao.notepaper.data.dao.impl.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.leixiao.notepaper.data.Note;
import com.example.leixiao.notepaper.data.dao.NoteDAO;

import java.util.ArrayList;
import java.util.Date;

/**
 * 实现数据访问接口，从这里增删盖数据
 */
public class NoteSQLiteDAO implements NoteDAO {

    private static final String TAG = NoteSQLiteDAO.class.getSimpleName();
    private static final String WHERE_ID_CLAUSE = NoteEntry._ID + " = ?";

    private final SQLiteOpenHelper databaseHelper;

    /**
     *
     */
    public NoteSQLiteDAO(SQLiteOpenHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     *
     */
    @Override
    public ArrayList<Note> fetchAll() {
        ArrayList<Note> result = null;
        Cursor cursor = null;
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            String[] columns = {NoteEntry._ID,
                    NoteEntry.TITLE,
                    NoteEntry.CONTENT,
                    NoteEntry.CREATED_AT,
                    NoteEntry.UPDATED_AT};
            cursor = database.query(NoteEntry.TABLE_NAME, columns, null, null, null, null, null);
            result = new ArrayList<>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(NoteEntry._ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(NoteEntry.TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(NoteEntry.CONTENT)));
                note.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteEntry.CREATED_AT))));
                note.setUpdatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteEntry.UPDATED_AT))));
                result.add(note);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Could not complete fetch all", ex);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex) {
                    Log.e(TAG, "Couldn't close cursor correctly");
                }
            }
            database.close();
        }
        return result;
    }

    /**
     *
     */
    @Override
    public void insert(Note note) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(NoteEntry.TITLE, note.getTitle());
            values.put(NoteEntry.CONTENT, note.getContent());
            values.put(NoteEntry.CREATED_AT, note.getCreatedAt().getTime());
            values.put(NoteEntry.UPDATED_AT, note.getUpdatedAt().getTime());
            long rowId = database.insert(NoteEntry.TABLE_NAME, null, values);
            note.setId(rowId);
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Could not complete insert [" + note + "]", ex);
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    /**
     *
     */
    @Override
    public void update(Note note) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(NoteEntry.TITLE, note.getTitle());
            values.put(NoteEntry.CONTENT, note.getContent());
            values.put(NoteEntry.UPDATED_AT, note.getUpdatedAt().getTime());
            String[] whereArgs = {String.valueOf(note.getId())};
            database.update(NoteEntry.TABLE_NAME, values, WHERE_ID_CLAUSE, whereArgs);
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Could not complete update [" + note + "]", ex);
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    /**
     * Borra una nota de la tabla {@link NoteEntry#TABLE_NAME}.
     *
     * @param note la nota a borrar.
     * @see <a href="http://bit.ly/1syEh1A">Delete Information from a Database</a>
     */
    @Override
    public void delete(Note note) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            String[] whereArgs = {String.valueOf(note.getId())};
            database.delete(NoteEntry.TABLE_NAME, WHERE_ID_CLAUSE, whereArgs);
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Could not complete delete [" + note + "]", ex);
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    private static class NoteEntry implements BaseColumns {
        private static final String TABLE_NAME = "note";
        private static final String TITLE = "title";
        private static final String CONTENT = "content";
        private static final String CREATED_AT = "created_at";
        private static final String UPDATED_AT = "updated_at";
    }
}