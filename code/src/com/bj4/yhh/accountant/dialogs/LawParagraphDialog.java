
package com.bj4.yhh.accountant.dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawPara;
import com.bj4.yhh.accountant.R;

public class LawParagraphDialog extends BaseDialog {
    public interface Callback {
        public void onItemSelected(String content);
    }

    private int mLawType = 0;

    private Callback mCallback;

    private TextView mNoDataHint;

    public LawParagraphDialog(int type, Callback c) {
        mLawType = type;
        mCallback = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.law_paragraph_dialog, null);
        ListView list = (ListView)v.findViewById(R.id.law_paragraph_list);
        mNoDataHint = (TextView)v.findViewById(R.id.law_paragraph_no_data_hint);
        final LawParagraphListAdapter adapter = new LawParagraphListAdapter(context, mLawType);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (mCallback != null) {
                    mCallback.onItemSelected(adapter.getItem(arg2).mTitle);
                    dismiss();
                }
            }
        });
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setTitle(R.string.law_paragraph_dialog_title).setCancelable(true).setView(v);
        return builder.create();
    }

    class LawParagraphListAdapter extends BaseAdapter {

        private final ArrayList<LawPara> mData = new ArrayList<LawPara>();

        private Context mContext;

        private int mLawType;

        private LayoutInflater mInflater;

        public LawParagraphListAdapter(Context c, int lawType) {
            mContext = c;
            mLawType = lawType;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            init();
        }

        public void init() {
            mData.clear();
            mData.addAll(AccountantApplication.getDatabaseHelper(mContext)
                    .getLawParagraph(mLawType));
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
                holder.mParagraph = (TextView)convertView.findViewById(R.id.law_paragraph_content);
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
