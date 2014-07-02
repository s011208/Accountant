
package com.bj4.yhh.accountant.fragments;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.dialogs.LawVersionDialog;
import com.bj4.yhh.accountant.dialogs.SettingDialog;
import com.bj4.yhh.accountant.dialogs.ShareDialog;
import com.bj4.yhh.accountant.service.ParseService;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainEntryFragment extends BaseFragment {
    private Context mContext;

    private RelativeLayout mContentView;

    private Button mPlan, mTest, mOverview, mCheckUpdate;

    private TextView mMainTitle;

    private MainActivity mMainActivity;

    private Vibrator mVibrator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
            mMainActivity = (MainActivity)getActivity();
            init();
        }
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        themeColorChanged(SettingManager.getInstance(mContext).getThemeColor());
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (RelativeLayout)inflater.inflate(R.layout.main_entry_fragment, null);
        mContentView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mVibrator.vibrate(50);
                SettingDialog sd = new SettingDialog();
                sd.show(getFragmentManager(), null);
                return true;
            }
        });
        mMainTitle = (TextView)mContentView.findViewById(R.id.main_title);
        mMainTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareDialog().show(getFragmentManager(), null);
            }
        });
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
        mCheckUpdate.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                new LawVersionDialog().show(getFragmentManager(), null);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }

    @Override
    public void themeColorChanged(int newColor) {
        switch (newColor) {
            case SettingManager.VALUE_THEME_BLUE:
                mPlan.setBackgroundResource(R.drawable.blue_btn_bg);
                mTest.setBackgroundResource(R.drawable.blue_btn_bg);
                mOverview.setBackgroundResource(R.drawable.blue_btn_bg);
                mCheckUpdate.setBackgroundResource(R.drawable.blue_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GRAY:
                mPlan.setBackgroundResource(R.drawable.gray_btn_bg);
                mTest.setBackgroundResource(R.drawable.gray_btn_bg);
                mOverview.setBackgroundResource(R.drawable.gray_btn_bg);
                mCheckUpdate.setBackgroundResource(R.drawable.gray_btn_bg);
                break;
            case SettingManager.VALUE_THEME_GREEN:
                mPlan.setBackgroundResource(R.drawable.green_btn_bg);
                mTest.setBackgroundResource(R.drawable.green_btn_bg);
                mOverview.setBackgroundResource(R.drawable.green_btn_bg);
                mCheckUpdate.setBackgroundResource(R.drawable.green_btn_bg);
                break;
        }
    }
}
