
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.MainActivity;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class OverViewFragment extends Fragment implements DatabaseHelper.RefreshLawCallback {
    public static final int OVERVIEW_FRAGMENT_LAW_LIST = 0;

    public static final int OVERVIEW_FRAGMENT_LAW_CONTENT = 1;

    private int mCurrentDisplayChild = OVERVIEW_FRAGMENT_LAW_LIST;

    private Context mContext;

    private ViewSwitcher mContentView;

    private MainActivity mMainActivity;

    private LinearLayout mLawListContainer;

    private DatabaseHelper mDatabaseHelper;

    private ListView mLawContent;

    private int mDisplayContentType = -1;

    private LawContentAdapter mLawContentAdapter;

    private LayoutInflater mInflater;

    public OverViewFragment() {
    }

    public OverViewFragment(MainActivity activity) {
        mContext = activity;
        mMainActivity = activity;
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        init();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper.addCallback(this);
    }

    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.removeCallback(this);
    }

    private void init() {
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (ViewSwitcher)mInflater.inflate(R.layout.over_view_fragment, null);
        mLawListContainer = (LinearLayout)mContentView
                .findViewById(R.id.overview_law_list_container);
        initLawList();
        mLawContent = (ListView)mContentView.findViewById(R.id.over_view_law_content);
        initContentListView();
    }

    class LawContentAdapter extends BaseAdapter {
        private ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

        public LawContentAdapter() {
            initData();
        }

        private void initData() {
            mData = mDatabaseHelper.query(mDisplayContentType);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public LawAttrs getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.over_view_fragment_law_content_row, null);
                holder = new ViewHolder();
                holder.mContent = (TextView)convertView.findViewById(R.id.content);
                holder.mLine = (TextView)convertView.findViewById(R.id.line);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            LawAttrs attr = getItem(position);
            holder.mLine.setText(attr.mLine);
            holder.mContent.setText(attr.mContent);
            return convertView;
        }

        class ViewHolder {
            TextView mLine;

            TextView mContent;
        }

        public void notifyDataSetChanged() {
            initData();
            super.notifyDataSetChanged();
        }
    }

    private void initContentListView() {
        if (mLawContent != null) {
            mLawContentAdapter = new LawContentAdapter();
            mLawContent.setAdapter(mLawContentAdapter);
        }
    }

    private void initLawList() {
        if (mLawListContainer != null) {
            mLawListContainer.removeAllViews();
            ArrayList<Integer> types = mDatabaseHelper.getAllLawTypes();
            for (final int t : types) {
                Button btn = new Button(mContext);
                btn.setText(GovLawParser.getTypeTextResource(t));
                mLawListContainer.addView(btn);
                btn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mDisplayContentType = t;
                        mLawContentAdapter.notifyDataSetChanged();
                        mLawContent.setSelection(0);
                        setDisplayedChild(OVERVIEW_FRAGMENT_LAW_CONTENT);
                    }
                });
            }
        }
    }

    public void setDisplayedChild(int child) {
        if (mContentView != null) {
            mCurrentDisplayChild = child;
            mContentView.setDisplayedChild(child);
        }
    }

    public int getDisplayedChild() {
        return mCurrentDisplayChild;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mContentView;
    }

    @Override
    public void notifyDataChanged() {
        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initLawList();
                mLawContentAdapter.notifyDataSetChanged();
            }
        });
    }
}
