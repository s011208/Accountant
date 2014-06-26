
package com.bj4.yhh.accountant.activities;

import java.util.ArrayList;
import java.util.Iterator;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseTestActivity extends Activity {

    public static final String TAG = "BaseTestActivity";

    public static final String INTENT_PLAN_TYPE = "intent_plan_type";

    public static final int QUESTION_TYPE_LINE = 0;

    public static final int QUESTION_TYPE_CONTENT = 1;

    protected PlanAttrs mPlan;

    protected DatabaseHelper mDatabaseHelper;

    protected ArrayList<LawAttrs> mLaws;

    protected String mFixedTitle;

    protected int mUpperBound = 0;

    protected int mLowerBound = 0;

    protected int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    public void refreshData() {
        Intent intent = getIntent();
        int planType = 0;
        if (intent != null) {
            planType = intent.getIntExtra(INTENT_PLAN_TYPE, -1);
            Log.d(TAG, "plan type: " + planType);
        }
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(this);
        mPlan = mDatabaseHelper.getPlan(planType);
        if (mPlan == null) {
            Log.w(TAG, "should not be null");
            finish();
        }
        mLaws = mDatabaseHelper.query(mPlan.mPlanType);
        mFixedTitle = getResources().getString(GovLawParser.getTypeTextResource(mPlan.mPlanType));
        setBound();
        generateQuestion();
    }

    protected void setBound() {
        int unit = (int)(Math.ceil(mLaws.size() / (mPlan.mTotalProgress - 1)));
        mUpperBound = unit * (mPlan.mCurrentProgress + 1);
        mLowerBound = unit * (mPlan.mCurrentProgress - 1);
        mLowerBound = mLowerBound < 0 ? 0 : mLowerBound;
        Iterator<LawAttrs> iter = mLaws.iterator();
        int counter = 0;
        Log.d(TAG, "mUpperBound: " + mUpperBound + ", mLowerBound: " + mLowerBound
                + ", mLaws.size(): " + mLaws.size());
        int tempUpperBound = mUpperBound;
        int tempLowerBound = mLowerBound;
        int debugAnswered = 0;
        int debugNotAnswered = 0;
        while (iter.hasNext()) {
            LawAttrs law = iter.next();
            if (counter < mLowerBound) {
                if ((Math.random() * 10) % 2 == 0) {
                    iter.remove();
                    tempUpperBound--;
                    tempLowerBound--;
                    // randomly remove lower bound
                }
            } else if (counter < mUpperBound) {
                if (law.mHasAnswered == LawAttrs.HAS_ANSWERED) {
                    ++debugAnswered;
                    iter.remove();
                    tempUpperBound--;
                } else {
                    ++debugNotAnswered;
                }
            } else {
                iter.remove();
            }
            ++counter;
        }
        mUpperBound = tempUpperBound;
        mLowerBound = tempLowerBound;

        Log.i(TAG, "mUpperBound: " + mUpperBound + ", mLowerBound: " + mLowerBound
                + ", mLaws.size(): " + mLaws.size() + ", debugAnswered: " + debugAnswered
                + ", debugNotAnswered: " + debugNotAnswered);
    }

    public void onResume() {
        super.onResume();
        refreshData();
    }

    public abstract void generateQuestion();
}
