package com.example.leixiao.notepaper.database;

import android.accounts.Account;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.leixiao.notepaper.database.NotePaper.AccountConstract;

public class SyncStateContentProviderHelper {
    public static final String PATH = "syncstate";
    private static final String[] ACCOUNT_PROJECTION = new String[]{AccountConstract.ACCOUNT_NMAE, "account_type"};
    private static final String QUERY_COUNT_SYNC_STATE_ROWS = "SELECT count(*) FROM _sync_state WHERE _id=?";
    private static final String SELECT_BY_ACCOUNT = "account_name=? AND account_type=?";
    private static final String SYNC_STATE_META_TABLE = "_sync_state_metadata";
    private static final String SYNC_STATE_META_VERSION_COLUMN = "version";
    private static final String SYNC_STATE_TABLE = "_sync_state";
    private static long DB_VERSION = 1;

    private static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element == null) {
                if (value == null) {
                    return true;
                }
            } else if (value != null && element.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void createDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS _sync_state");
        db.execSQL("CREATE TABLE _sync_state (_id INTEGER PRIMARY KEY,account_name TEXT NOT NULL,account_type TEXT NOT NULL,data TEXT,UNIQUE(account_name, account_type));");
        db.execSQL("DROP TABLE IF EXISTS _sync_state_metadata");
        db.execSQL("CREATE TABLE _sync_state_metadata (version INTEGER);");
        ContentValues values = new ContentValues();
        values.put(SYNC_STATE_META_VERSION_COLUMN, Long.valueOf(DB_VERSION));
        db.insert(SYNC_STATE_META_TABLE, SYNC_STATE_META_VERSION_COLUMN, values);
    }

    public void onDatabaseOpened(SQLiteDatabase db) {
        if (DatabaseUtils.longForQuery(db, "SELECT version FROM _sync_state_metadata", null) != DB_VERSION) {
            createDatabase(db);
        }
    }

    public Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return db.query(SYNC_STATE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public long insert(SQLiteDatabase db, ContentValues values) {
        return db.replace(SYNC_STATE_TABLE, AccountConstract.ACCOUNT_NMAE, values);
    }

    public int delete(SQLiteDatabase db, String userWhere, String[] whereArgs) {
        return db.delete(SYNC_STATE_TABLE, userWhere, whereArgs);
    }

    public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
        return db.update(SYNC_STATE_TABLE, values, selection, selectionArgs);
    }

    public int update(SQLiteDatabase db, long rowId, Object data) {
        if (DatabaseUtils.longForQuery(db, QUERY_COUNT_SYNC_STATE_ROWS, new String[]{Long.toString(rowId)}) < 1) {
            return 0;
        }
        db.execSQL("UPDATE _sync_state SET data=? WHERE _id=" + rowId, new Object[]{data});
        return 1;
    }

    public void onAccountsChanged(SQLiteDatabase db, Account[] accounts) {
        Cursor c = db.query(SYNC_STATE_TABLE, ACCOUNT_PROJECTION, null, null, null, null, null);
        while (c.moveToNext()) {
            try {
                if (!contains(accounts, new Account(c.getString(0), c.getString(1)))) {
                    //       db.delete(SYNC_STATE_TABLE, SELECT_BY_ACCOUNT, new String[]{accountName, accountType});
                }
            } finally {
                c.close();
            }
        }
    }
}
