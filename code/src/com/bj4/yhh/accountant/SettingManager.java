
package com.bj4.yhh.accountant;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingManager {

    private static final String SHARE_PREF_NAME = "settings";

    private static final String KEY_SHOW_TEST_ACTIVITY_EXIT_DIALOG = "key_test_activity_exit_dialog";

    private static final String KEY_THEME_COLOR = "theme_color";

    public static final int VALUE_THEME_BLUE = 0;

    public static final int VALUE_THEME_GRAY = 1;

    public static final int VALUE_THEME_GREEN = 2;

    private static final String KEY_FIRST_USE = "key_first_use";

    private static final String KEY_D_MODE = "developer_mode";

    private Context mContext;

    private SharedPreferences mPref;

    public interface ThemeColorChangeCallback {
        public void themeColorChanged(int newTheme);
    }

    private final ArrayList<ThemeColorChangeCallback> mThemeColorChangeCallback = new ArrayList<ThemeColorChangeCallback>();

    private static SettingManager sInstance;

    public synchronized static SettingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingManager(context);
        }
        return sInstance;
    }

    private SettingManager(Context context) {
        mContext = context.getApplicationContext();
    }

    private synchronized SharedPreferences getSharPref() {
        if (mPref == null) {
            mPref = mContext.getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
        }
        return mPref;
    }

    public boolean hasDModeOpened() {
        return getSharPref().getBoolean(KEY_D_MODE, false);
    }

    public void setDModeOpened(boolean open) {
        getSharPref().edit().putBoolean(KEY_D_MODE, open).apply();
    }

    public boolean isFirstUse() {
        return getSharPref().getBoolean(KEY_FIRST_USE, true);
    }

    public void setFirstUse() {
        getSharPref().edit().putBoolean(KEY_FIRST_USE, false).apply();
    }

    public boolean showTestActivityExitDialog() {
        return getSharPref().getBoolean(KEY_SHOW_TEST_ACTIVITY_EXIT_DIALOG, true);
    }

    public void setShowTestActivityExitDialog(boolean show) {
        getSharPref().edit().putBoolean(KEY_SHOW_TEST_ACTIVITY_EXIT_DIALOG, show).apply();
    }

    public void addThemeColorChangeCallback(ThemeColorChangeCallback cb) {
        if (mThemeColorChangeCallback.contains(cb) == false) {
            mThemeColorChangeCallback.add(cb);
        }
    }

    public void removeThemeColorChangeCallback(ThemeColorChangeCallback cb) {
        mThemeColorChangeCallback.remove(cb);
    }

    public int getThemeColor() {
        return getSharPref().getInt(KEY_THEME_COLOR, VALUE_THEME_BLUE);
    }

    public void setThemeColor(int newThemeColor) {
        getSharPref().edit().putInt(KEY_THEME_COLOR, newThemeColor).commit();
        for (ThemeColorChangeCallback cb : mThemeColorChangeCallback) {
            cb.themeColorChanged(newThemeColor);
        }
    }
}
