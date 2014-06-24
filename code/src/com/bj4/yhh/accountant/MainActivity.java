
package com.bj4.yhh.accountant;

import com.bj4.yhh.accountant.fragments.CreatePlanFragment;
import com.bj4.yhh.accountant.fragments.MainEntryFragment;
import com.bj4.yhh.accountant.fragments.OverViewFragment;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.os.Build;

public class MainActivity extends Activity {
    public static final int MAIN_ENTRY_FRAGMENT = 1;

    public static final int CREATE_PLAN_FRAGMENT = 2;

    public static final int OVER_VIEW_FRAGMENT = 3;

    private int mCurrentFragment = MAIN_ENTRY_FRAGMENT;

    private MainEntryFragment mMainEntryFragment;

    private CreatePlanFragment mCreatePlanFragment;

    private OverViewFragment mOverViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchFragment(MAIN_ENTRY_FRAGMENT);
        new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY)).start();
        new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND)).start();
        new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TEXT_COLLECTION)).start();
    }

    public void onBackPressed() {
        if (mCurrentFragment == CREATE_PLAN_FRAGMENT) {
            if (getCreatePlanFragment().getDisplayedChild() == CreatePlanFragment.CREATE_PLAIN_EDIT) {
                getCreatePlanFragment().setDisplayedChild(
                        CreatePlanFragment.CREATE_PLAN_MANAGE_PAGE);
            } else {
                switchFragment(MAIN_ENTRY_FRAGMENT);
            }
        } else if (mCurrentFragment == OVER_VIEW_FRAGMENT) {
            if (getOverViewFragment().getDisplayedChild() == OverViewFragment.OVERVIEW_FRAGMENT_LAW_CONTENT) {
                getOverViewFragment()
                        .setDisplayedChild(OverViewFragment.OVERVIEW_FRAGMENT_LAW_LIST);
            } else {
                switchFragment(MAIN_ENTRY_FRAGMENT);
            }
        } else {
            super.onBackPressed();
        }
    }

    public void switchFragment(int targetFragment) {
        Fragment target = getMainEntryFragment();
        switch (targetFragment) {
            case MAIN_ENTRY_FRAGMENT:
                target = getMainEntryFragment();
                mCurrentFragment = MAIN_ENTRY_FRAGMENT;
                break;
            case CREATE_PLAN_FRAGMENT:
                target = getCreatePlanFragment();
                mCurrentFragment = CREATE_PLAN_FRAGMENT;
                break;
            case OVER_VIEW_FRAGMENT:
                target = getOverViewFragment();
                mCurrentFragment = OVER_VIEW_FRAGMENT;
                break;
        }
        getFragmentManager().beginTransaction().replace(R.id.main_fragment, target).commit();
    }

    private synchronized OverViewFragment getOverViewFragment() {
        if (mOverViewFragment == null) {
            mOverViewFragment = new OverViewFragment(this);
        }
        return mOverViewFragment;
    }

    private synchronized MainEntryFragment getMainEntryFragment() {
        if (mMainEntryFragment == null) {
            mMainEntryFragment = new MainEntryFragment(this);
        }
        return mMainEntryFragment;
    }

    private synchronized CreatePlanFragment getCreatePlanFragment() {
        if (mCreatePlanFragment == null) {
            mCreatePlanFragment = new CreatePlanFragment(this);
        }
        return mCreatePlanFragment;
    }

}
