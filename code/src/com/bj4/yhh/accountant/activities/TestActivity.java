
package com.bj4.yhh.accountant.activities;

import java.util.ArrayList;
import java.util.Iterator;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.dialogs.ConfirmToExitDialog;
import com.bj4.yhh.accountant.fragments.CreatePlanFragment;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class TestActivity extends BaseActivity {

    public static final boolean DEBUG = false;

    public static final String TAG = "QQQQ";

    public static final String INTENT_PLAN_TYPE = "intent_plan_type";

    public static final String INTENT_DISPLAY_CHILD = "intent_displayed_child";

    public static final String INTENT_FULL_TEST = "intent_full_test";

    public static final String INTENT_FROM_TEST_FRAGMENT = "from_test_fragment";

    public static final int QUESTION_TYPE_LINE = 0;

    public static final int QUESTION_TYPE_CONTENT = 1;

    public static final int DISPLAY_CHILD_SIMPLE_TEST = 0;

    public static final int DISPLAY_CHILD_REAL_TEST = 1;

    private boolean mFromTestFragment = false;

    private boolean mFullTest = false;

    private RelativeLayout mMainBackground;

    private TextView mTitle;

    private PlanAttrs mPlan;

    private DatabaseHelper mDatabaseHelper;

    private ArrayList<LawAttrs> mQuestionList, mLaws;

    private String mFixedTitle;

    private int mCurrentIndex = 0;

    private TextView mProgressHint;

    private ViewSwitcher mSwitcher;

    private int mDisplayChild = DISPLAY_CHILD_SIMPLE_TEST;

    // simple test
    private TextView mQuestion, mAnswer;

    private Button mYes, mNo, mNext, mComplete;

    private ScrollView mSimpleScrollView;

    // real test

    private TextView mQuestionReal;

    private ScrollView mRealQuestionScrollView, mRealOptionScrollView;

    private Button mOption1, mOption2, mOption3, mOption0;

    private int mAnswerOption = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        init();
        initSimpleTestView();
        initRealTestView();
        refreshData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        refreshData();
    }

    private void refreshData() {
        mCurrentIndex = 0;
        mAnswerOption = 0;
        mQuestionList.clear();
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(this);
        Intent intent = getIntent();
        int planType = 0;
        if (intent != null) {
            planType = intent.getIntExtra(INTENT_PLAN_TYPE, -1);
            mDisplayChild = intent.getIntExtra(INTENT_DISPLAY_CHILD, DISPLAY_CHILD_SIMPLE_TEST);
            mFullTest = intent.getBooleanExtra(INTENT_FULL_TEST, false);
            mFromTestFragment = intent.getBooleanExtra(INTENT_FROM_TEST_FRAGMENT, false);
        }
        if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
            mComplete.setText(R.string.start_composite_test);
        }
        mSwitcher.setDisplayedChild(mDisplayChild);
        mPlan = mDatabaseHelper.getPlan(planType);
        if (mFromTestFragment) {
            mLaws = mDatabaseHelper.getPlanDataFromTestFragment();
            mPlan = new PlanAttrs(planType, CreatePlanFragment.READING_ORDER_CHAPTER, 0, 0, 0);
        } else {
            mLaws = mDatabaseHelper.getPlanData(mPlan, true);
        }
        if (mPlan == null) {
            Log.w(TAG, "should not be null");
            finish();
        }
        mFixedTitle = getResources().getString(GovLawParser.getTypeTextResource(mPlan.mPlanType));
        if (mTitle != null) {
            mTitle.setText(mFixedTitle);
        }
        setBound();
        generateQuestion();
    }

    private void resetWidgetStatusOnResume() {
        if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
            if (mQuestionList.isEmpty() == false) {
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.GONE);
                mComplete.setVisibility(View.GONE);
            }
        } else {
            mYes.setVisibility(View.GONE);
            mNo.setVisibility(View.GONE);
            mNext.setVisibility(View.GONE);
            mComplete.setVisibility(View.GONE);
            mProgressHint.setVisibility(View.VISIBLE);
        }
    }

    private void setBound() {
        if (DEBUG)
            Log.d(TAG, "setBound");
        ArrayList<LawAttrs> laws;
        if (mFromTestFragment) {
            laws = mDatabaseHelper.getPlanDataFromTestFragment();
        } else {
            laws = mDatabaseHelper.getPlanData(mPlan, false);
        }
        int unit = laws.size() / (mPlan.mTotalProgress - 1);
        int bound[] = getTestBound(laws.size(), mPlan.mTotalProgress, mPlan.mCurrentProgress);
        int upperBound = bound[0];
        int lowerBound = bound[1];
        if (mFullTest || mFromTestFragment) {
            upperBound = laws.size();
            lowerBound = 0;
            // test all laws at last day
        }
        for (int i = lowerBound; i < upperBound; i++) {
            LawAttrs law = laws.get(i);
            if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
                if (law.mHasAnsweredSimple == LawAttrs.HAS_NOT_ANSWERED) {
                    if (DatabaseHelper.IGNORE_CONTENT.equals(law.mContent)) {
                        // ignore deleted law
                    } else {
                        mQuestionList.add(laws.get(i));
                    }
                }
            } else {
                if (law.mHasAnsweredComposite == LawAttrs.HAS_NOT_ANSWERED) {
                    if (DatabaseHelper.IGNORE_CONTENT.equals(law.mContent)) {
                        // ignore deleted law
                    } else {
                        mQuestionList.add(laws.get(i));
                    }
                } else {
                }
            }
        }
        // add previous questions
        if (mQuestionList.isEmpty() == false && mFromTestFragment == false && mFullTest == false) {
            int tempLowerBound = lowerBound;
            if (tempLowerBound > 0) {
                int randomCount = 0;
                while (randomCount <= unit || tempLowerBound == 0) {
                    int randomIndex = (int)((Math.random() * 10000) % tempLowerBound);
                    LawAttrs law = laws.remove(randomIndex);
                    if (DatabaseHelper.IGNORE_CONTENT.equals(law.mContent) == false) {
                        mQuestionList.add(law);
                        ++randomCount;
                    }
                    --tempLowerBound;
                }
            }
        }
        if (DEBUG)
            Log.d(TAG,
                    "mQuestionList size: " + mQuestionList.size() + ", law size: " + mLaws.size());
    }

    @Override
    public void onBackPressed() {
        if (SettingManager.getInstance(getApplicationContext()).showTestActivityExitDialog()) {
            new ConfirmToExitDialog(new ConfirmToExitDialog.Callback() {

                @Override
                public void onClick(int result) {
                    if (result == ConfirmToExitDialog.OK) {
                        TestActivity.super.onBackPressed();
                    } else {
                        // temp leave
                        // Intent setIntent = new Intent(Intent.ACTION_MAIN);
                        // setIntent.addCategory(Intent.CATEGORY_HOME);
                        // setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // startActivity(setIntent);
                    }
                }
            }).show(getFragmentManager(), null);
        } else {
            super.onBackPressed();
        }
    }

    public void onResume() {
        super.onResume();
        resetWidgetStatusOnResume();
        themeColorChanged(SettingManager.getInstance(this).getThemeColor());
    }

    private void finishTest() {
        if (DEBUG)
            Log.d(TAG, "finishTest, child: " + mDisplayChild);
        if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
            mNo.setVisibility(View.GONE);
            mYes.setVisibility(View.GONE);
            mNext.setVisibility(View.GONE);
            mComplete.setVisibility(View.VISIBLE);
            mQuestion.setVisibility(View.GONE);
            mAnswer.setVisibility(View.GONE);
            mProgressHint.setVisibility(View.GONE);
        } else {
            mPlan.mCurrentProgress++;
            mDatabaseHelper.updatePlan(mPlan);
            if (mQuestionList.isEmpty()) {
                mDatabaseHelper.resetSimpleTestStatus(mPlan.mPlanType);
                mDatabaseHelper.resetCompositeTestStatus(mPlan.mPlanType);
            }
            mComplete.setVisibility(View.VISIBLE);
            mComplete.setText(R.string.test_activity_finish_real_test);
            mOption0.setVisibility(View.GONE);
            mOption1.setVisibility(View.GONE);
            mOption2.setVisibility(View.GONE);
            mOption3.setVisibility(View.GONE);
            mQuestionReal.setVisibility(View.GONE);
            mProgressHint.setVisibility(View.GONE);
            // TODO show complete dialog
        }
    }

    private void init() {
        mSwitcher = (ViewSwitcher)findViewById(R.id.test_switcher);
        mQuestionList = new ArrayList<LawAttrs>();
        mMainBackground = (RelativeLayout)findViewById(R.id.main_test_bg);
        mTitle = (TextView)findViewById(R.id.law_name);
        mProgressHint = (TextView)findViewById(R.id.progress_hint);
        mYes = (Button)findViewById(R.id.yes);
        mYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // only active when simple test
                if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
                    LawAttrs law = mQuestionList.remove(mCurrentIndex);
                    law.mHasAnsweredSimple = LawAttrs.HAS_ANSWERED;
                    mDatabaseHelper.updateSimpleTestStatus(law, mPlan.mPlanType);
                    if (mQuestionList.isEmpty()) {
                        finishTest();
                    } else {
                        generateQuestion();
                    }
                }
            }
        });
        mNo = (Button)findViewById(R.id.no);
        mNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // only active when simple test
                if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
                    LawAttrs law = mQuestionList.get(mCurrentIndex);
                    ++law.mWrongTime;
                    mDatabaseHelper.updateSimpleTestStatus(law, mPlan.mPlanType);
                    mAnswer.setVisibility(View.VISIBLE);
                    mNo.setVisibility(View.GONE);
                    mYes.setVisibility(View.GONE);
                    mNext.setVisibility(View.VISIBLE);
                }
            }
        });
        mNext = (Button)findViewById(R.id.next);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
                    generateQuestion();
                    mNext.setVisibility(View.GONE);
                    mNo.setVisibility(View.VISIBLE);
                    mYes.setVisibility(View.VISIBLE);
                } else {
                    generateQuestion();
                }
            }
        });
        mComplete = (Button)findViewById(R.id.complete);
        mComplete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
                    Intent start = new Intent(getApplicationContext(), TestActivity.class);
                    start.putExtra(INTENT_PLAN_TYPE, mPlan.mPlanType);
                    start.putExtra(INTENT_DISPLAY_CHILD, DISPLAY_CHILD_REAL_TEST);
                    start.putExtra(INTENT_FULL_TEST, mFullTest);
                    start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(start);
                } else {
                    finish();
                }
            }
        });
    }

    private final OnClickListener mOptionClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            boolean correct = false;
            int vId = v.getId();
            switch (vId) {
                case R.id.option0:
                    if (mAnswerOption == 0) {
                        correct = true;
                    }
                    break;
                case R.id.option1:
                    if (mAnswerOption == 1) {
                        correct = true;
                    }
                    break;
                case R.id.option2:
                    if (mAnswerOption == 2) {
                        correct = true;
                    }
                    break;
                case R.id.option3:
                    if (mAnswerOption == 3) {
                        correct = true;
                    }
                    break;
            }
            if (correct == false) {
                switch (mAnswerOption) {
                    case 0:
                        mOption0.setTextColor(0xff228B22);
                        ((Button)v).setTextColor(Color.RED);
                        break;
                    case 1:
                        mOption1.setTextColor(0xff228B22);
                        ((Button)v).setTextColor(Color.RED);
                        break;
                    case 2:
                        mOption2.setTextColor(0xff228B22);
                        ((Button)v).setTextColor(Color.RED);
                        break;
                    case 3:
                        mOption3.setTextColor(0xff228B22);
                        ((Button)v).setTextColor(Color.RED);
                        break;
                }
                mNext.setVisibility(View.VISIBLE);
                mOption0.setOnClickListener(null);
                mOption1.setOnClickListener(null);
                mOption2.setOnClickListener(null);
                mOption3.setOnClickListener(null);
                LawAttrs law = mQuestionList.get(mCurrentIndex);
                ++law.mWrongTime;
                mDatabaseHelper.updateCompositeTestFragmentStatus(law, mPlan.mPlanType);
            } else {
                LawAttrs law = mQuestionList.remove(mCurrentIndex);
                law.mHasAnsweredComposite = LawAttrs.HAS_ANSWERED;
                mDatabaseHelper.updateCompositeTestFragmentStatus(law, mPlan.mPlanType);
                if (mQuestionList.isEmpty()) {
                    finishTest();
                } else {
                    generateQuestion();
                }
            }
        }
    };

    private void initRealTestView() {
        mRealQuestionScrollView = (ScrollView)findViewById(R.id.question_scroller_real);
        mRealOptionScrollView = (ScrollView)findViewById(R.id.option_scroller_real);
        mQuestionReal = (TextView)findViewById(R.id.question_real);
        mOption0 = (Button)findViewById(R.id.option0);
        mOption1 = (Button)findViewById(R.id.option1);
        mOption2 = (Button)findViewById(R.id.option2);
        mOption3 = (Button)findViewById(R.id.option3);
        mOption0.setOnClickListener(mOptionClickListener);
        mOption1.setOnClickListener(mOptionClickListener);
        mOption2.setOnClickListener(mOptionClickListener);
        mOption3.setOnClickListener(mOptionClickListener);
    }

    private void initSimpleTestView() {
        mSimpleScrollView = (ScrollView)findViewById(R.id.question_scroller);
        mQuestion = (TextView)findViewById(R.id.question);
        mAnswer = (TextView)findViewById(R.id.answer);
    }

    private void generateQuestion() {
        if (mQuestionList.isEmpty()) {
            finishTest();
            return;
        } else {
            mProgressHint.setText(getBaseContext().getString(R.string.rest_items)
                    + mQuestionList.size());
        }
        int type = (int)((Math.random() * 100) % 2);
        mCurrentIndex = (int)((Math.random() * 10000) % mQuestionList.size());
        String question = "";
        LawAttrs law = mQuestionList.get(mCurrentIndex);
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
        if (mDisplayChild == DISPLAY_CHILD_SIMPLE_TEST) {
            String answer = "";
            switch (type) {
                case QUESTION_TYPE_LINE:
                    question = law.mLine;
                    answer = mQuestionList.get(mCurrentIndex).mContent;
                    mQuestion.setGravity(Gravity.CENTER);
                    mAnswer.setGravity(Gravity.LEFT);
                    break;
                case QUESTION_TYPE_CONTENT:
                    answer = law.mLine;
                    question = mQuestionList.get(mCurrentIndex).mContent;
                    mQuestion.setGravity(Gravity.LEFT);
                    mAnswer.setGravity(Gravity.CENTER);
                    break;
            }
            mQuestion.setText(question);
            mAnswer.setText(answer);
            mAnswer.setVisibility(View.GONE);
            mSimpleScrollView.scrollTo(0, 0);
        } else {
            String answer = "";
            resetRealTestWidgets();
            mAnswerOption = (int)((Math.random() * 1000) % 4);
            ArrayList<Integer> restOption = getConfusedOptionIndex();
            String confusedOption0 = null, confusedOption1 = null, confusedOption2 = null;
            switch (type) {
                case QUESTION_TYPE_LINE:
                    question = law.mLine;
                    answer = mQuestionList.get(mCurrentIndex).mContent;
                    confusedOption0 = mLaws.get(restOption.get(0)).mContent;
                    confusedOption1 = mLaws.get(restOption.get(1)).mContent;
                    confusedOption2 = mLaws.get(restOption.get(2)).mContent;
                    mQuestionReal.setGravity(Gravity.CENTER);
                    mOption0.setGravity(Gravity.LEFT);
                    mOption1.setGravity(Gravity.LEFT);
                    mOption2.setGravity(Gravity.LEFT);
                    mOption3.setGravity(Gravity.LEFT);
                    break;
                case QUESTION_TYPE_CONTENT:
                    answer = law.mLine;
                    confusedOption0 = mLaws.get(restOption.get(0)).mLine;
                    confusedOption1 = mLaws.get(restOption.get(1)).mLine;
                    confusedOption2 = mLaws.get(restOption.get(2)).mLine;
                    question = mQuestionList.get(mCurrentIndex).mContent;
                    mQuestionReal.setGravity(Gravity.LEFT);
                    mOption0.setGravity(Gravity.CENTER);
                    mOption1.setGravity(Gravity.CENTER);
                    mOption2.setGravity(Gravity.CENTER);
                    mOption3.setGravity(Gravity.CENTER);
                    break;
            }
            mQuestionReal.setText(question);
            switch (mAnswerOption) {
                case 0:
                    mOption0.setText(answer);
                    if (DEBUG) {
                        mOption0.setTextColor(Color.MAGENTA);
                    }
                    mOption1.setText(confusedOption0);
                    mOption2.setText(confusedOption1);
                    mOption3.setText(confusedOption2);
                    break;
                case 1:
                    mOption0.setText(confusedOption0);
                    if (DEBUG) {
                        mOption1.setTextColor(Color.MAGENTA);
                    }
                    mOption1.setText(answer);
                    mOption2.setText(confusedOption1);
                    mOption3.setText(confusedOption2);
                    break;
                case 2:
                    mOption0.setText(confusedOption0);
                    mOption1.setText(confusedOption1);
                    if (DEBUG) {
                        mOption2.setTextColor(Color.MAGENTA);
                    }
                    mOption2.setText(answer);
                    mOption3.setText(confusedOption2);
                    break;
                case 3:
                    mOption0.setText(confusedOption0);
                    mOption1.setText(confusedOption1);
                    mOption2.setText(confusedOption2);
                    if (DEBUG) {
                        mOption3.setTextColor(Color.MAGENTA);
                    }
                    mOption3.setText(answer);
                    break;
            }
        }
    }

    private void resetRealTestWidgets() {
        mOption0.setTextColor(Color.BLACK);
        mOption1.setTextColor(Color.BLACK);
        mOption2.setTextColor(Color.BLACK);
        mOption3.setTextColor(Color.BLACK);
        mNext.setVisibility(View.GONE);
        mRealQuestionScrollView.scrollTo(0, 0);
        mRealOptionScrollView.scrollTo(0, 0);
        mOption0.setOnClickListener(mOptionClickListener);
        mOption1.setOnClickListener(mOptionClickListener);
        mOption2.setOnClickListener(mOptionClickListener);
        mOption3.setOnClickListener(mOptionClickListener);
    }

    public ArrayList<Integer> getConfusedOptionIndex() {
        ArrayList<Integer> rtn = new ArrayList<Integer>();
        while (rtn.size() < 3) {
            int index = (int)(Math.random() * 1000) % mLaws.size();
            if (rtn.contains(index) == false) {
                if (mLaws.get(index).mContent.equals(mQuestionList.get(mCurrentIndex).mContent) == false
                        || mLaws.get(index).mLine.equals(mQuestionList.get(mCurrentIndex).mLine) == false)
                    rtn.add(index);
            }
        }
        return rtn;
    }

    @Override
    public void themeColorChanged(int newTheme) {
        switch (newTheme) {
            case SettingManager.VALUE_THEME_BLUE:
                mYes.setBackgroundResource(R.drawable.blue_btn_bg);
                mNo.setBackgroundResource(R.drawable.blue_btn_bg);
                mNext.setBackgroundResource(R.drawable.blue_btn_bg);
                mComplete.setBackgroundResource(R.drawable.blue_btn_bg);
                mMainBackground.setBackgroundResource(R.drawable.blue_main_paper_bg);
                break;
            case SettingManager.VALUE_THEME_GRAY:
                mYes.setBackgroundResource(R.drawable.gray_btn_bg);
                mNo.setBackgroundResource(R.drawable.gray_btn_bg);
                mNext.setBackgroundResource(R.drawable.gray_btn_bg);
                mComplete.setBackgroundResource(R.drawable.gray_btn_bg);
                mMainBackground.setBackgroundResource(R.drawable.gray_main_paper_bg);
                break;
            case SettingManager.VALUE_THEME_GREEN:
                mYes.setBackgroundResource(R.drawable.green_btn_bg);
                mNo.setBackgroundResource(R.drawable.green_btn_bg);
                mNext.setBackgroundResource(R.drawable.green_btn_bg);
                mComplete.setBackgroundResource(R.drawable.green_btn_bg);
                mMainBackground.setBackgroundResource(R.drawable.green_main_paper_bg);
                break;
        }
    }

    public static final int getUpperBound(final int totalSize, final int totalProgress,
            final int currentProgress) {
        int unit = totalSize / totalProgress;
        int restDay = totalSize % totalProgress;
        int rest = 0;
        if (restDay > currentProgress) {
            ++rest;
        }
        if (currentProgress == 0) {
            return unit * 2 + rest;
        } else if (currentProgress + 1 == totalProgress) {
            return totalSize;
        } else {
            return unit + rest + getUpperBound(totalSize, totalProgress, currentProgress - 1);
        }
    }

    public static final int[] getTestBound(final int totalSize, final int totalProgress,
            final int currentProgress) {
        if (totalProgress == 0) {
            return new int[] {
                    0, 0
            };
        }
        int unit = totalSize / totalProgress;
        int restDay = totalSize % totalProgress;
        int upperBound = getUpperBound(totalSize, totalProgress, currentProgress);
        int lowerBound = upperBound - unit;
        if (currentProgress == 0) {
            lowerBound = 0;
        } else if (restDay > currentProgress) {
            --lowerBound;
        }
        int rtn[] = new int[] {
                upperBound, lowerBound
        };
        return rtn;
    }
}
