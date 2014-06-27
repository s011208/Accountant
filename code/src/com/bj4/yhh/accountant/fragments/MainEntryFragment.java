
package com.bj4.yhh.accountant.fragments;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.service.ParseService;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainEntryFragment extends Fragment {
    private Context mContext;

    private RelativeLayout mContentView;

    private Button mPlan, mTest, mOverview, mCheckUpdate;

    private MainActivity mMainActivity;

    public MainEntryFragment() {
    }

    public MainEntryFragment(MainActivity activity) {
        mContext = activity;
        mMainActivity = activity;
        init();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
        }
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (RelativeLayout)inflater.inflate(R.layout.main_entry_fragment, null);
        mPlan = (Button)mContentView.findViewById(R.id.plan);
        mTest = (Button)mContentView.findViewById(R.id.test);
        mOverview = (Button)mContentView.findViewById(R.id.overview);
        mCheckUpdate = (Button)mContentView.findViewById(R.id.check_update);
        mPlan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMainActivity.switchFragment(MainActivity.CREATE_PLAN_FRAGMENT);
            }
        });
        mTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMainActivity.switchFragment(MainActivity.TEST_FRAGMENT);
            }
        });
        mOverview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMainActivity.switchFragment(MainActivity.OVER_VIEW_FRAGMENT);
            }
        });
        mCheckUpdate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ParseService.class);
                intent.putExtra(ParseService.UPDATE_ALL, true);
                mContext.startService(intent);
                AccountantApplication.sCheckForUpdate = false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }
}
