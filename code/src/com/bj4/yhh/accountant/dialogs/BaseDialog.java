
package com.bj4.yhh.accountant.dialogs;

import com.bj4.yhh.accountant.SettingManager;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class BaseDialog extends DialogFragment {

    protected boolean mDisableAnimation = false;

    protected View mContentView;

    private void startAnimation() {
        try {
            if (!mDisableAnimation && mContentView != null
                    && SettingManager.getInstance(getActivity()).enableHighPerformance() == false) {
                int type = ((int)(Math.random() * 1000)) % 3;
                switch (type) {
                    case 0:
                        startAnimationType0();
                        break;
                    case 1:
                        startAnimationType1();
                        break;
                    case 2:
                        startAnimationType2();
                        break;
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void startAnimationType0() {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1.2f, 0.95f, 1.02f, 1f);
        va.setDuration(500);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float)animation.getAnimatedValue();
                mContentView.setScaleY(value);
                mContentView.setScaleX(value);
            }
        });
        va.start();
    }

    private void startAnimationType1() {
        ValueAnimator va = ValueAnimator.ofFloat(4f, 1f);
        va.setDuration(500);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float)animation.getAnimatedValue();
                mContentView.setScaleY(value);
                mContentView.setScaleX(value);
            }
        });
        va.start();
    }

    private void startAnimationType2() {
        ValueAnimator va = ValueAnimator.ofFloat(20, -15, 10, -5, 3, -1, 0);
        va.setDuration(500);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float)animation.getAnimatedValue();
                mContentView.setRotation(value);
            }
        });
        va.start();
    }

    public AlertDialog.Builder getDialogBuilder() {
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        setupGravityAndPosition();
        startAnimation();
        return container;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    public void setupGravityAndPosition() {
        Dialog dialog = getDialog();
        Window window = dialog.getWindow();

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
    }
}
