
package com.bj4.yhh.accountant.database;

import java.util.ArrayList;

import com.bj4.yhh.accountant.LawAttrs;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "law.db";

    private static final String TAG = "QQQQ";

    public static final String TABLE_NAME = "business_law";

    public static final String COLUMN_CHAPTER = "chapter";

    public static final String COLUMN_SECTION = "section";

    public static final String COLUMN_MU = "mu";

    public static final String COLUMN_LINE = "line";

    public static final String COLUMN_CONTENT = "content";

    public static final String COLUMN_ID = "_id";

    private SQLiteDatabase mDb;

    private Context mContext;

    private SQLiteDatabase getDataBase() {
        if ((mDb == null) || (mDb != null && mDb.isOpen() == false)) {
            try {
                mDb = getWritableDatabase();
            } catch (SQLiteFullException e) {
                Log.w(TAG, "SQLiteFullException", e);
            } catch (SQLiteException e) {
                Log.w(TAG, "SQLiteException", e);
            } catch (Exception e) {
                Log.w(TAG, "Exception", e);
            }
        }
        return mDb;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        getDataBase().execSQL("PRAGMA synchronous = 1");
        setWriteAheadLoggingEnabled(true);
        createTables();
    }

    public void createTables() {
        // business law
        getDataBase().execSQL(
                "CREATE TABLE if not exists " + TABLE_NAME + " (" + COLUMN_CHAPTER + " TEXT, "
                        + COLUMN_SECTION + " TEXT, " + COLUMN_MU + " TEXT, " + COLUMN_LINE
                        + " TEXT, " + COLUMN_CONTENT + " TEXT)");
    }

    public ArrayList<ContentValues> convertFromLawAttrsToContentValues(ArrayList<LawAttrs> list) {
        ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
        for (LawAttrs attr : list) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_CHAPTER, attr.mChapter);
            cv.put(COLUMN_CONTENT, attr.mContent);
            cv.put(COLUMN_LINE, attr.mLine);
            cv.put(COLUMN_MU, attr.mMu);
            cv.put(COLUMN_SECTION, attr.mSection);
            cvs.add(cv);
        }
        return cvs;
    }

    public ArrayList<LawAttrs> convertFromContentValuesToLawAttrs(ArrayList<ContentValues> cvs) {
        ArrayList<LawAttrs> rtn = new ArrayList<LawAttrs>();
        for (ContentValues cv : cvs) {
            rtn.add(new LawAttrs(cv.getAsString(COLUMN_CHAPTER), cv.getAsString(COLUMN_SECTION), cv
                    .getAsString(COLUMN_MU), cv.getAsString(COLUMN_LINE), cv
                    .getAsString(COLUMN_CONTENT)));
        }
        return rtn;
    }

    public void refreshBusinessLaw(ArrayList<LawAttrs> list) {
        getDataBase().delete(TABLE_NAME, null, null);
        ArrayList<ContentValues> cvs = convertFromLawAttrsToContentValues(list);
        try {
            getDataBase().beginTransaction();
            for (ContentValues cv : cvs) {
                getDataBase().insertOrThrow(TABLE_NAME, null, cv);
            }
            getDataBase().setTransactionSuccessful();
        } finally {
            getDataBase().endTransaction();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
