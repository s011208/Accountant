
package com.bj4.yhh.accountant.dialogs;

import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EnlargeOverViewContentDialog extends BaseDialog {
    private LawAttrs mLawAttrs;

    private int mPlanType;

    public EnlargeOverViewContentDialog(LawAttrs law, int type) {
        mLawAttrs = law;
        mPlanType = type;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.enlarge_over_view_content_dialog, null);
        String fixedTitle = getResources().getString(GovLawParser.getTypeTextResource(mPlanType));
        StringBuilder title = new StringBuilder();
        title.append("\n");
        if (Integer.valueOf(mLawAttrs.mPart) != 0) {
            title.append(" 第 " + mLawAttrs.mPart + " 編 ");
        }
        if (Integer.valueOf(mLawAttrs.mChapter) != 0) {
            title.append(" 第 " + mLawAttrs.mChapter + " 章 ");
        }
        if (Integer.valueOf(mLawAttrs.mSection) != 0) {
            title.append(" 第 " + mLawAttrs.mSection + " 節 ");
        }
        if (Integer.valueOf(mLawAttrs.mSubSection) != 0) {
            title.append(" 第 " + mLawAttrs.mSubSection + " 目 ");
        }
        AlertDialog.Builder builder = getDialogBuilder();
        TextView txt = (TextView)v.findViewById(R.id.enlarge_over_view_content);
        txt.setText(mLawAttrs.mContent);
        builder.setTitle(fixedTitle + title.toString() + mLawAttrs.mLine).setCancelable(true)
                .setView(v);
        return builder.create();
    }

}
