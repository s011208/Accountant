
package com.bj4.yhh.accountant.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

public class CompositeTestActivity extends Activity {

    protected PlanAttrs mPlan;

    protected DatabaseHelper mDatabaseHelper;

    protected ArrayList<LawAttrs> mLaws;

    protected String mFixedTitle;

    private Button mOption1, mOption2, mOption3, mOption4;

    private TextView mTitle, mProgressHint, mQuestion;

    private Button mCompelete, mNext;

    private int mAnswerOption = 0;

    private int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.composite_test_activity);
        mTitle = (TextView)findViewById(R.id.law_name);
        mQuestion = (TextView)findViewById(R.id.question);
        mOption1 = (Button)findViewById(R.id.option1);
        mOption2 = (Button)findViewById(R.id.option2);
        mOption3 = (Button)findViewById(R.id.option3);
        mOption4 = (Button)findViewById(R.id.option4);
        mOption1.setOnClickListener(mOptionClickListener);
        mOption2.setOnClickListener(mOptionClickListener);
        mOption3.setOnClickListener(mOptionClickListener);
        mOption4.setOnClickListener(mOptionClickListener);
        mOption1.setVisibility(View.GONE);
        mOption2.setVisibility(View.GONE);
        mOption3.setVisibility(View.GONE);
        mOption4.setVisibility(View.GONE);
        mCompelete = (Button)findViewById(R.id.complete);
        mNext = (Button)findViewById(R.id.next);
        mNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                generateQuestion();
            }
        });
        mProgressHint = (TextView)findViewById(R.id.progress_hint);
    }

    private OnClickListener mOptionClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int selectedOption = 0;
            switch (v.getId()) {
                case R.id.option1:
                    selectedOption = 0;
                    break;
                case R.id.option2:
                    selectedOption = 1;
                    break;
                case R.id.option3:
                    selectedOption = 2;
                    break;
                case R.id.option4:
                    selectedOption = 3;
                    break;
            }
            if (selectedOption != mAnswerOption) {
                if (mAnswerOption == 0) {
                    mOption1.setBackgroundColor(0xcc00ff00);
                    mOption1.setTextColor(Color.BLACK);
                } else if (mAnswerOption == 1) {
                    mOption2.setBackgroundColor(0xcc00ff00);
                    mOption2.setTextColor(Color.BLACK);
                } else if (mAnswerOption == 2) {
                    mOption3.setBackgroundColor(0xcc00ff00);
                    mOption3.setTextColor(Color.BLACK);
                } else if (mAnswerOption == 3) {
                    mOption4.setBackgroundColor(0xcc00ff00);
                    mOption4.setTextColor(Color.BLACK);
                }
                mNext.setVisibility(View.VISIBLE);
                v.setBackgroundColor(0xccff0000);
                mOption1.setEnabled(false);
                mOption2.setEnabled(false);
                mOption3.setEnabled(false);
                mOption4.setEnabled(false);
            } else {
                generateQuestion();
            }
        }
    };

    private void generateQuestion() {
        if (mLaws.isEmpty()) {
            return;
        }
        mOption1.setVisibility(View.VISIBLE);
        mOption2.setVisibility(View.VISIBLE);
        mOption3.setVisibility(View.VISIBLE);
        mOption4.setVisibility(View.VISIBLE);
        mNext.setVisibility(View.GONE);
        mOption1.setBackgroundResource(R.drawable.question_button_btn);
        mOption2.setBackgroundResource(R.drawable.question_button_btn);
        mOption3.setBackgroundResource(R.drawable.question_button_btn);
        mOption4.setBackgroundResource(R.drawable.question_button_btn);
        mOption1.setEnabled(true);
        mOption2.setEnabled(true);
        mOption3.setEnabled(true);
        mOption4.setEnabled(true);
        int type = (int)((Math.random() * 100) % 2);
        mCurrentIndex = (int)((Math.random() * 10000) % mLaws.size());
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
        mAnswerOption = (int)((Math.random() * 100) % 4);
        switch (type) {
            case BaseTestActivity.QUESTION_TYPE_LINE:
                question = law.mLine;
                answer = mLaws.get(mCurrentIndex).mContent;
                mQuestion.setGravity(Gravity.CENTER);
                break;
            case BaseTestActivity.QUESTION_TYPE_CONTENT:
                answer = law.mLine;
                question = mLaws.get(mCurrentIndex).mContent;
                mQuestion.setGravity(Gravity.LEFT);
                break;
        }
        mQuestion.setText(question);
        ArrayList<String> restOptions = getRestOptions(3, mCurrentIndex, type);
        if (mAnswerOption == 0) {
            mOption1.setText(answer);
            mOption2.setText(restOptions.get(0));
            mOption3.setText(restOptions.get(1));
            mOption4.setText(restOptions.get(2));
        } else if (mAnswerOption == 1) {
            mOption2.setText(answer);
            mOption1.setText(restOptions.get(0));
            mOption3.setText(restOptions.get(1));
            mOption4.setText(restOptions.get(2));
        } else if (mAnswerOption == 2) {
            mOption3.setText(answer);
            mOption2.setText(restOptions.get(0));
            mOption1.setText(restOptions.get(1));
            mOption4.setText(restOptions.get(2));
        } else if (mAnswerOption == 3) {
            mOption4.setText(answer);
            mOption2.setText(restOptions.get(0));
            mOption3.setText(restOptions.get(1));
            mOption1.setText(restOptions.get(2));
        }
    }

    private ArrayList<String> getRestOptions(int optionNumber, int currentIndex, int questionType) {
        ArrayList<String> rtn = new ArrayList<String>();
        for (int i = 0; i < optionNumber; i++) {
            int index = 0;
            do {
                index = (int)((Math.random() * 10000) % mLaws.size());
            } while (index == currentIndex);
            rtn.add(questionType == BaseTestActivity.QUESTION_TYPE_LINE ? mLaws.get(index).mContent
                    : mLaws.get(index).mLine);
        }

        return rtn;
    }

    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        Intent intent = getIntent();
        int planType = 0;
        if (intent != null) {
            planType = intent.getIntExtra(BaseTestActivity.INTENT_PLAN_TYPE, -1);
        }
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(this);
        mPlan = mDatabaseHelper.getPlan(planType);
        if (mPlan == null) {
            finish();
        }
        mLaws = mDatabaseHelper.query(mPlan.mPlanType);
        mFixedTitle = getResources().getString(GovLawParser.getTypeTextResource(mPlan.mPlanType));
        if (mTitle != null) {
            mTitle.setText(mFixedTitle);
        }
    }

}
