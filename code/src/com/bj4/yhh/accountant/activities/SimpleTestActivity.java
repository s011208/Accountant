
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

    private TextView mQuestion, mAnswer;

    private Button mYes, mNo, mNext;

    private ArrayList<LawAttrs> mLaws;

    private String mFixedTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_test_activity);
        init();
    }

    private void refreshData() {
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
        mTitle.setText(mFixedTitle);
        generateQuestion();
    }

    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void init() {
        mTitle = (TextView)findViewById(R.id.law_name);
        mQuestion = (TextView)findViewById(R.id.question);
        mAnswer = (TextView)findViewById(R.id.answer);
        mYes = (Button)findViewById(R.id.yes);
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
                mAnswer.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.GONE);
                mYes.setVisibility(View.GONE);
                mNext.setVisibility(View.VISIBLE);
            }
        });
        mNext =  (Button)findViewById(R.id.next);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuestion();
                mNext.setVisibility(View.GONE);
                mNo.setVisibility(View.VISIBLE);
                mYes.setVisibility(View.VISIBLE);
            }
        });
    }

    private void generateQuestion() {
        int type = (int)((Math.random() * 100) % 2);
        int index = (int)((Math.random() * 10000) % mLaws.size());
        String question = "";
        LawAttrs law = mLaws.get(index);
        StringBuilder title = new StringBuilder();
        title.append("\n");
        if (Integer.valueOf(law.mPart) != 0) {
            title.append(" 第 " + law.mPart + " 編 ");
        }
        if (Integer.valueOf(law.mChapter) != 0) {
            title.append(" 第 " + law.mChapter + " 章 ");
        }
        if (Integer.valueOf(law.mSection) != 0) {
            title.append(" 第 " + law.mSection + " 節 ");
        }
        if (Integer.valueOf(law.mSubSection) != 0) {
            title.append(" 第 " + law.mSubSection + " 目 ");
        }
        mTitle.setText(mFixedTitle + title);
        String answer = "";
        switch (type) {
            case QUESTION_TYPE_LINE:
                question = law.mLine;
                answer = mLaws.get(index).mContent;
                mQuestion.setGravity(Gravity.CENTER);
                break;
            case QUESTION_TYPE_CONTENT:
                answer = law.mLine;
                question = mLaws.get(index).mContent;
                mQuestion.setGravity(Gravity.LEFT);
                break;
        }
        mQuestion.setText(question);
        mAnswer.setText(answer);
        mAnswer.setVisibility(View.GONE);
    }
}
