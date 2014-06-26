
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

public class SimpleTestActivity extends BaseTestActivity {
    public static final String TAG = "SimpleTestActivity";

    private TextView mTitle, mProgressHint;

    private TextView mQuestion, mAnswer;

    private Button mYes, mNo, mNext, mComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_test_activity);
        init();
    }

    public void refreshData() {
        super.refreshData();
        mTitle.setText(mFixedTitle);
    }

    public void onResume() {
        super.onResume();
    }

    private void finishTest() {
        mNo.setVisibility(View.GONE);
        mYes.setVisibility(View.GONE);
        mNext.setVisibility(View.GONE);
        mComplete.setVisibility(View.VISIBLE);
        mQuestion.setVisibility(View.GONE);
        mAnswer.setVisibility(View.GONE);
        mProgressHint.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), "done", Toast.LENGTH_SHORT).show();
    }

    private void init() {
        mTitle = (TextView)findViewById(R.id.law_name);
        mQuestion = (TextView)findViewById(R.id.question);
        mAnswer = (TextView)findViewById(R.id.answer);
        mProgressHint = (TextView)findViewById(R.id.progress_hint);
        mYes = (Button)findViewById(R.id.yes);
        mYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LawAttrs law = mLaws.get(mCurrentIndex);
                law.mHasAnswered = LawAttrs.HAS_ANSWERED;
                mDatabaseHelper.updateTestStatus(law, mPlan.mPlanType);
                mLaws.remove(mCurrentIndex);
                --mUpperBound;
                if (mLaws.isEmpty()) {
                    finishTest();
                } else {
                    generateQuestion();
                }
            }
        });
        mNo = (Button)findViewById(R.id.no);
        mNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LawAttrs law = mLaws.get(mCurrentIndex);
                ++law.mWrongTime;
                mDatabaseHelper.updateTestStatus(law, mPlan.mPlanType);
                mAnswer.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.GONE);
                mYes.setVisibility(View.GONE);
                mNext.setVisibility(View.VISIBLE);
            }
        });
        mNext = (Button)findViewById(R.id.next);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuestion();
                mNext.setVisibility(View.GONE);
                mNo.setVisibility(View.VISIBLE);
                mYes.setVisibility(View.VISIBLE);
            }
        });
        mComplete = (Button)findViewById(R.id.complete);
        mComplete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void generateQuestion() {
        if (mLaws.isEmpty()) {
            finishTest();
            return;
        } else {
            mProgressHint.setText(getBaseContext().getString(R.string.rest_items) + mLaws.size());
        }
        int type = (int)((Math.random() * 100) % 2);
        mCurrentIndex = (int)((Math.random() * 10000) % mUpperBound);
        String question = "";
        LawAttrs law = mLaws.get(mCurrentIndex);
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
                answer = mLaws.get(mCurrentIndex).mContent;
                mQuestion.setGravity(Gravity.CENTER);
                mAnswer.setGravity(Gravity.LEFT);
                break;
            case QUESTION_TYPE_CONTENT:
                answer = law.mLine;
                question = mLaws.get(mCurrentIndex).mContent;
                mQuestion.setGravity(Gravity.LEFT);
                mAnswer.setGravity(Gravity.CENTER);
                break;
        }
        mQuestion.setText(question);
        mAnswer.setText(answer);
        mAnswer.setVisibility(View.GONE);
    }
}
