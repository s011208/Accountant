
package com.bj4.yhh.accountant.fragments;

import com.bj4.yhh.accountant.SettingManager;

import android.app.Fragment;

public abstract class BaseFragment extends Fragment {
    public abstract void themeColorChanged(int newColor);

    protected boolean mEnableHighPerformance = false;

    public void onResume() {
        super.onResume();
        mEnableHighPerformance = SettingManager.getInstance(getActivity()).enableHighPerformance();
    }
}
