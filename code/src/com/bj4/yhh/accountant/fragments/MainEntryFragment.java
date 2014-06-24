
package com.bj4.yhh.accountant.fragments;

import com.bj4.yhh.accountant.MainActivity;
import com.bj4.yhh.accountant.R;

import android.app.Fragment;
import android.content.Context;
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

    private Button mPlan, mTest, mOverview;

    private MainActivity mMainActivity;

    public MainEntryFragment() {
    }

    public MainEntryFragment(MainActivity activity) {
        mContext = activity;
        mMainActivity = activity;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (RelativeLayout)inflater.inflate(R.layout.main_entry_fragment, null);
        mPlan = (Button)mContentView.findViewById(R.id.plan);
        mTest = (Button)mContentView.findViewById(R.id.test);
        mOverview = (Button)mContentView.findViewById(R.id.overview);
        mPlan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMainActivity.switchFragment(MainActivity.CREATE_PLAN_FRAGMENT);
            }
        });
        mTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
        mOverview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMainActivity.switchFragment(MainActivity.OVER_VIEW_FRAGMENT);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }
}
