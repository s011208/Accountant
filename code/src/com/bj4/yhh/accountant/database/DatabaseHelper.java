
package com.bj4.yhh.accountant.database;

import java.util.ArrayList;
import java.util.Collections;

import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.LawPara;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.activities.TestActivity;
import com.bj4.yhh.accountant.fragments.CreatePlanFragment;
import com.bj4.yhh.accountant.fragments.TestFragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "law.db";

    private static final String TAG = "DatabaseHelper";

    // law paragraph table

    public static final String TABLE_NAME_LAW_PARAGRAPH = "law_paragraph";

    public static final String COLUMN_LAW_PARAGRAPH_TITLE = "title";

    // update time table
    public static final String TABLE_NAME_LAW_UPDATE_TIME = "law_update_time";

    public static final String COLUMN_LAW_UPDATE_TIME = "update_time";

    // test related
    /**
     * for test activity
     */
    public static final String TABLE_NAME_TEST = "test";

    /**
     * for test fragment
     */
    public static final String TABLE_NAME_TEST_FRAGMENT = "test_frag";

    // plan related
    public static final String TABLE_NAME_PLAN = "plan";

    public static final String COLUMN_LAW_TYPE = "law_type";

    public static final String COLUMN_TOTAL_PROGRESS = "total_progress";

    public static final String COLUMN_CURRENT_PROGRESS = "current_progress";

    public static final String COLUMN_READING_ORDER = "reading_order";

    public static final String COLUMN_DATE = "date";

    // data related
    public static final String TABLE_NAME_LAW = "law";

    public static final String COLUMN_PART = "part";

    public static final String COLUMN_CHAPTER = "chapter";

    public static final String COLUMN_SECTION = "section";

    public static final String COLUMN_SUBSECTION = "subsection";

    public static final String COLUMN_LINE = "line";

    public static final String COLUMN_CONTENT = "content";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_TYPE = "law_type";

    public static final String COLUMN_WRONG_TIME = "wrong_time";

    public static final String COLUMN_HAS_ANSWERED_SIMPLE = "has_answered_simple";

    public static final String COLUMN_HAS_ANSWERED_COMPOSITE = "has_answered_composite";

    public static final String COLUMN_ORDER = "c_order";

    public static final String IGNORE_CONTENT = "¡]§R°£¡^";

    public static final String LAW_TABLE_COLUMN = " (" + COLUMN_CHAPTER + " TEXT, "
            + COLUMN_SECTION + " TEXT, " + COLUMN_PART + " TEXT, " + COLUMN_SUBSECTION + " TEXT, "
            + COLUMN_LINE + " TEXT, " + COLUMN_CONTENT + " TEXT, " + COLUMN_TYPE + " INTEGER,"
            + COLUMN_HAS_ANSWERED_SIMPLE + " INTEGER, " + COLUMN_HAS_ANSWERED_COMPOSITE
            + " INTEGER, " + COLUMN_ORDER + " INTEGER, " + COLUMN_WRONG_TIME
            + " INTEGER, PRIMARY KEY(" + COLUMN_LINE + ", " + COLUMN_TYPE + "))";

    public interface RefreshLawCallback {
        public void notifyDataChanged();
    }

    private final ArrayList<RefreshLawCallback> mRefreshLawCallback = new ArrayList<RefreshLawCallback>();

    public interface RefreshPlanCallback {
        public void notifyDataChanged();
    }

    private final ArrayList<RefreshPlanCallback> mRefreshPlanCallback = new ArrayList<RefreshPlanCallback>();

    private SQLiteDatabase mDb;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getDataBase().execSQL("PRAGMA synchronous = 1");
            setWriteAheadLoggingEnabled(true);
        }
        createTables();
    }

    public boolean isLawTableEmpty() {
        boolean rtn = true;
        Cursor c = getDataBase().rawQuery("select count(*) from " + TABLE_NAME_LAW, null);
        if (c != null) {
            c.moveToNext();
            rtn = c.getInt(0) == 0;
            c.close();
        }
        return rtn;
    }

    public void createTables() {

        // law table
        getDataBase().execSQL("CREATE TABLE if not exists " + TABLE_NAME_LAW + LAW_TABLE_COLUMN);
        // plan table
        getDataBase().execSQL(
                "CREATE TABLE if not exists " + TABLE_NAME_PLAN + " (" + COLUMN_LAW_TYPE
                        + " INTEGER PRIMARY KEY , " + COLUMN_DATE + " TEXT, "
                        + COLUMN_TOTAL_PROGRESS + " TEXT, " + COLUMN_CURRENT_PROGRESS + " TEXT, "
                        + COLUMN_READING_ORDER + " TEXT)");
        // test table (copy all column from TABLE_NAME_LAW)
        getDataBase().execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TEST + LAW_TABLE_COLUMN);
        getDataBase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TEST_FRAGMENT + LAW_TABLE_COLUMN);

        // law update time table
        getDataBase().execSQL(
                "CREATE TABLE if not exists " + TABLE_NAME_LAW_UPDATE_TIME + " (" + COLUMN_LAW_TYPE
                        + " INTEGER PRIMARY KEY , " + COLUMN_LAW_UPDATE_TIME + " TEXT)");

        // law paragraph
        getDataBase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_LAW_PARAGRAPH + "(" + COLUMN_TYPE
                        + " INTEGER, " + COLUMN_LAW_PARAGRAPH_TITLE + " TEXT)");
    }

    public ArrayList<LawPara> getLawParagraph(int lawType) {
        ArrayList<LawPara> rtn = new ArrayList<LawPara>();
        Cursor data = getDataBase().query(TABLE_NAME_LAW_PARAGRAPH, null,
                COLUMN_TYPE + "=" + lawType, null, null, null, null);
        if (data != null) {
            int typeIndex = data.getColumnIndex(COLUMN_TYPE);
            int titleIndex = data.getColumnIndex(COLUMN_LAW_PARAGRAPH_TITLE);
            while (data.moveToNext()) {
                rtn.add(new LawPara(data.getInt(typeIndex), data.getString(titleIndex)));
            }
            data.close();
        }
        return rtn;
    }

    public void insertLawParagraph(ArrayList<LawPara> para, int lawType) {
        getDataBase().delete(TABLE_NAME_LAW_PARAGRAPH, COLUMN_TYPE + "=" + lawType, null);
        try {
            getDataBase().beginTransaction();
            for (LawPara p : para) {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_TYPE, p.mLawType);
                cv.put(COLUMN_LAW_PARAGRAPH_TITLE, p.mTitle);
                try {
                    getDataBase().insertOrThrow(TABLE_NAME_LAW_PARAGRAPH, null, cv);
                } catch (Exception e) {
                }
            }
            getDataBase().setTransactionSuccessful();
        } finally {
            getDataBase().endTransaction();
        }
    }

    public void insertLawUpdateTime(int type, String time) {
        getDataBase().delete(TABLE_NAME_LAW_UPDATE_TIME, COLUMN_LAW_TYPE + "='" + type + "'", null);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAW_TYPE, type);
        cv.put(COLUMN_LAW_UPDATE_TIME, time);
        getDataBase().insert(TABLE_NAME_LAW_UPDATE_TIME, null, cv);
    }

    public Cursor getLawUpdateTime() {
        return getDataBase().query(TABLE_NAME_LAW_UPDATE_TIME, null, null, null, null, null, null);
    }

    public void clearTestFragmentData() {
        getDataBase().delete(TABLE_NAME_TEST_FRAGMENT, null, null);
    }

    public int getTestFragmentDataType() {
        int rtn = -1;
        Cursor data = getDataBase().query(TABLE_NAME_TEST_FRAGMENT, null, null, null, null, null,
                null);
        if (data != null) {
            while (data.moveToNext()) {
                rtn = data.getInt(data.getColumnIndex(COLUMN_LAW_TYPE));
                break;
            }
            data.close();
        }
        return rtn;
    }

    public void setTestFragmentData(int type, int testType) {
        if (testType == TestFragment.TEST_TYPE_BY_LAW) {
            getDataBase().execSQL(
                    "insert into " + TABLE_NAME_TEST_FRAGMENT + " select * from " + TABLE_NAME_LAW
                            + " where " + COLUMN_TYPE + "='" + type + "'");
        } else if (testType == TestFragment.TEST_TYPE_REVIEW) {
            PlanAttrs plan = getPlan(type);
            if (plan != null) {
                int count = getPlanTypeCount(type);
                --plan.mCurrentProgress;
                if (plan.mCurrentProgress < 0) {
                    plan.mCurrentProgress = 0;
                }
                int upperBound = CreatePlanFragment.getUpperBound(count, plan.mTotalProgress,
                        plan.mCurrentProgress);
                getDataBase().execSQL(
                        "insert into " + TABLE_NAME_TEST_FRAGMENT + "  select * from "
                                + TABLE_NAME_TEST + " where " + COLUMN_TYPE + "='" + type
                                + "' and " + COLUMN_ORDER + " <" + upperBound + " order by "
                                + COLUMN_ORDER + " ");
            } else {
                // should not be null
                Log.w(TAG, "failed, plan should not be null");
            }
        } else if (testType == TestFragment.TEST_TYPE_BY_LAW_RANDOM) {
            getDataBase().execSQL(
                    "insert into " + TABLE_NAME_TEST_FRAGMENT + " select * from " + TABLE_NAME_LAW
                            + " where " + COLUMN_TYPE + "='" + type
                            + "' order by RANDOM() LIMIT 50");
        }
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, LawAttrs.HAS_NOT_ANSWERED);
        cv.put(COLUMN_HAS_ANSWERED_SIMPLE, LawAttrs.HAS_NOT_ANSWERED);
        getDataBase().update(TABLE_NAME_TEST_FRAGMENT, cv, null, null);
    }

    public boolean hasPreviousDataInTestFragment() {
        int rtn = 0;
        Cursor data = getDataBase().rawQuery("select count(*) from " + TABLE_NAME_TEST_FRAGMENT,
                null);
        if (data != null) {
            data.moveToNext();
            rtn = data.getInt(0);
            data.close();
        }
        return rtn > 0;
    }

    public void addCallback(RefreshPlanCallback c) {
        if (mRefreshPlanCallback.contains(c) == false)
            mRefreshPlanCallback.add(c);
    }

    public void removeCallback(RefreshPlanCallback c) {
        mRefreshPlanCallback.remove(c);
    }

    public PlanAttrs getPlan(int type) {
        PlanAttrs rtn = null;
        Cursor data = getDataBase().query(TABLE_NAME_PLAN, null,
                COLUMN_LAW_TYPE + "='" + type + "'", null, null, null, null);
        if (data != null) {
            int typeI = data.getColumnIndex(COLUMN_LAW_TYPE);
            int tProgressI = data.getColumnIndex(COLUMN_TOTAL_PROGRESS);
            int cProgressI = data.getColumnIndex(COLUMN_CURRENT_PROGRESS);
            int rOrderI = data.getColumnIndex(COLUMN_READING_ORDER);
            int dateI = data.getColumnIndex(COLUMN_DATE);
            while (data.moveToNext()) {
                rtn = new PlanAttrs(data.getInt(typeI), data.getInt(rOrderI),
                        data.getInt(tProgressI), data.getInt(cProgressI), data.getInt(dateI));
            }
            data.close();
        }
        return rtn;
    }

    public ArrayList<PlanAttrs> getAllPlans() {
        ArrayList<PlanAttrs> rtn = new ArrayList<PlanAttrs>();
        Cursor data = getDataBase().query(TABLE_NAME_PLAN, null, null, null, null, null, null);
        if (data != null) {
            int typeI = data.getColumnIndex(COLUMN_LAW_TYPE);
            int tProgressI = data.getColumnIndex(COLUMN_TOTAL_PROGRESS);
            int cProgressI = data.getColumnIndex(COLUMN_CURRENT_PROGRESS);
            int rOrderI = data.getColumnIndex(COLUMN_READING_ORDER);
            int dateI = data.getColumnIndex(COLUMN_DATE);
            while (data.moveToNext()) {
                rtn.add(new PlanAttrs(data.getInt(typeI), data.getInt(rOrderI), data
                        .getInt(tProgressI), data.getInt(cProgressI), data.getInt(dateI)));
            }
            data.close();
        }
        return rtn;
    }

    public ArrayList<LawAttrs> getPlanDataFromTestFragment() {
        ArrayList<LawAttrs> rtn = null;
        Cursor data = null;
        String whereClause = "";
        whereClause = COLUMN_CONTENT + " !='" + IGNORE_CONTENT + "' and "
                + COLUMN_HAS_ANSWERED_COMPOSITE + "!='" + LawAttrs.HAS_ANSWERED + "'";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            data = getDataBase().query(true, TABLE_NAME_TEST_FRAGMENT, null, whereClause, null,
                    null, null, COLUMN_ORDER, null, null);
        } else {
            data = getDataBase().query(TABLE_NAME_TEST_FRAGMENT, null, whereClause, null, null,
                    null, COLUMN_ORDER);
        }
        rtn = convertFromCursorToLawAttrs(data);
        return rtn;
    }

    public ArrayList<LawAttrs> getPlanDataFromLawTable(int planType, boolean ignoreContent,
            boolean orderByWrongType) {
        ArrayList<LawAttrs> rtn = null;
        Cursor data = null;
        String whereClause = "";
        if (ignoreContent) {
            whereClause = COLUMN_TYPE + "='" + planType + "' and " + COLUMN_CONTENT + " !='"
                    + IGNORE_CONTENT + "'";
        } else {
            whereClause = COLUMN_TYPE + "='" + planType + "'";
        }
        String order = "";
        if (orderByWrongType) {
            order += COLUMN_WRONG_TIME + " desc";
        } else {
            order = COLUMN_ORDER;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            data = getDataBase().query(true, TABLE_NAME_LAW, null, whereClause, null, null, null,
                    order, null, null);
        } else {
            data = getDataBase().query(TABLE_NAME_LAW, null, whereClause, null, null, null, order);
        }
        rtn = convertFromCursorToLawAttrs(data);
        return rtn;
    }

    public ArrayList<LawAttrs> getPlanData(PlanAttrs plan, boolean ignoreContent) {
        return getPlanData(plan.mPlanType, ignoreContent);
    }

    public ArrayList<LawAttrs> getPlanData(int planType, boolean ignoreContent) {
        ArrayList<LawAttrs> rtn = null;
        Cursor data = null;
        String whereClause = "";
        if (ignoreContent) {
            whereClause = COLUMN_TYPE + "='" + planType + "' and " + COLUMN_CONTENT + " !='"
                    + IGNORE_CONTENT + "'";
        } else {
            whereClause = COLUMN_TYPE + "='" + planType + "'";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            data = getDataBase().query(true, TABLE_NAME_TEST, null, whereClause, null, null, null,
                    COLUMN_ORDER, null, null);
        } else {
            data = getDataBase().query(TABLE_NAME_TEST, null, whereClause, null, null, null, null);
        }

        rtn = convertFromCursorToLawAttrs(data);
        return rtn;
    }

    public void addNewPlan(PlanAttrs plan) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAW_TYPE, plan.mPlanType);
        cv.put(COLUMN_TOTAL_PROGRESS, plan.mTotalProgress);
        cv.put(COLUMN_CURRENT_PROGRESS, plan.mCurrentProgress);
        cv.put(COLUMN_READING_ORDER, plan.mReadingOrder);
        cv.put(COLUMN_DATE, plan.mDate);
        getDataBase().insert(TABLE_NAME_PLAN, null, cv);
        for (RefreshPlanCallback c : mRefreshPlanCallback) {
            c.notifyDataChanged();
        }
        Cursor data = getDataBase().query(TABLE_NAME_LAW, null,
                COLUMN_TYPE + "='" + plan.mPlanType + "'", null, null, null, null);
        ArrayList<ContentValues> randomList = new ArrayList<ContentValues>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        int rCount = 0;
        if (data != null) {
            while (data.moveToNext()) {
                cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(data, cv);
                order.add(rCount++);
                randomList.add(cv);
            }
            data.close();
        }
        if (randomList.isEmpty() == false) {
            if (plan.mReadingOrder == CreatePlanFragment.READING_ORDER_RANDOM) {
                Collections.shuffle(order);
            }
            try {
                getDataBase().beginTransaction();
                for (int i = 0; i < randomList.size(); i++) {
                    ContentValues c = randomList.get(i);
                    c.put(COLUMN_ORDER, order.get(i));
                    c.put(COLUMN_HAS_ANSWERED_COMPOSITE, LawAttrs.HAS_NOT_ANSWERED);
                    c.put(COLUMN_HAS_ANSWERED_SIMPLE, LawAttrs.HAS_NOT_ANSWERED);
                    getDataBase().insert(TABLE_NAME_TEST, null, c);
                }
                getDataBase().setTransactionSuccessful();
            } finally {
                getDataBase().endTransaction();
            }
        }
    }

    public void updatePlan(PlanAttrs plan) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TOTAL_PROGRESS, plan.mTotalProgress);
        cv.put(COLUMN_CURRENT_PROGRESS, plan.mCurrentProgress);
        cv.put(COLUMN_DATE, plan.mDate);
        getDataBase().update(TABLE_NAME_PLAN, cv, COLUMN_LAW_TYPE + "='" + plan.mPlanType + "'",
                null);
        for (RefreshPlanCallback c : mRefreshPlanCallback) {
            c.notifyDataChanged();
        }
    }

    public void deletePlan(int type) {
        getDataBase().delete(TABLE_NAME_PLAN, COLUMN_LAW_TYPE + "='" + type + "'", null);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HAS_ANSWERED_SIMPLE, LawAttrs.HAS_NOT_ANSWERED);
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, LawAttrs.HAS_NOT_ANSWERED);
        getDataBase().update(TABLE_NAME_LAW, cv, COLUMN_TYPE + "='" + type + "'", null);
        for (RefreshPlanCallback c : mRefreshPlanCallback) {
            c.notifyDataChanged();
        }
        getDataBase().delete(TABLE_NAME_TEST, COLUMN_TYPE + "='" + type + "'", null);
    }

    public void clearAllPlans() {
        getDataBase().delete(TABLE_NAME_PLAN, null, null);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HAS_ANSWERED_SIMPLE, LawAttrs.HAS_NOT_ANSWERED);
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, LawAttrs.HAS_NOT_ANSWERED);
        getDataBase().update(TABLE_NAME_LAW, cv, null, null);
        for (RefreshPlanCallback c : mRefreshPlanCallback) {
            c.notifyDataChanged();
        }
        getDataBase().delete(TABLE_NAME_TEST, null, null);
    }

    public ArrayList<Integer> getAvailableLawTypes() {
        ArrayList<Integer> availableLawTypes = getAllLawTypes();
        Cursor data = getDataBase().rawQuery(
                "select " + COLUMN_LAW_TYPE + " from " + TABLE_NAME_PLAN, null);
        if (data != null) {
            while (data.moveToNext()) {
                availableLawTypes.remove((Integer)data.getInt(0));
            }
            data.close();
        }
        return availableLawTypes;
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
            int wrongIndex = c.getColumnIndex(COLUMN_WRONG_TIME);
            int answeredIndex = c.getColumnIndex(COLUMN_HAS_ANSWERED_SIMPLE);
            int answeredCompositeIndex = c.getColumnIndex(COLUMN_HAS_ANSWERED_COMPOSITE);
            while (c.moveToNext()) {
                rtn.add(new LawAttrs(c.getString(partIndex), c.getString(chapterIndex), c
                        .getString(sectionIndex), c.getString(subSectionIndex), c
                        .getString(lineIndex), c.getString(contentIndex), c.getInt(wrongIndex), c
                        .getInt(answeredIndex), c.getInt(answeredCompositeIndex)));
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
            cv.put(COLUMN_WRONG_TIME, attr.mWrongTime);
            cv.put(COLUMN_HAS_ANSWERED_SIMPLE, attr.mHasAnsweredSimple);
            cv.put(COLUMN_TYPE, type);
            cvs.add(cv);
        }
        return cvs;
    }

    public int getPlanTypeCount(int type) {
        int rtn = 0;
        Cursor data = getDataBase().rawQuery(
                "select count(*) from " + TABLE_NAME_LAW + " where " + COLUMN_TYPE + "='" + type
                        + "'", null);
        if (data != null) {
            data.moveToNext();
            rtn = data.getInt(0);
            data.close();
        }
        return rtn;
    }

    public ArrayList<LawAttrs> convertFromContentValuesToLawAttrs(ArrayList<ContentValues> cvs) {
        ArrayList<LawAttrs> rtn = new ArrayList<LawAttrs>();
        for (ContentValues cv : cvs) {
            rtn.add(new LawAttrs(cv.getAsString(COLUMN_PART), cv.getAsString(COLUMN_CHAPTER), cv
                    .getAsString(COLUMN_SECTION), cv.getAsString(COLUMN_SUBSECTION), cv
                    .getAsString(COLUMN_LINE), cv.getAsString(COLUMN_CONTENT), cv
                    .getAsInteger(COLUMN_WRONG_TIME), cv.getAsInteger(COLUMN_HAS_ANSWERED_SIMPLE),
                    cv.getAsInteger(COLUMN_HAS_ANSWERED_COMPOSITE)));
        }
        return rtn;
    }

    public void addCallback(RefreshLawCallback c) {
        if (mRefreshLawCallback.contains(c) == false)
            mRefreshLawCallback.add(c);
    }

    public void removeCallback(RefreshLawCallback c) {
        mRefreshLawCallback.remove(c);
    }

    public void resetSimpleTestStatus(int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HAS_ANSWERED_SIMPLE, LawAttrs.HAS_NOT_ANSWERED);
        getDataBase().update(TABLE_NAME_TEST, cv, COLUMN_TYPE + "='" + type + "'", null);
    }

    public void resetCompositeTestStatus(int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, LawAttrs.HAS_NOT_ANSWERED);
        getDataBase().update(TABLE_NAME_TEST, cv, COLUMN_TYPE + "='" + type + "'", null);
    }

    public void updateSimpleTestStatus(LawAttrs attr, int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WRONG_TIME, attr.mWrongTime);
        getDataBase().update(TABLE_NAME_LAW, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
        cv.put(COLUMN_HAS_ANSWERED_SIMPLE, attr.mHasAnsweredSimple);
        getDataBase().update(TABLE_NAME_TEST, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
        getDataBase().update(TABLE_NAME_TEST, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
    }

    public void updateCompositeTestStatus(LawAttrs attr, int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WRONG_TIME, attr.mWrongTime);
        getDataBase().update(TABLE_NAME_LAW, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, attr.mHasAnsweredComposite);
        getDataBase().update(TABLE_NAME_TEST, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
    }

    public void updateCompositeTestFragmentStatus(LawAttrs attr, int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WRONG_TIME, attr.mWrongTime);
        getDataBase().update(TABLE_NAME_LAW, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
        cv.put(COLUMN_HAS_ANSWERED_COMPOSITE, attr.mHasAnsweredComposite);
        getDataBase().update(TABLE_NAME_TEST_FRAGMENT, cv,
                COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + attr.mLine + "'", null);
    }

    /**
     * clear all type data and insert again
     * 
     * @param list
     * @param type
     */
    public void updateLawTable(ArrayList<LawAttrs> list, int type) {
        ArrayList<ContentValues> cvs = convertFromLawAttrsToContentValues(list, type);
        try {
            getDataBase().beginTransaction();
            for (ContentValues cv : cvs) {
                try {
                    getDataBase().insertOrThrow(TABLE_NAME_LAW, null, cv);
                } catch (Exception e) {
                    String line = cv.getAsString(COLUMN_LINE);
                    getDataBase().update(TABLE_NAME_LAW, cv,
                            COLUMN_TYPE + "='" + type + "' and " + COLUMN_LINE + "='" + line + "'",
                            null);
                }
            }
            getDataBase().setTransactionSuccessful();
        } finally {
            getDataBase().endTransaction();
        }
        for (RefreshLawCallback c : mRefreshLawCallback) {
            c.notifyDataChanged();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
