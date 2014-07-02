
package com.bj4.yhh.accountant.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.bj4.yhh.accountant.R;

public class ShareDialog extends BaseDialog {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.share_to_dialog, null);
        LinearLayout shareTo = (LinearLayout)v.findViewById(R.id.share_to);
        LinearLayout sendSuggestion = (LinearLayout)v.findViewById(R.id.send_suggestion);
        LinearLayout gradeMe = (LinearLayout)v.findViewById(R.id.grade_me);
        shareTo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                email(context,
                        "",
                        "",
                        context.getString(R.string.app_name),
                        context.getString(R.string.share_to_text) + " "
                                + context.getString(R.string.app_name) + "\n"
                                + "http://play.google.com/store/apps/details?id="
                                + context.getApplicationContext().getPackageName());
            }
        });
        sendSuggestion.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                email(context, "bj4dev@gmail.com", "", context.getString(R.string.app_name), "");
            }
        });
        gradeMe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                Uri uri = Uri.parse("market://details?id="
                        + context.getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("http://play.google.com/store/apps/details?id="
                                    + context.getApplicationContext().getPackageName())));
                }
            }
        });
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setTitle(R.string.share_dialog_title).setCancelable(true).setView(v);
        return builder.create();
    }

    public static void email(Context context, String emailTo, String emailCC, String subject,
            String emailText) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        if (emailTo != null) {
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
                emailTo
            });
        }
        if (emailCC != null) {
            emailIntent.putExtra(android.content.Intent.EXTRA_CC, new String[] {
                emailCC
            });
        }
        if (subject != null) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (emailText != null) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
        }
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
