
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;
import java.util.Collections;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.PlanAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.activities.TestActivity;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.activities.TestActivity;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;
import com.bj4.yhh.accountant.utilities.ToastHelper;

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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class CreatePlanFragment extends BaseFragment implements DatabaseHelper.RefreshPlanCallback {
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

    private TextView mTotalLawCount;

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
        mDatabaseHelper.addCallback(mRefreshLawCallback);
        themeColorChanged(SettingManager.getInstance(mContext).getThemeColor());
    }

    public void onResume() {
        super.onResume();
        initLawOptionSpinner();
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
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_DOWNLOAD_LAWS_INADVANCE)
                            .show();
                } else if (mLawOptions != null && mLawOptions.getAdapter().getCount() > 0) {
                    setDisplayedChild(CREATE_PLAIN_EDIT);
                } else {
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_REMOVE_PLANS_INADVANCE)
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
                if (plan.mCurrentProgress >= plan.mTotalProgress) {
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_DONE_TYPE_PLAN).show();
                } else {
                    Intent start = new Intent(mContext, TestActivity.class);
                    if (plan.mCurrentProgress + 1 == plan.mTotalProgress) {
                        start.putExtra(TestActivity.INTENT_FULL_TEST, true);
                    }
                    start.putExtra(TestActivity.INTENT_PLAN_TYPE, plan.mPlanType);
                    mContext.startActivity(start);
                }
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
            int totalLines = mDatabaseHelper.getPlanTypeCount(plan.mPlanType);
            int currentLines = getUpperBound(totalLines, plan.mTotalProgress,
                    plan.mCurrentProgress - 1);
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

    public static final int getUpperBound(final int totalSize, final int totalProgress,
            final int currentProgress) {
        if (totalProgress == 0) {
            return 0;
        }
        int unit = totalSize / totalProgress;
        int restDay = totalSize % totalProgress;
        int rest = 0;
        if (restDay > currentProgress) {
            ++rest;
        }
        if (currentProgress == 0) {
            return unit * 2 + rest;
        } else if (currentProgress + 1 >= totalProgress) {
            return totalSize;
        } else if (currentProgress == -1) {
            return 0;
        } else {
            return unit + rest + getUpperBound(totalSize, totalProgress, currentProgress - 1);
        }
    }

    private int getPlanType(String txt) {
        return GovLawParser.getTextType(mContext, txt);
    }

    private void initEdit() {
        mTotalLawCount = (TextView)mContentView.findViewById(R.id.law_count);
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
                int totalDay = 3;
                try {
                    totalDay = Integer.valueOf(mEstimateDays.getText().toString());
                    if (totalDay < 3) {
                        ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_WRONG_ESTIMATE_DAY)
                                .show();
                        mEstimateDays.setText("3");
                        return;
                    }
                } catch (Exception e) {
                    // using default
                    totalDay = 3;
                    ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_WRONG_ESTIMATE_DAY)
                            .show();
                    mEstimateDays.setText("3");
                    return;
                }
                int planType = getPlanType(mLawOptions.getSelectedItem().toString());
                mDatabaseHelper.addNewPlan(new PlanAttrs(planType, mReadingOrder
                        .getSelectedItemPosition(), totalDay, 0, 0));
                initLawOptionSpinner();
                setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
            }
        });
        mEstimateDays = (EditText)mContentView.findViewById(R.id.estimate_days);
        mEstimateDays.setText("7");
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
        if (mLawOptions != null) {
            mLawOptions.setAdapter(lawOptionAdapter);
            mLawOptions.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int planType = getPlanType(mLawOptions.getSelectedItem().toString());
                    mTotalLawCount.setText("" + mDatabaseHelper.getPlanTypeCount(planType));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                    mTotalLawCount.setText("");
                }
            });
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
    public void notifyDataChanged() {
        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mPlanListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void themeColorChanged(int newColor) {
        switch (newColor) {
            case SettingManager.VALUE_THEME_BLUE:
                mCreatePlan.setBackgroundResource(R.drawable.blue_btn_bg);
                mDeletePlan.setBackgroundResource(R.drawable.blue_btn_bg);
                mCancel.setBackgroundResource(R.drawable.blue_btn_bg);
                mOk.setBackgroundResource(R.drawable.blue_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GRAY:
                mCreatePlan.setBackgroundResource(R.drawable.gray_btn_bg);
                mDeletePlan.setBackgroundResource(R.drawable.gray_btn_bg);
                mCancel.setBackgroundResource(R.drawable.gray_btn_bg);
                mOk.setBackgroundResource(R.drawable.gray_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GREEN:
                mCreatePlan.setBackgroundResource(R.drawable.green_btn_bg);
                mDeletePlan.setBackgroundResource(R.drawable.green_btn_bg);
                mCancel.setBackgroundResource(R.drawable.green_btn_bg);
                mOk.setBackgroundResource(R.drawable.green_btn_bg);
                break;
        }
    }
}
