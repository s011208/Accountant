
package com.bj4.yhh.accountant.dialogs;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.LawPara;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.dialogs.ConfirmToExitDialog.Callback;
import com.bj4.yhh.accountant.dialogs.LawParagraphDialog.LawParagraphListAdapter;
import com.bj4.yhh.accountant.dialogs.LawParagraphDialog.LawParagraphListAdapter.ViewHolder;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ApiUnder16DialogHelper {
    public abstract static class BaseDialog {
        protected Context mContext;

        protected Dialog mDialog;

        protected View mContentView;

        protected BaseDialog(Context c) {
            mContext = c;
        }

        private void setupGravityAndPosition() {
            Window window = mDialog.getWindow();

            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
        }

        protected abstract void init();

        protected Dialog getNewDialog() {
            mDialog = new Dialog(new ContextThemeWrapper(mContext,
                    android.R.style.Theme_Holo_Light_Dialog));
            setupGravityAndPosition();
            return mDialog;
        }
    }

    public static class EnlargeOverViewContentDialog extends BaseDialog {
        private LawAttrs mLawAttrs;

        private int mPlanType;

        protected EnlargeOverViewContentDialog(Context c, LawAttrs law, int type) {
            super(c);
            mLawAttrs = law;
            mPlanType = type;
        }

        @Override
        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.enlarge_over_view_content_dialog, null);
            String fixedTitle = mContext.getResources().getString(
                    GovLawParser.getTypeTextResource(mPlanType));
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
            TextView txt = (TextView)mContentView.findViewById(R.id.enlarge_over_view_content);
            txt.setText(mLawAttrs.mContent);
            mDialog.setTitle(fixedTitle + title.toString() + mLawAttrs.mLine);
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c, LawAttrs law, int type) {
            EnlargeOverViewContentDialog dialog = new EnlargeOverViewContentDialog(c, law, type);
            return dialog.getNewDialog();
        }
    }

    public static class ConfirmToExitDialog extends BaseDialog {
        public interface Callback {
            public void onClick(int result);
        }

        private Callback mCallback;

        protected ConfirmToExitDialog(Context c, Callback cb) {
            super(c);
            mCallback = cb;
        }

        @Override
        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.confirm_to_exit_dialog_api_below_16, null);
            CheckBox cb = (CheckBox)mContentView.findViewById(R.id.show_again);
            cb.setChecked(false);
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    SettingManager.getInstance(mContext).setShowTestActivityExitDialog(!arg1);
                }
            });
            Button cancel = (Button)mContentView.findViewById(R.id.cancel);
            Button ok = (Button)mContentView.findViewById(R.id.ok);
            ok.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onClick(com.bj4.yhh.accountant.dialogs.ConfirmToExitDialog.OK);
                    }
                    mDialog.dismiss();
                }
            });
            cancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback
                                .onClick(com.bj4.yhh.accountant.dialogs.ConfirmToExitDialog.CANCEL);
                    }
                    mDialog.dismiss();
                }
            });
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setTitle(R.string.confirm_to_exit_dialog_title);
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c, Callback cb) {
            ConfirmToExitDialog dialog = new ConfirmToExitDialog(c, cb);
            return dialog.getNewDialog();
        }
    }

    public static class LawVersionDialog extends BaseDialog {

        protected LawVersionDialog(Context c) {
            super(c);
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setTitle(R.string.law_update_time_dialog_title);
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c) {
            LawVersionDialog dialog = new LawVersionDialog(c);
            return dialog.getNewDialog();
        }

        @Override
        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.law_version_dialog_api_below_16, null);
            TextView content = (TextView)mContentView.findViewById(R.id.law_version_content);
            Cursor data = AccountantApplication.getDatabaseHelper(mContext).getLawUpdateTime();
            StringBuilder sb = new StringBuilder();
            if (data != null) {
                while (data.moveToNext()) {
                    int type = data.getInt(data.getColumnIndex(DatabaseHelper.COLUMN_LAW_TYPE));
                    String typeString = GovLawParser.getTypeText(mContext, type);
                    String updateTime = data.getString(data
                            .getColumnIndex(DatabaseHelper.COLUMN_LAW_UPDATE_TIME));
                    if (updateTime == null) {
                        updateTime = mContext.getString(R.string.no_updated_time);
                    }
                    sb.append(typeString + ":\n" + updateTime + "\n");
                }
                data.close();
            }
            content.setText(sb.toString());
            content.setGravity(Gravity.CENTER);
        }
    }

    public static class ShareDialog extends BaseDialog {

        protected ShareDialog(Context c) {
            super(c);
        }

        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.share_to_dialog, null);
            LinearLayout shareTo = (LinearLayout)mContentView.findViewById(R.id.share_to);
            LinearLayout sendSuggestion = (LinearLayout)mContentView
                    .findViewById(R.id.send_suggestion);
            LinearLayout gradeMe = (LinearLayout)mContentView.findViewById(R.id.grade_me);
            shareTo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    com.bj4.yhh.accountant.dialogs.ShareDialog.email(
                            mContext,
                            "",
                            "",
                            mContext.getString(R.string.app_name),
                            mContext.getString(R.string.share_to_text) + " "
                                    + mContext.getString(R.string.app_name) + "\n"
                                    + "http://play.google.com/store/apps/details?id="
                                    + mContext.getApplicationContext().getPackageName());
                }
            });
            sendSuggestion.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    com.bj4.yhh.accountant.dialogs.ShareDialog.email(mContext, "bj4dev@gmail.com",
                            "", mContext.getString(R.string.app_name), "");
                }
            });
            gradeMe.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    Uri uri = Uri.parse("market://details?id="
                            + mContext.getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        mContext.startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse("http://play.google.com/store/apps/details?id="
                                        + mContext.getApplicationContext().getPackageName())));
                    }
                }
            });
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setTitle(R.string.share_dialog_title);
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c) {
            ShareDialog dialog = new ShareDialog(c);
            return dialog.getNewDialog();
        }
    }

    public static class SettingDialog extends BaseDialog {
        private ImageView mBlueTheme, mGreenTheme, mBlackTheme, mBlueCheck, mGreenCheck,
                mBlackCheck;

        private SettingManager mSettingManager;

        private CheckBox mConfirmWhenExitTest;

        protected SettingDialog(Context c) {
            super(c);
        }

        private void initTheme() {
            // theme
            OnClickListener themeClickListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int vid = v.getId();
                    int newTheme = SettingManager.VALUE_THEME_BLUE;
                    switch (vid) {
                        case R.id.color_blue:
                            mBlueCheck.setVisibility(View.VISIBLE);
                            mGreenCheck.setVisibility(View.GONE);
                            mBlackCheck.setVisibility(View.GONE);
                            newTheme = SettingManager.VALUE_THEME_BLUE;
                            break;
                        case R.id.color_gray:
                            mBlueCheck.setVisibility(View.GONE);
                            mGreenCheck.setVisibility(View.GONE);
                            mBlackCheck.setVisibility(View.VISIBLE);
                            newTheme = SettingManager.VALUE_THEME_GRAY;
                            break;
                        case R.id.color_green:
                            mBlueCheck.setVisibility(View.GONE);
                            mGreenCheck.setVisibility(View.VISIBLE);
                            mBlackCheck.setVisibility(View.GONE);
                            newTheme = SettingManager.VALUE_THEME_GREEN;
                            break;
                    }
                    mSettingManager.setThemeColor(newTheme);
                }
            };
            mBlueTheme = (ImageView)mContentView.findViewById(R.id.color_blue);
            mBlackTheme = (ImageView)mContentView.findViewById(R.id.color_gray);
            mGreenTheme = (ImageView)mContentView.findViewById(R.id.color_green);
            mBlueTheme.setOnClickListener(themeClickListener);
            mBlackTheme.setOnClickListener(themeClickListener);
            mGreenTheme.setOnClickListener(themeClickListener);
            mBlueCheck = (ImageView)mContentView.findViewById(R.id.color_blue_selected);
            mGreenCheck = (ImageView)mContentView.findViewById(R.id.color_green_selected);
            mBlackCheck = (ImageView)mContentView.findViewById(R.id.color_gray_selected);
            int themeColor = mSettingManager.getThemeColor();
            if (themeColor == SettingManager.VALUE_THEME_BLUE) {
                mBlueCheck.setVisibility(View.VISIBLE);
                mGreenCheck.setVisibility(View.GONE);
                mBlackCheck.setVisibility(View.GONE);
            } else if (themeColor == SettingManager.VALUE_THEME_GRAY) {
                mBlueCheck.setVisibility(View.GONE);
                mGreenCheck.setVisibility(View.GONE);
                mBlackCheck.setVisibility(View.VISIBLE);
            } else if (themeColor == SettingManager.VALUE_THEME_GREEN) {
                mBlueCheck.setVisibility(View.GONE);
                mGreenCheck.setVisibility(View.VISIBLE);
                mBlackCheck.setVisibility(View.GONE);
            }

        }

        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.settings_dialog, null);
            mSettingManager = SettingManager.getInstance(mContext);
            initTheme();
            initConfirmWhenExitTest();
            initDeveloperMode();
        }

        private void initConfirmWhenExitTest() {
            // confirm when exit
            mConfirmWhenExitTest = (CheckBox)mContentView
                    .findViewById(R.id.settings_confirm_when_exit_test_activity);
            mConfirmWhenExitTest.setChecked(mSettingManager.showTestActivityExitDialog());
            mConfirmWhenExitTest.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // TODO Auto-generated method stub
                    mSettingManager.setShowTestActivityExitDialog(isChecked);
                }
            });
        }

        private void initDeveloperMode() {
            View parent = mContentView.findViewById(R.id.developer_mode);
            if (mSettingManager.hasDModeOpened()) {
                parent.setVisibility(View.VISIBLE);
                CheckBox developerCb = (CheckBox)mContentView
                        .findViewById(R.id.settings_developer_mode);
                developerCb.setChecked(true);
                developerCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSettingManager.setDModeOpened(isChecked);
                    }
                });
            } else {
                parent.setVisibility(View.GONE);
            }
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setTitle(R.string.basic_setting_title);
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c) {
            SettingDialog dialog = new SettingDialog(c);
            return dialog.getNewDialog();
        }
    }

    public static class LawParagraphDialog extends BaseDialog {
        private int mLawType = 0;

        private TextView mNoDataHint;

        private com.bj4.yhh.accountant.dialogs.LawParagraphDialog.Callback mCallback;

        protected LawParagraphDialog(Context c, int type,
                com.bj4.yhh.accountant.dialogs.LawParagraphDialog.Callback cb) {
            super(c);
            mLawType = type;
            mCallback = cb;
        }

        @Override
        protected void init() {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContentView = inflater.inflate(R.layout.law_paragraph_dialog, null);
            mNoDataHint = (TextView)mContentView.findViewById(R.id.law_paragraph_no_data_hint);
            ListView list = (ListView)mContentView.findViewById(R.id.law_paragraph_list);
            final LawParagraphListAdapter adapter = new LawParagraphListAdapter(mContext, mLawType);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (mCallback != null) {
                        mCallback.onItemSelected(adapter.getItem(arg2).mTitle);
                        mDialog.dismiss();
                    }
                }
            });
        }

        protected Dialog getNewDialog() {
            mDialog = super.getNewDialog();
            init();
            mDialog.setTitle(R.string.law_paragraph_dialog_title);
            mDialog.setContentView(mContentView);
            mDialog.setCancelable(true);
            return mDialog;
        }

        public static Dialog getNewInstanceDialog(Context c, int type,
                com.bj4.yhh.accountant.dialogs.LawParagraphDialog.Callback cb) {
            LawParagraphDialog dialog = new LawParagraphDialog(c, type, cb);
            return dialog.getNewDialog();
        }

        class LawParagraphListAdapter extends BaseAdapter {

            private final ArrayList<LawPara> mData = new ArrayList<LawPara>();

            private Context mContext;

            private int mLawType;

            private LayoutInflater mInflater;

            public LawParagraphListAdapter(Context c, int lawType) {
                mContext = c;
                mLawType = lawType;
                mInflater = (LayoutInflater)mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                init();
            }

            public void init() {
                mData.clear();
                mData.addAll(AccountantApplication.getDatabaseHelper(mContext).getLawParagraph(
                        mLawType));
                if (mData.isEmpty()) {
                    mNoDataHint.setVisibility(View.VISIBLE);
                } else {
                    mNoDataHint.setVisibility(View.GONE);
                }
            }

            public void notifyDataSetChanged() {
                init();
                super.notifyDataSetChanged();
            }

            @Override
            public int getCount() {
                return mData.size();
            }

            @Override
            public LawPara getItem(int arg0) {
                return mData.get(arg0);
            }

            @Override
            public long getItemId(int arg0) {
                return arg0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder = null;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(R.layout.law_paragraph_dialog_row, null);
                    holder.mParagraph = (TextView)convertView
                            .findViewById(R.id.law_paragraph_content);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder)convertView.getTag();
                }
                final LawPara para = getItem(position);
                holder.mParagraph.setText(LawPara.generateReadableString(para));
                return convertView;
            }

            class ViewHolder {
                TextView mParagraph;
            }
        }
    }
}
