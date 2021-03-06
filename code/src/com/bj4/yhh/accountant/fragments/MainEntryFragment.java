
package com.bj4.yhh.accountant.fragments;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.dialogs.ApiUnder16DialogHelper;
import com.bj4.yhh.accountant.dialogs.LawVersionDialog;
import com.bj4.yhh.accountant.dialogs.SettingDialog;
import com.bj4.yhh.accountant.dialogs.ShareDialog;
import com.bj4.yhh.accountant.service.ParseService;
import com.bj4.yhh.accountant.utilities.ToastHelper;
import com.bj4.yhh.accountant.utilities.TutorialView;
import com.bj4.yhh.accountant.utilities.TutorialView.Callback;
import com.bj4.yhh.accountant.utilities.Utilities;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainEntryFragment extends BaseFragment {
    private static boolean ENABLE_TUTORIAL = false;

    private static final int DEVELOPER_MODE_BOUNDARY = 30;

    private int mDeveloperModeBoundary = 0;

    private Context mContext;

    private RelativeLayout mContentView;

    private Button mPlan, mTest, mOverview, mCheckUpdate;

    private TextView mMainTitle, mUpdatingProgress;

    private MainActivity mMainActivity;

    private Vibrator mVibrator;

    private Handler mHandler = new Handler();

    private TutorialView mTutorialView;

    private volatile boolean mHasTutorialView = false;

    private Object mTutorialSycn = new Object();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
            ENABLE_TUTORIAL = Utilities.isRamLargerThan1G(mContext);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    new SettingDialog().show(getFragmentManager(), null);
                } else {
                    ApiUnder16DialogHelper.SettingDialog.getNewInstanceDialog(mContext).show();
                }
                return true;
            }
        });
        mContentView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SettingManager sm = SettingManager.getInstance(mContext);
                if (sm.hasDModeOpened() == false) {
                    ++mDeveloperModeBoundary;
                    if (DEVELOPER_MODE_BOUNDARY <= mDeveloperModeBoundary) {
                        sm.setDModeOpened(true);
                        ToastHelper.makeToast(mContext, ToastHelper.TOAST_TYPE_DEVELOPER_MODE)
                                .show();
                    }
                }
            }
        });
        mMainTitle = (TextView)mContentView.findViewById(R.id.main_title);
        if (ENABLE_TUTORIAL) {
            mMainTitle.getViewTreeObserver().addOnGlobalLayoutListener(
                    new OnGlobalLayoutListener() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mMainTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                mMainTitle.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            synchronized (mTutorialSycn) {
                                if (mHasTutorialView == false) {
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            showTitleToturial();
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
        mMainTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    new ShareDialog().show(getFragmentManager(), null);
                } else {
                    ApiUnder16DialogHelper.ShareDialog.getNewInstanceDialog(mContext).show();
                }
            }
        });
        mUpdatingProgress = (TextView)mContentView.findViewById(R.id.update_progress);
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
            }
        });
        mCheckUpdate.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    new LawVersionDialog().show(getFragmentManager(), null);
                } else {
                    ApiUnder16DialogHelper.LawVersionDialog.getNewInstanceDialog(mContext).show();
                }
                return true;
            }
        });
    }

    public void onPause() {
        super.onPause();
        mDeveloperModeBoundary = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                ((ViewGroup)parent).removeView(mContentView);
            }
        }
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

    private void showTitleToturial() {
        if (SettingManager.getInstance(mContext).isFirstUse() == false) {
            return;
        }
        synchronized (mTutorialSycn) {
            mHasTutorialView = true;
        }
        mTutorialView = new TutorialView(mContext);
        mTutorialView.setText(mContext.getString(R.string.tutorial_title),
                TutorialView.POSITION_CENTER);
        mTutorialView.setCallback(new Callback() {
            @Override
            public void onDismiss() {
                if (mContentView != null) {
                    mContentView.removeView(mTutorialView);
                    showUpdatingInfoTutorial();
                }
            }

            @Override
            public void onDrawBackgroundDone() {
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                if (mContentView != null) {
                    mContentView.removeView(mTutorialView);
                }
                mContentView.addView(mTutorialView, rl);
                mTutorialView.bringToFront();
            }

            @Override
            public void onFailed() {
                if (mContentView != null && mTutorialView != null) {
                    mContentView.removeView(mTutorialView);
                }
                SettingManager.getInstance(mContext).setFirstUse();
            }
        });
        mTutorialView.setMainBackground(((MainActivity)getActivity()).getMainBackground(),
                mMainTitle);
    }

    private void showUpdatingInfoTutorial() {
        mTutorialView = new TutorialView(mContext);
        mTutorialView.setText(mContext.getString(R.string.tutorial_updating_info),
                TutorialView.POSITION_TOP);
        mTutorialView.setCallback(new Callback() {
            @Override
            public void onDismiss() {
                if (mContentView != null) {
                    if (mTutorialView != null)
                        mContentView.removeView(mTutorialView);
                    showSettingTutorial();
                }
            }

            @Override
            public void onDrawBackgroundDone() {
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                if (mContentView != null) {
                    if (mTutorialView != null)
                        mContentView.removeView(mTutorialView);
                }
                mContentView.addView(mTutorialView, rl);
                mTutorialView.bringToFront();
            }

            @Override
            public void onFailed() {
                if (mContentView != null && mTutorialView != null) {
                    mContentView.removeView(mTutorialView);
                }
                SettingManager.getInstance(mContext).setFirstUse();
            }
        });
        mTutorialView.setMainBackground(((MainActivity)getActivity()).getMainBackground(),
                mCheckUpdate);
    }

    private void showSettingTutorial() {
        mTutorialView = new TutorialView(mContext);
        mTutorialView.setText(mContext.getString(R.string.tutorial_setting_info),
                TutorialView.POSITION_CENTER);
        mTutorialView.setCallback(new Callback() {
            @Override
            public void onDismiss() {
                if (mContentView != null) {
                    if (mTutorialView != null) {
                        try {
                            mContentView.removeView(mTutorialView);
                        } catch (Exception e) {
                        }
                    }
                    SettingManager.getInstance(mContext).setFirstUse();
                }
            }

            @Override
            public void onDrawBackgroundDone() {
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                if (mContentView != null) {
                    mContentView.removeView(mTutorialView);
                }
                mContentView.addView(mTutorialView, rl);
                mTutorialView.bringToFront();
            }

            @Override
            public void onFailed() {
                if (mContentView != null && mTutorialView != null) {
                    mContentView.removeView(mTutorialView);
                }
                SettingManager.getInstance(mContext).setFirstUse();
            }
        });
        mTutorialView.setMainBackground(((MainActivity)getActivity()).getMainBackground(), null);
    }

    public void setUpdatingProgressVisibility(int visibility) {
        if (mUpdatingProgress != null) {
            mUpdatingProgress.setVisibility(visibility);
            if (visibility == View.VISIBLE) {
                mUpdatingProgress
                        .setText(mContext.getString(R.string.retrieve_progress) + ":  0 %");
            }
        }
    }

    public void setUpdatingProgress(int progress) {
        if (mUpdatingProgress != null) {
            mUpdatingProgress.setText(mContext.getString(R.string.retrieve_progress) + ":  "
                    + progress + " %");
        }
    }
}
