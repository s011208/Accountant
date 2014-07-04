
package com.bj4.yhh.accountant.activities;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.fragments.BaseFragment;
import com.bj4.yhh.accountant.fragments.CreatePlanFragment;
import com.bj4.yhh.accountant.fragments.MainEntryFragment;
import com.bj4.yhh.accountant.fragments.OverViewFragment;
import com.bj4.yhh.accountant.fragments.TestFragment;
import com.bj4.yhh.accountant.service.ParseService;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MainActivity extends BaseActivity {
    public static final int MAIN_ENTRY_FRAGMENT = 1;

    public static final int CREATE_PLAN_FRAGMENT = 2;

    public static final int OVER_VIEW_FRAGMENT = 3;

    public static final int TEST_FRAGMENT = 4;

    private int mCurrentFragment = MAIN_ENTRY_FRAGMENT;

    private static final String BUNDLE_PREVIOUS_FRAGMENT = "previous_frgment";

    private static final String BUNDLE_PREVIOUD_DISPLAYED_CHILD = "previous_displayed_child";

    private int mPreviousFragment = MAIN_ENTRY_FRAGMENT;

    private int mPreviousDisplayChild = -1;

    private MainEntryFragment mMainEntryFragment;

    private CreatePlanFragment mCreatePlanFragment;

    private OverViewFragment mOverViewFragment;

    private TestFragment mTestFragment;

    private DatabaseHelper mDatabaseHelper;

    private RelativeLayout mMainBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPreviousFragment = savedInstanceState.getInt(BUNDLE_PREVIOUS_FRAGMENT);
            mPreviousDisplayChild = savedInstanceState.getInt(BUNDLE_PREVIOUD_DISPLAYED_CHILD);
        }
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(this);
        setContentView(R.layout.activity_main);
        mMainBackground = (RelativeLayout)findViewById(R.id.main_bg);
        switchFragment(mPreviousFragment, false);
        runParserIfNeeded();
        themeColorChanged(SettingManager.getInstance(this).getThemeColor());
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_PREVIOUS_FRAGMENT, mCurrentFragment);
        int displayedChild = -1;
        if (mCurrentFragment == CREATE_PLAN_FRAGMENT) {
            displayedChild = getCreatePlanFragment().getDisplayedChild();
        } else if (mCurrentFragment == OVER_VIEW_FRAGMENT) {
            displayedChild = getOverViewFragment().getDisplayedChild();
        } else if (mCurrentFragment == TEST_FRAGMENT) {
            displayedChild = getTestFragment().getDisplayedChild();
        }
        outState.putInt(BUNDLE_PREVIOUD_DISPLAYED_CHILD, displayedChild);
    }

    private void runParserIfNeeded() {
        if (mDatabaseHelper.isLawTableEmpty()) {
            Intent intent = new Intent(this, ParseService.class);
            intent.putExtra(ParseService.PARSE_ALL, true);
            startService(intent);
        }
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
        } else if (mCurrentFragment == TEST_FRAGMENT) {
            if (getTestFragment().getDisplayedChild() == TestFragment.TEST_FRAGMENT_LAW_LIST) {
                getTestFragment().setDisplayedChild(TestFragment.TEST_FRAGMENT_TEST_TYPE);
            } else {
                switchFragment(MAIN_ENTRY_FRAGMENT);
            }
        } else {
            super.onBackPressed();
        }
    }

    public void switchFragment(int targetFragment) {
        switchFragment(targetFragment, true);
    }

    public void switchFragment(int targetFragment, boolean animated) {
        Fragment target = getMainEntryFragment();
        int animationIn = R.anim.fragment_slide_in_r_to_l, animationOut = R.anim.fragment_slide_out_r_to_l;
        switch (targetFragment) {
            case MAIN_ENTRY_FRAGMENT:
                target = getMainEntryFragment();
                mCurrentFragment = MAIN_ENTRY_FRAGMENT;
                animationIn = R.anim.fragment_slide_in_l_to_r;
                animationOut = R.anim.fragment_slide_out_l_to_r;
                break;
            case CREATE_PLAN_FRAGMENT:
                target = getCreatePlanFragment();
                if (animated == false && mPreviousDisplayChild != -1) {
                    getCreatePlanFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = CREATE_PLAN_FRAGMENT;
                break;
            case OVER_VIEW_FRAGMENT:
                target = getOverViewFragment();
                if (animated == false && mPreviousDisplayChild != -1) {
                    getOverViewFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = OVER_VIEW_FRAGMENT;
                break;
            case TEST_FRAGMENT:
                target = getTestFragment();
                if (animated == false && mPreviousDisplayChild != -1) {
                    getTestFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = TEST_FRAGMENT;
                break;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animated) {
            transaction.setCustomAnimations(animationIn, animationOut)
                    .replace(R.id.main_fragment, target).commit();
        } else {
            transaction.replace(R.id.main_fragment, target).commit();
        }
        mPreviousDisplayChild = -1;
    }

    private BaseFragment getCurrentFragment() {
        BaseFragment target = getMainEntryFragment();
        switch (mCurrentFragment) {
            case MAIN_ENTRY_FRAGMENT:
                target = getMainEntryFragment();
                break;
            case CREATE_PLAN_FRAGMENT:
                target = getCreatePlanFragment();
                break;
            case OVER_VIEW_FRAGMENT:
                target = getOverViewFragment();
                break;
            case TEST_FRAGMENT:
                target = getTestFragment();
                break;
        }
        return target;
    }

    private synchronized TestFragment getTestFragment() {
        if (mTestFragment == null) {
            mTestFragment = new TestFragment();
        }
        return mTestFragment;
    }

    private synchronized OverViewFragment getOverViewFragment() {
        if (mOverViewFragment == null) {
            mOverViewFragment = new OverViewFragment();
        }
        return mOverViewFragment;
    }

    private synchronized MainEntryFragment getMainEntryFragment() {
        if (mMainEntryFragment == null) {
            mMainEntryFragment = new MainEntryFragment();
        }
        return mMainEntryFragment;
    }

    private synchronized CreatePlanFragment getCreatePlanFragment() {
        if (mCreatePlanFragment == null) {
            mCreatePlanFragment = new CreatePlanFragment();
        }
        return mCreatePlanFragment;
    }

    public RelativeLayout getMainBackground() {
        return mMainBackground;
    }

    @Override
    public void themeColorChanged(int newTheme) {
        try {
            getCurrentFragment().themeColorChanged(newTheme);
        } catch (Exception e) {
            // do nothing
        }
        switch (newTheme) {
            case SettingManager.VALUE_THEME_BLUE:
                mMainBackground.setBackgroundResource(R.drawable.blue_main_paper_bg);
                break;
            case SettingManager.VALUE_THEME_GRAY:
                mMainBackground.setBackgroundResource(R.drawable.gray_main_paper_bg);
                break;
            case SettingManager.VALUE_THEME_GREEN:
                mMainBackground.setBackgroundResource(R.drawable.green_main_paper_bg);
                break;
        }
    }

}
