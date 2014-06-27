
package com.bj4.yhh.accountant.activities;

import com.bj4.yhh.accountant.SettingManager;

import android.app.Activity;

public abstract class BaseActivity extends Activity implements
        SettingManager.ThemeColorChangeCallback {
    public void onResume() {
        super.onResume();
        SettingManager.getInstance(getApplicationContext()).addThemeColorChangeCallback(this);
    }

    public void onPause() {
        super.onPause();
        SettingManager.getInstance(getApplicationContext()).removeThemeColorChangeCallback(this);
    }
}
