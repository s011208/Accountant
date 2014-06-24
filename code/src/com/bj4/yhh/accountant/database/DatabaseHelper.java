
package com.bj4.yhh.accountant.database;

import java.util.ArrayList;

import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    // plan related
    public static final String TABLE_NAME_PLAN = "plan";

    public static final String COLUMN_LAW_TYPE = "law_type";

    public static final String COLUMN_TOTAL_PROGRESS = "total_progress";

    public static final String COLUMN_CURRENT_PROGRESS = "current_progress";

    public static final String COLUMN_READING_ORDER = "reading_order";

    // data related
    public static final String TABLE_NAME_LAW = "law";

    public static final String COLUMN_CHAPTER = "chapter";

    public static final String COLUMN_SECTION = "section";

    public static final String COLUMN_SUBSECTION = "subsection";

    public static final String COLUMN_LINE = "line";

    public static final String COLUMN_CONTENT = "content";

    public static final String COLUMN_PART = "part";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_TYPE = "law_type";

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
        // law table
        getDataBase().execSQL(
                "CREATE TABLE if not exists " + TABLE_NAME_LAW + " (" + COLUMN_CHAPTER + " TEXT, "
                        + COLUMN_SECTION + " TEXT, " + COLUMN_PART + " TEXT, " + COLUMN_SUBSECTION
                        + " TEXT, " + COLUMN_LINE + " TEXT, " + COLUMN_CONTENT + " TEXT, "
                        + COLUMN_TYPE + " INTEGER)");
        // plan table
        getDataBase().execSQL(
                "CREATE TABLE if not exists " + TABLE_NAME_PLAN + " (" + COLUMN_LAW_TYPE
                        + " INTEGER PRIMARY KEY , " + COLUMN_TOTAL_PROGRESS + " TEXT, "
                        + COLUMN_CURRENT_PROGRESS + " TEXT, " + COLUMN_READING_ORDER + " TEXT)");
    }
    
    public void addNewPlan(){}

    public ArrayList<LawAttrs> query(int type) {
        ArrayList<LawAttrs> rtn = null;
        Cursor data = null;
        data = getDataBase().query(true, TABLE_NAME_LAW, null, COLUMN_TYPE + "='" + type + "'",
                null, null, null, null, null, null);
        rtn = convertFromCursorToLawAttrs(data);
        return rtn;
    }

    public ArrayList<Integer> getAllLawTypes() {
        ArrayList<Integer> rtn = new ArrayList<Integer>();
        Cursor data = getDataBase().rawQuery(
                "select distinct " + COLUMN_TYPE + " from " + TABLE_NAME_LAW, null);
        if (data != null) {
            while (data.moveToNext()) {
                rtn.add(data.getInt(0));
            }
            data.close();
        }
        return rtn;
    }

    public ArrayList<LawAttrs> convertFromCursorToLawAttrs(Cursor c) {
        ArrayList<LawAttrs> rtn = new ArrayList<LawAttrs>();
        if (c != null) {
            int chapterIndex = c.getColumnIndex(COLUMN_CHAPTER);
            int contentIndex = c.getColumnIndex(COLUMN_CONTENT);
            int lineIndex = c.getColumnIndex(COLUMN_LINE);
            int partIndex = c.getColumnIndex(COLUMN_PART);
            int sectionIndex = c.getColumnIndex(COLUMN_SECTION);
            int subSectionIndex = c.getColumnIndex(COLUMN_SUBSECTION);
            while (c.moveToNext()) {
                rtn.add(new LawAttrs(c.getString(partIndex), c.getString(chapterIndex), c
                        .getString(sectionIndex), c.getString(subSectionIndex), c
                        .getString(lineIndex), c.getString(contentIndex)));
            }
            c.close();
        }
        return rtn;
    }

    public ArrayList<ContentValues> convertFromLawAttrsToContentValues(ArrayList<LawAttrs> list,
            int type) {
        ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
        for (LawAttrs attr : list) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_CHAPTER, attr.mChapter);
            cv.put(COLUMN_CONTENT, attr.mContent);
            cv.put(COLUMN_LINE, attr.mLine);
            cv.put(COLUMN_SUBSECTION, attr.mSubSection);
            cv.put(COLUMN_SECTION, attr.mSection);
            cv.put(COLUMN_PART, attr.mPart);
            cv.put(COLUMN_TYPE, type);
            cvs.add(cv);
        }
        return cvs;
    }

    public ArrayList<LawAttrs> convertFromContentValuesToLawAttrs(ArrayList<ContentValues> cvs) {
        ArrayList<LawAttrs> rtn = new ArrayList<LawAttrs>();
        for (ContentValues cv : cvs) {
            rtn.add(new LawAttrs(cv.getAsString(COLUMN_PART), cv.getAsString(COLUMN_CHAPTER), cv
                    .getAsString(COLUMN_SECTION), cv.getAsString(COLUMN_SUBSECTION), cv
                    .getAsString(COLUMN_LINE), cv.getAsString(COLUMN_CONTENT)));
        }
        return rtn;
    }

    public void refreshLaw(ArrayList<LawAttrs> list, int type) {
        getDataBase().delete(TABLE_NAME_LAW, COLUMN_TYPE + "='" + type + "'", null);
        ArrayList<ContentValues> cvs = convertFromLawAttrsToContentValues(list, type);
        try {
            getDataBase().beginTransaction();
            for (ContentValues cv : cvs) {
                getDataBase().insertOrThrow(TABLE_NAME_LAW, null, cv);
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
