
package com.bj4.yhh.accountant.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.parser.GovLawParser;

public class ConfirmToExitDialog extends BaseDialog {
    public static final int OK = 0;

    public static final int CANCEL = 1;

    public interface Callback {
        public void onClick(int result);
    }

    private Callback mCallback;

    public ConfirmToExitDialog(Callback cb) {
        mCallback = cb;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.confirm_to_exit_dialog, null);
        CheckBox cb = (CheckBox)v.findViewById(R.id.show_again);
        cb.setChecked(false);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                SettingManager.getInstance(context).setShowTestActivityExitDialog(!arg1);
            }
        });
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setTitle(R.string.confirm_to_exit_dialog_title).setCancelable(true).setView(v);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (mCallback != null) {
                    mCallback.onClick(OK);
                }
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onClick(CANCEL);
                }
                dismiss();
            }
        });
        return builder.create();
    }
}
