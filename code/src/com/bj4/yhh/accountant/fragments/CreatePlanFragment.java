
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

public class CreatePlanFragment extends Fragment {
    public static final int CREATE_PLAN_MANAGE_PAGE = 0;

    public static final int CREATE_PLAIN_EDIT = 1;

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

    public CreatePlanFragment() {
    }

    public CreatePlanFragment(Context context) {
        mContext = context;
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (ViewSwitcher)inflater.inflate(R.layout.create_plan_fragment, null);
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
                // TODO Auto-generated method stub
                setDisplayedChild(CREATE_PLAIN_EDIT);
            }
        });
        mDeletePlan = (Button)mContentView.findViewById(R.id.delete_plan);
        mPlanList = (ListView)mContentView.findViewById(R.id.plan_listview);
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
                setDisplayedChild(CREATE_PLAN_MANAGE_PAGE);
            }
        });
        mEstimateDays = (EditText)mContentView.findViewById(R.id.estimate_days);
        mEstimateDays.setText("7");// set 7 as default
        mLawOptions = (Spinner)mContentView.findViewById(R.id.law_options);
        ArrayList<String> lawOption = new ArrayList<String>();
        for (int type : mDatabaseHelper.getAllLawTypes()) {
            lawOption.add(GovLawParser.getTypeText(mContext, type));
        }
        ArrayAdapter<String> lawOptionAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, lawOption);
        mLawOptions.setAdapter(lawOptionAdapter);
        mReadingOrder = (Spinner)mContentView.findViewById(R.id.reading_order);
        ArrayAdapter<String> readingOrderAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, mContext.getResources()
                        .getStringArray(R.array.reading_order));
        mReadingOrder.setAdapter(readingOrderAdapter);
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
}
