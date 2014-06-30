
package com.bj4.yhh.accountant.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

public class LawVersionDialog extends BaseDialog {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        Cursor data = AccountantApplication.getDatabaseHelper(context).getLawUpdateTime();
        StringBuilder sb = new StringBuilder();

        if (data != null) {
            while (data.moveToNext()) {
                int type = data.getInt(data.getColumnIndex(DatabaseHelper.COLUMN_LAW_TYPE));
                String typeString = GovLawParser.getTypeText(context, type);
                String updateTime = data.getString(data
                        .getColumnIndex(DatabaseHelper.COLUMN_LAW_UPDATE_TIME));
                sb.append(typeString + ":\n" + updateTime + "\n");
            }
            data.close();
        }
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setTitle(R.string.law_update_time_dialog_title).setCancelable(true)
                .setMessage(sb.toString());
        return builder.create();
    }
}
