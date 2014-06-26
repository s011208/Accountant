
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;
import java.util.Collections;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.activities.BaseTestActivity;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.activities.SimpleTestActivity;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
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

    private NumberPicker mEstimateDays;

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
                        initLawOptionSpinner();
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
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
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
        mContentView.setInAnimation(mContext, R.anim.alpha_scale_switch_in);
        mContentView.setOutAnimation(mContext, R.anim.alpha_scale_switch_out);
        // manage
        initManage();
        // edit
        initEdit();
        initLawOptionSpinner();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
                        mContext, android.R.style.Theme_Holo_Light_Dialog));
                builder.setMessage(R.string.dialog_confirm_to_delete_all_msg);
                builder.setTitle(R.string.dialog_confirm_to_delete_all_title);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseHelper.clearAllPlans();
                        initLawOptionSpinner();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        mPlanList = (ListView)mContentView.findViewById(R.id.plan_listview);
        mPlanListAdapter = new PlanListAdapter();
        mPlanList.setAdapter(mPlanListAdapter);
        mPlanList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlanAttrs plan = mPlanListAdapter.getItem(position);
                Intent start = new Intent(mContext, SimpleTestActivity.class);
                start.putExtra(BaseTestActivity.INTENT_PLAN_TYPE, plan.mPlanType);
                mContext.startActivity(start);
            }
        });
        mPlanList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                    long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
                        mContext, android.R.style.Theme_Holo_Light_Dialog));
                builder.setMessage(R.string.dialog_confirm_to_delete_msg);
                builder.setTitle(R.string.dialog_confirm_to_delete_title);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseHelper.deletePlan(mPlanListAdapter.getItem(position).mPlanType);
                        initLawOptionSpinner();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            }
        });
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
            return mData.size();
        }

        @Override
        public PlanAttrs getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater
                        .inflate(R.layout.create_plan_fragment_manage_list_row, null);
                holder = new ViewHolder();
                holder.mType = (TextView)convertView.findViewById(R.id.type);
                holder.mProgressByDay = (TextView)convertView.findViewById(R.id.progress_by_day);
                holder.mProgressByLine = (TextView)convertView.findViewById(R.id.progress_by_line);
                holder.mOrder = (TextView)convertView.findViewById(R.id.order);
                holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            PlanAttrs plan = getItem(position);
            holder.mType.setText(GovLawParser.getTypeTextResource(plan.mPlanType));
            holder.mOrder
                    .setText(plan.mReadingOrder == READING_ORDER_CHAPTER ? R.string.reading_order_chapter
                            : R.string.reading_order_random);
            holder.mProgressByDay.setText(plan.mCurrentProgress + " / " + plan.mTotalProgress);
            int totalLines = mDatabaseHelper.queryTypeCount(plan.mPlanType);
            int currentLines = (int)Math.ceil(totalLines / plan.mTotalProgress)
                    * plan.mCurrentProgress;
            holder.mProgressBar.setMax(plan.mTotalProgress);
            holder.mProgressBar.setProgress(plan.mCurrentProgress);
            holder.mProgressByLine.setText(currentLines + " / " + totalLines);
            return convertView;
        }

        class ViewHolder {
            TextView mType;

            TextView mOrder;

            TextView mProgressByDay;

            TextView mProgressByLine;

            ProgressBar mProgressBar;
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
                        .getSelectedItemPosition(), mEstimateDays.getValue(), 0, 0));
                initLawOptionSpinner();
                setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
            }
        });
        mEstimateDays = (NumberPicker)mContentView.findViewById(R.id.estimate_days);
        mEstimateDays.setMinValue(3);
        mEstimateDays.setMaxValue(20);
        mEstimateDays.setValue(7);// set 7 as default
        mLawOptions = (Spinner)mContentView.findViewById(R.id.law_options);
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
