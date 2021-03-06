
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.activities.TestActivity;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.dialogs.EnlargeOverViewContentDialog;
import com.bj4.yhh.accountant.parser.GovLawParser;
import com.bj4.yhh.accountant.utilities.GA;
import com.bj4.yhh.accountant.utilities.MagicFuzzy;
import com.bj4.yhh.accountant.utilities.ToastHelper;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class TestFragment extends BaseFragment implements DatabaseHelper.RefreshLawCallback {
    public static final int TEST_FRAGMENT_TEST_TYPE = 0;

    public static final int TEST_FRAGMENT_LAW_LIST = 1;

    private int mCurrentDisplayChild = TEST_FRAGMENT_TEST_TYPE;

    private Context mContext;

    private ViewSwitcher mContentView;

    private MainActivity mMainActivity;

    private ListView mLawList;

    private LawListAdapter mLawListAdapter;

    private DatabaseHelper mDatabaseHelper;

    private LayoutInflater mInflater;

    private Button mTypeReview, mTypeByLaw, mKeepPreviousTest, mTypeByLawRandom;

    public static final int TEST_TYPE_REVIEW = 0;

    public static final int TEST_TYPE_BY_LAW = 1;

    public static final int TEST_TYPE_BY_LAW_RANDOM = 2;

    private static int sTestType = TEST_TYPE_BY_LAW;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
            mMainActivity = (MainActivity)getActivity();
            mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
            init();
        }
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        mDatabaseHelper.addCallback(this);
        themeColorChanged(SettingManager.getInstance(mContext).getThemeColor());
    }

    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.removeCallback(this);
    }

    private void init() {
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (ViewSwitcher)mInflater.inflate(R.layout.test_fragment, null);
        mContentView.setInAnimation(mContext, R.anim.alpha_scale_switch_in);
        mContentView.setOutAnimation(mContext, R.anim.alpha_scale_switch_out);
        // test type
        mTypeByLawRandom = (Button)mContentView.findViewById(R.id.test_type_by_law_random);
        mTypeReview = (Button)mContentView.findViewById(R.id.test_type_review);
        mTypeByLaw = (Button)mContentView.findViewById(R.id.test_type_by_law);
        mTypeReview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_TEST_FRAGMENT,
                        GA.ACTIONS.ACTIONS_TEST_REVIEW_PLAN, null, null);
                int previousType = sTestType;
                sTestType = TEST_TYPE_REVIEW;
                mLawListAdapter.notifyDataSetChanged();
                if (mLawListAdapter.getCount() == 0) {
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_CREATE_PLANS_IN_ADVANCE)
                            .show();
                    sTestType = previousType;
                } else {
                    setDisplayedChild(TEST_FRAGMENT_LAW_LIST);
                }
            }
        });
        mTypeByLaw.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_TEST_FRAGMENT,
                        GA.ACTIONS.ACTIONS_TEST_REVIEW_OVERALL, null, null);
                int previousType = sTestType;
                sTestType = TEST_TYPE_BY_LAW;
                mLawListAdapter.notifyDataSetChanged();
                if (mLawListAdapter.getCount() == 0) {
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_DOWNLOAD_LAWS_INADVANCE)
                            .show();
                    sTestType = previousType;
                } else {
                    setDisplayedChild(TEST_FRAGMENT_LAW_LIST);
                }
            }
        });
        mTypeByLawRandom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_TEST_FRAGMENT,
                        GA.ACTIONS.ACTIONS_TEST_REVIEW_RANDOM, null, null);
                int previousType = sTestType;
                sTestType = TEST_TYPE_BY_LAW_RANDOM;
                mLawListAdapter.notifyDataSetChanged();
                if (mLawListAdapter.getCount() == 0) {
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_DOWNLOAD_LAWS_INADVANCE)
                            .show();
                    sTestType = previousType;
                } else {
                    setDisplayedChild(TEST_FRAGMENT_LAW_LIST);
                }
            }
        });
        mKeepPreviousTest = (Button)mContentView.findViewById(R.id.keep_previous_test);
        mKeepPreviousTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean available = mDatabaseHelper.hasPreviousDataInTestFragment();
                if (available) {
                    startTestActivity(mDatabaseHelper.getTestFragmentDataType(), false);
                } else {
                    ToastHelper.makeToast(mContext,
                            ToastHelper.TOAST_TYPE_NONE_PREVIOUS_TEST_FRAGMENT_DATA).show();
                }
            }
        });
        // law list
        mLawList = (ListView)mContentView.findViewById(R.id.overview_law_list);
        initLawList();
    }

    private void initLawList() {
        if (mLawList != null) {
            mLawListAdapter = new LawListAdapter();
            mLawList.setAdapter(mLawListAdapter);
            mLawList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startTestActivity(mLawListAdapter.getItem(position));
                }
            });
        }
    }

    private void startTestActivity(int planType) {
        startTestActivity(planType, true);
    }

    private void startTestActivity(int planType, boolean clearPrevious) {
        if (clearPrevious) {
            mDatabaseHelper.clearTestFragmentData();
            mDatabaseHelper.setTestFragmentData(planType, sTestType);
        }
        Intent start = new Intent(mContext, TestActivity.class);
        start.putExtra(TestActivity.INTENT_PLAN_TYPE, planType);
        start.putExtra(TestActivity.INTENT_FROM_TEST_FRAGMENT, true);
        start.putExtra(TestActivity.INTENT_DISPLAY_CHILD, TestActivity.DISPLAY_CHILD_REAL_TEST);
        mContext.startActivity(start);
    }

    private class LawListAdapter extends BaseAdapter {
        private ArrayList<Integer> mTypeData;

        public LawListAdapter() {
            init();
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        private void init() {
            if (sTestType == TEST_TYPE_BY_LAW || sTestType == TEST_TYPE_BY_LAW_RANDOM) {
                mTypeData = mDatabaseHelper.getAllLawTypes();
            } else if (sTestType == TEST_TYPE_REVIEW) {
                ArrayList<PlanAttrs> data = mDatabaseHelper.getAllPlans();
                mTypeData = new ArrayList<Integer>();
                for (PlanAttrs attr : data) {
                    mTypeData.add(attr.mPlanType);
                }
            }
        }

        @Override
        public int getCount() {
            return mTypeData.size();
        }

        @Override
        public Integer getItem(int position) {
            return mTypeData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.over_view_fragment_law_list_row, null);
                holder = new ViewHolder();
                holder.mLawText = (TextView)convertView
                        .findViewById(R.id.overview_law_list_type_txt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            int type = getItem(position);
            holder.mLawText.setText(GovLawParser.getTypeTextResource(type));
            return convertView;
        }

        private class ViewHolder {
            TextView mLawText;
        }
    }

    public void setDisplayedChild(int child) {
        if (mContentView != null) {
            mCurrentDisplayChild = child;
            mContentView.setDisplayedChild(child);
        }
    }

    public int getDisplayedChild() {
        return mCurrentDisplayChild;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                ((ViewGroup)parent).removeView(mContentView);
            }
        }
    }

    @Override
    public void notifyDataChanged() {
        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mLawListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void themeColorChanged(int newColor) {
        switch (newColor) {
            case SettingManager.VALUE_THEME_BLUE:
                mTypeReview.setBackgroundResource(R.drawable.blue_btn_bg);
                mTypeByLaw.setBackgroundResource(R.drawable.blue_btn_bg);
                mKeepPreviousTest.setBackgroundResource(R.drawable.blue_btn_bg);
                mTypeByLawRandom.setBackgroundResource(R.drawable.blue_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GRAY:
                mTypeReview.setBackgroundResource(R.drawable.gray_btn_bg);
                mTypeByLaw.setBackgroundResource(R.drawable.gray_btn_bg);
                mKeepPreviousTest.setBackgroundResource(R.drawable.gray_btn_bg);
                mTypeByLawRandom.setBackgroundResource(R.drawable.gray_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GREEN:
                mTypeReview.setBackgroundResource(R.drawable.green_btn_bg);
                mTypeByLaw.setBackgroundResource(R.drawable.green_btn_bg);
                mKeepPreviousTest.setBackgroundResource(R.drawable.green_btn_bg);
                mTypeByLawRandom.setBackgroundResource(R.drawable.green_btn_bg);
                break;
        }
    }
}
