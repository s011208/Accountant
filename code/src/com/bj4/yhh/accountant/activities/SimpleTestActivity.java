
package com.bj4.yhh.accountant.activities;

import java.util.ArrayList;

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

public class SimpleTestActivity extends Activity {

    public static final String TAG = "QQQQ";

    public static final String INTENT_PLAN_TYPE = "intent_plan_type";

    public static final int QUESTION_TYPE_LINE = 0;

    public static final int QUESTION_TYPE_CONTENT = 1;

    private PlanAttrs mPlan;

    private DatabaseHelper mDatabaseHelper;

    private TextView mTitle;

    private TextView mQuestion;

    private Button mYes, mNo;

    private ArrayList<LawAttrs> mLaws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_test_activity);
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
        init();
    }

    private void init() {
        mTitle = (TextView)findViewById(R.id.law_name);
        mTitle.setText(GovLawParser.getTypeTextResource(mPlan.mPlanType));
        mQuestion = (TextView)findViewById(R.id.question);
        mYes = (Button)findViewById(R.id.yes);
        generateQuestion();
        mYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuestion();
            }
        });
        mNo = (Button)findViewById(R.id.no);
        mNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuestion();
            }
        });
    }

    private void generateQuestion() {
        int type = (int)((Math.random() * 100) % 2);
        int index = (int)((Math.random() * 100) % mLaws.size());
        String question = "";
        switch (type) {
            case QUESTION_TYPE_LINE:
                LawAttrs law = mLaws.get(index);
                StringBuilder sb = new StringBuilder();
                if (Integer.valueOf(law.mPart) != 0) {
                    sb.append(" 第 " + law.mPart + " 編 ");
                }
                if (Integer.valueOf(law.mChapter) != 0) {
                    sb.append(" 第 " + law.mChapter + " 章 ");
                }
                if (Integer.valueOf(law.mSection) != 0) {
                    sb.append(" 第 " + law.mSection + " 節 ");
                }
                if (Integer.valueOf(law.mSubSection) != 0) {
                    sb.append(" 第 " + law.mSubSection + " 目 ");
                }
                sb.append(law.mLine);
                question = sb.toString();
                mQuestion.setGravity(Gravity.CENTER);
                break;
            case QUESTION_TYPE_CONTENT:
                question = mLaws.get(index).mContent;
                mQuestion.setGravity(Gravity.LEFT);
                break;
        }
        mQuestion.setText(question);
    }
}
