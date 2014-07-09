
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
import com.bj4.yhh.accountant.service.ParseServiceBinder;
import com.bj4.yhh.accountant.service.ParseServiceCallback;
import com.bj4.yhh.accountant.utilities.GA;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

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

    private ParseServiceBinder mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ParseServiceBinder.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
            }
            mService = null;
        }
    };

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

    public void onResume() {
        super.onResume();
        bindService();
    }

    public void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    public void bindService() {
        Intent intent = new Intent(this, ParseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private ParseServiceCallback mCallback = new ParseServiceCallback.Stub() {

        @Override
        public void loadingProcess(final int percentage) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentFragment == MAIN_ENTRY_FRAGMENT) {
                        getMainEntryFragment().setUpdatingProgress(percentage);
                    }
                }
            });
        }

        @Override
        public void loadingDone() throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentFragment == MAIN_ENTRY_FRAGMENT) {
                        getMainEntryFragment().setUpdatingProgressVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        @Override
        public void startLoading() throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentFragment == MAIN_ENTRY_FRAGMENT) {
                        getMainEntryFragment().setUpdatingProgressVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

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
        Fragment target = getCurrentFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // do nothing
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (target.isAdded()) {
                transaction.remove(target);
            }
        } else {
            // ?
        }
        int animationIn = R.anim.fragment_slide_in_r_to_l, animationOut = R.anim.fragment_slide_out_r_to_l;
        switch (targetFragment) {
            case MAIN_ENTRY_FRAGMENT:
                target = getMainEntryFragment();
                GA.sendEvents(getApplicationContext(), GA.CATEGORY.CATEGORY_MAIN_ENRTY_FRAGMENT,
                        null, null, null);
                mCurrentFragment = MAIN_ENTRY_FRAGMENT;
                animationIn = R.anim.fragment_slide_in_l_to_r;
                animationOut = R.anim.fragment_slide_out_l_to_r;
                break;
            case CREATE_PLAN_FRAGMENT:
                target = getCreatePlanFragment();
                GA.sendEvents(getApplicationContext(), GA.CATEGORY.CATEGORY_CREATE_PLAN_FRAGMENT,
                        null, null, null);
                if (animated == false && mPreviousDisplayChild != -1) {
                    getCreatePlanFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = CREATE_PLAN_FRAGMENT;
                break;
            case OVER_VIEW_FRAGMENT:
                target = getOverViewFragment();
                GA.sendEvents(getApplicationContext(), GA.CATEGORY.CATEGORY_OVER_VIEW_FRAGMENT,
                        null, null, null);
                if (animated == false && mPreviousDisplayChild != -1) {
                    getOverViewFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = OVER_VIEW_FRAGMENT;
                break;
            case TEST_FRAGMENT:
                target = getTestFragment();
                GA.sendEvents(getApplicationContext(), GA.CATEGORY.CATEGORY_TEST_FRAGMENT, null,
                        null, null);
                if (animated == false && mPreviousDisplayChild != -1) {
                    getTestFragment().setDisplayedChild(mPreviousDisplayChild);
                }
                mCurrentFragment = TEST_FRAGMENT;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (animated) {
                transaction.setCustomAnimations(animationIn, animationOut)
                        .replace(R.id.main_fragment, target).commit();
            } else {
                transaction.replace(R.id.main_fragment, target).commit();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (animated) {
                transaction.setCustomAnimations(animationIn, animationOut)
                        .add(R.id.main_fragment, target).commit();
            } else {
                transaction.add(R.id.main_fragment, target).commit();
            }
        } else {
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
