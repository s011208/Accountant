
package com.bj4.yhh.accountant.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.parser.GovLawParser;

public class SettingDialog extends BaseDialog {
    private View mContentView;

    private ImageView mBlueTheme, mGreenTheme, mBlackTheme, mBlueCheck, mGreenCheck, mBlackCheck;

    private Context mContext;

    private SettingManager mSettingManager;

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
                    case R.id.color_black:
                        mBlueCheck.setVisibility(View.GONE);
                        mGreenCheck.setVisibility(View.GONE);
                        mBlackCheck.setVisibility(View.VISIBLE);
                        newTheme = SettingManager.VALUE_THEME_BLACK;
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
        mBlackTheme = (ImageView)mContentView.findViewById(R.id.color_black);
        mGreenTheme = (ImageView)mContentView.findViewById(R.id.color_green);
        mBlueTheme.setOnClickListener(themeClickListener);
        mBlackTheme.setOnClickListener(themeClickListener);
        mGreenTheme.setOnClickListener(themeClickListener);
        mBlueCheck = (ImageView)mContentView.findViewById(R.id.color_blue_selected);
        mGreenCheck = (ImageView)mContentView.findViewById(R.id.color_green_selected);
        mBlackCheck = (ImageView)mContentView.findViewById(R.id.color_black_selected);
        int themeColor = mSettingManager.getThemeColor();
        if (themeColor == SettingManager.VALUE_THEME_BLUE) {
            mBlueCheck.setVisibility(View.VISIBLE);
            mGreenCheck.setVisibility(View.GONE);
            mBlackCheck.setVisibility(View.GONE);
        } else if (themeColor == SettingManager.VALUE_THEME_BLACK) {
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
    }

}
