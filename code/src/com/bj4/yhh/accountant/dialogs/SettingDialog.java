
package com.bj4.yhh.accountant.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.parser.GovLawParser;

public class SettingDialog extends BaseDialog {
    private ImageView mBlueTheme, mGreenTheme, mBlackTheme, mBlueCheck, mGreenCheck, mBlackCheck;

    private Context mContext;

    private SettingManager mSettingManager;

    private CheckBox mConfirmWhenExitTest;

    public SettingDialog() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        init();
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setTitle(R.string.basic_setting_title).setCancelable(true).setView(mContentView);
        return builder.create();
    }

    private void initTheme() {
        // theme
        OnClickListener themeClickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                int vid = v.getId();
                int newTheme = SettingManager.VALUE_THEME_BLUE;
                switch (vid) {
                    case R.id.color_blue:
                        mBlueCheck.setVisibility(View.VISIBLE);
                        mGreenCheck.setVisibility(View.GONE);
                        mBlackCheck.setVisibility(View.GONE);
                        newTheme = SettingManager.VALUE_THEME_BLUE;
                        break;
                    case R.id.color_gray:
                        mBlueCheck.setVisibility(View.GONE);
                        mGreenCheck.setVisibility(View.GONE);
                        mBlackCheck.setVisibility(View.VISIBLE);
                        newTheme = SettingManager.VALUE_THEME_GRAY;
                        break;
                    case R.id.color_green:
                        mBlueCheck.setVisibility(View.GONE);
                        mGreenCheck.setVisibility(View.VISIBLE);
                        mBlackCheck.setVisibility(View.GONE);
                        newTheme = SettingManager.VALUE_THEME_GREEN;
                        break;
                }
                mSettingManager.setThemeColor(newTheme);
            }
        };
        mBlueTheme = (ImageView)mContentView.findViewById(R.id.color_blue);
        mBlackTheme = (ImageView)mContentView.findViewById(R.id.color_gray);
        mGreenTheme = (ImageView)mContentView.findViewById(R.id.color_green);
        mBlueTheme.setOnClickListener(themeClickListener);
        mBlackTheme.setOnClickListener(themeClickListener);
        mGreenTheme.setOnClickListener(themeClickListener);
        mBlueCheck = (ImageView)mContentView.findViewById(R.id.color_blue_selected);
        mGreenCheck = (ImageView)mContentView.findViewById(R.id.color_green_selected);
        mBlackCheck = (ImageView)mContentView.findViewById(R.id.color_gray_selected);
        int themeColor = mSettingManager.getThemeColor();
        if (themeColor == SettingManager.VALUE_THEME_BLUE) {
            mBlueCheck.setVisibility(View.VISIBLE);
            mGreenCheck.setVisibility(View.GONE);
            mBlackCheck.setVisibility(View.GONE);
        } else if (themeColor == SettingManager.VALUE_THEME_GRAY) {
            mBlueCheck.setVisibility(View.GONE);
            mGreenCheck.setVisibility(View.GONE);
            mBlackCheck.setVisibility(View.VISIBLE);
        } else if (themeColor == SettingManager.VALUE_THEME_GREEN) {
            mBlueCheck.setVisibility(View.GONE);
            mGreenCheck.setVisibility(View.VISIBLE);
            mBlackCheck.setVisibility(View.GONE);
        }

    }

    private void init() {
        mContext = getActivity();
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.settings_dialog, null);
        mSettingManager = SettingManager.getInstance(mContext);
        initTheme();
        initConfirmWhenExitTest();
        initDeveloperMode();
        initHighPerformanceMode();
    }

    private void initConfirmWhenExitTest() {
        // confirm when exit
        mConfirmWhenExitTest = (CheckBox)mContentView
                .findViewById(R.id.settings_confirm_when_exit_test_activity);
        mConfirmWhenExitTest.setChecked(mSettingManager.showTestActivityExitDialog());
        mConfirmWhenExitTest.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mSettingManager.setShowTestActivityExitDialog(isChecked);
            }
        });
    }

    private void initHighPerformanceMode() {
        CheckBox enableHighPerformance = (CheckBox)mContentView
                .findViewById(R.id.settings_high_performance_mode);
        if (enableHighPerformance != null) {
            enableHighPerformance.setChecked(!mSettingManager.enableHighPerformance());
            enableHighPerformance.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSettingManager.setEnableHighPerformance(!isChecked);
                }
            });
        }
    }

    private void initDeveloperMode() {
        View parent = mContentView.findViewById(R.id.developer_mode);
        if (mSettingManager.hasDModeOpened()) {
            parent.setVisibility(View.VISIBLE);
            CheckBox developerCb = (CheckBox)mContentView
                    .findViewById(R.id.settings_developer_mode);
            developerCb.setChecked(true);
            developerCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSettingManager.setDModeOpened(isChecked);
                }
            });
        } else {
            parent.setVisibility(View.GONE);
        }
    }

}
