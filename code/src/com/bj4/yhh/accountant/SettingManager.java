
package com.bj4.yhh.accountant;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {

    private static final String SHARE_PREF_NAME = "settings";

    private static final String KEY_THEME_COLOR = "theme_color";

    public static final int VALUE_THEME_BLUE = 0;

    public static final int VALUE_THEME_GRAY = 1;

    public static final int VALUE_THEME_GREEN = 2;

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
