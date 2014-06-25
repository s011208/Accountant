
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.MainActivity;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class CreatePlanFragment extends Fragment implements DatabaseHelper.RefreshPlanCallback {
    public static final int CREATE_PLAN_MANAGE_PAGE = 0;

    public static final int CREATE_PLAIN_EDIT = 1;

    public static final int READING_ORDER_CHAPTER = 0;

    public static final int READING_ORDER_RANDOM = 1;

    private int mCurrentDisplayChild = CREATE_PLAN_MANAGE_PAGE;

    private Context mContext;

    private ViewSwitcher mContentView;

    // manage plan
    private Button mCreatePlan, mDeletePlan;

    private ListView mPlanList;

    // edit plan
    private Button mCancel, mOk;

    private Spinner mLawOptions, mReadingOrder;

    private EditText mEstimateDays;

    private DatabaseHelper mDatabaseHelper;

    private MainActivity mMainActivity;

    private PlanListAdapter mPlanListAdapter;

    private LayoutInflater mInflater;

    private DatabaseHelper.RefreshLawCallback mRefreshLawCallback = new DatabaseHelper.RefreshLawCallback() {

        @Override
        public void notifyDataChanged() {
            if (mMainActivity != null) {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initEdit();
                    }
                });
            }
        }
    };

    public CreatePlanFragment() {
    }

    public CreatePlanFragment(MainActivity activity) {
        mContext = activity;
        mMainActivity = activity;
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        init();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper.addCallback(this);
        mDatabaseHelper.addCallback(mRefreshLawCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.removeCallback(this);
        mDatabaseHelper.removeCallback(mRefreshLawCallback);
    }

    private void init() {
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (ViewSwitcher)mInflater.inflate(R.layout.create_plan_fragment, null);
        // manage
        initManage();
        // edit
        initEdit();
        setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
    }

    private void initManage() {
        mCreatePlan = (Button)mContentView.findViewById(R.id.create_plan);
        mCreatePlan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDatabaseHelper.getAllLawTypes().isEmpty()) {
                    Toast.makeText(mContext, "download some laws in advance", Toast.LENGTH_LONG)
                            .show();
                } else if (mLawOptions != null && mLawOptions.getAdapter().getCount() > 0) {
                    setDisplayedChild(CREATE_PLAIN_EDIT);
                } else {
                    Toast.makeText(mContext, "remove some plans in advance", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        mDeletePlan = (Button)mContentView.findViewById(R.id.delete_plan);
        mDeletePlan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDatabaseHelper.clearAllPlans();
                initLawOptionSpinner();
            }
        });
        mPlanList = (ListView)mContentView.findViewById(R.id.plan_listview);
        mPlanListAdapter = new PlanListAdapter();
        mPlanList.setAdapter(mPlanListAdapter);
    }

    class PlanListAdapter extends BaseAdapter {
        private ArrayList<PlanAttrs> mData;

        public PlanListAdapter() {
            init();
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        private void init() {
            mData = mDatabaseHelper.getAllPlans();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public PlanAttrs getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater
                        .inflate(R.layout.create_plan_fragment_manage_list_row, null);
                holder = new ViewHolder();
                holder.mType = (TextView)convertView.findViewById(R.id.type);
                holder.mProgress = (TextView)convertView.findViewById(R.id.progress);
                holder.mOrder = (TextView)convertView.findViewById(R.id.order);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            PlanAttrs plan = getItem(position);
            holder.mType.setText(GovLawParser.getTypeTextResource(plan.mPlanType));
            holder.mOrder
                    .setText(plan.mReadingOrder == READING_ORDER_CHAPTER ? R.string.reading_order_chapter
                            : R.string.reading_order_random);
            holder.mProgress.setText(plan.mCurrentProgress + " / " + plan.mTotalProgress);
            return convertView;
        }

        class ViewHolder {
            TextView mType;

            TextView mOrder;

            TextView mProgress;
        }
    }

    private int getPlanType(String txt) {
        return GovLawParser.getTextType(mContext, txt);
    }

    private void initEdit() {
        mCancel = (Button)mContentView.findViewById(R.id.cancel);
        mCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
            }
        });
        mOk = (Button)mContentView.findViewById(R.id.ok);
        mOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int planType = getPlanType(mLawOptions.getSelectedItem().toString());
                mDatabaseHelper.addNewPlan(new PlanAttrs(planType, mReadingOrder
                        .getSelectedItemPosition(), Integer.valueOf(mEstimateDays.getText()
                        .toString()), 0, 0));
                initLawOptionSpinner();
                setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
            }
        });
        mEstimateDays = (EditText)mContentView.findViewById(R.id.estimate_days);
        mEstimateDays.setText("7");// set 7 as default
        mLawOptions = (Spinner)mContentView.findViewById(R.id.law_options);
        initLawOptionSpinner();
        mReadingOrder = (Spinner)mContentView.findViewById(R.id.reading_order);
        ArrayAdapter<String> readingOrderAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, mContext.getResources()
                        .getStringArray(R.array.reading_order));
        mReadingOrder.setAdapter(readingOrderAdapter);
    }

    private void initLawOptionSpinner() {
        ArrayList<String> lawOption = new ArrayList<String>();
        ArrayList<Integer> availableLawTypes = mDatabaseHelper.getAvailableLawTypes();
        Collections.sort(availableLawTypes);
        for (int type : availableLawTypes) {
            lawOption.add(GovLawParser.getTypeText(mContext, type));
        }
        ArrayAdapter<String> lawOptionAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, lawOption);
        mLawOptions.setAdapter(lawOptionAdapter);
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
    public void notifyDataChanged() {
        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mPlanListAdapter.notifyDataSetChanged();
            }
        });
    }
}
