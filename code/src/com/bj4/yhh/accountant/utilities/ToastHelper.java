
package com.bj4.yhh.accountant.utilities;

import com.bj4.yhh.accountant.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastHelper {

    public static final int TOAST_TYPE_START_LOAD = 0;

    public static final int TOAST_TYPE_UPDATE_RESULT_OK = 1;

    public static final int TOAST_TYPE_UPDATE_RESULT_FAIL = 2;

    public static final int TOAST_TYPE_START_UPDATE = 3;

    public static final int TOAST_TYPE_DOWNLOAD_LAWS_INADVANCE = 4;

    public static final int TOAST_TYPE_REMOVE_PLANS_INADVANCE = 5;

    public static final int TOAST_TYPE_WAITING_FOR_PREVIOUS_PARSING = 6;

    public static final int TOAST_TYPE_WRONG_ESTIMATE_DAY = 7;

    public static final int TOAST_TYPE_NONE_PREVIOUS_TEST_FRAGMENT_DATA = 8;

    public static final int TOAST_TYPE_DONE_TYPE_PLAN = 9;

    public static final int TOAST_TYPE_CREATE_PLANS_IN_ADVANCE = 10;

    public static final Toast makeToast(Context context, int type) {
        Toast rtn = new Toast(context);
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View parent = inflater.inflate(R.layout.toast_update_view, null);
        ImageView img = (ImageView)parent.findViewById(R.id.toast_icon);
        TextView txt = (TextView)parent.findViewById(R.id.toast_text);
        switch (type) {
            case TOAST_TYPE_START_LOAD:
                img.setImageResource(R.drawable.toast_start_update_icon);
                txt.setText(R.string.toast_start_loading);
                break;
            case TOAST_TYPE_UPDATE_RESULT_OK:
                img.setImageResource(R.drawable.toast_success_icon);
                txt.setText(R.string.toast_result_ok);
                break;
            case TOAST_TYPE_UPDATE_RESULT_FAIL:
                img.setImageResource(R.drawable.toast_fail_icon);
                txt.setText(R.string.toast_result_failed);
                break;
            case TOAST_TYPE_START_UPDATE:
                img.setImageResource(R.drawable.toast_start_update_icon);
                txt.setText(R.string.toast_start_updating);
                break;
            case TOAST_TYPE_DOWNLOAD_LAWS_INADVANCE:
                img.setImageResource(R.drawable.toast_icon_alert);
                txt.setText(R.string.toast_download_laws_in_advance);
                break;
            case TOAST_TYPE_REMOVE_PLANS_INADVANCE:
                img.setImageResource(R.drawable.toast_icon_forbidden);
                txt.setText(R.string.toast_remove_plans_in_advance);
                break;
            case TOAST_TYPE_WAITING_FOR_PREVIOUS_PARSING:
                img.setImageResource(R.drawable.toast_icon_forbidden);
                txt.setText(R.string.toast_waiting_for_previous_parsing);
                break;
            case TOAST_TYPE_WRONG_ESTIMATE_DAY:
                img.setImageResource(R.drawable.toast_icon_forbidden);
                txt.setText(R.string.toast_wrong_estimate_day);
                break;
            case TOAST_TYPE_NONE_PREVIOUS_TEST_FRAGMENT_DATA:
                img.setImageResource(R.drawable.toast_icon_forbidden);
                txt.setText(R.string.test_fragment_none_previous_test_data);
                break;
            case TOAST_TYPE_DONE_TYPE_PLAN:
                img.setImageResource(R.drawable.toast_icon_check);
                txt.setText(R.string.toast_done_type_plan);
                break;
            case TOAST_TYPE_CREATE_PLANS_IN_ADVANCE:
                img.setImageResource(R.drawable.toast_icon_forbidden);
                txt.setText(R.string.toast_create_plans_in_advance);
                break;
        }
        rtn.setView(parent);
        rtn.setDuration(Toast.LENGTH_SHORT);
        rtn.setGravity(Gravity.BOTTOM, 0,
                (int)context.getResources().getDimension(R.dimen.updating_toast_y_offset));
        return rtn;
    }
}
