
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.dialogs.EnlargeOverViewContentDialog;
import com.bj4.yhh.accountant.parser.GovLawParser;
import com.bj4.yhh.accountant.utilities.MagicFuzzy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private EditText mSearchContent;

    private String mSearchingText = "";

    private View mOverViewShadow;

    // private ImageButton mNextSearching, mPreviousSearching;
    //
    // private int mCurrentSearchingPosition = 0;

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
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        mDatabaseHelper.addCallback(this);
    }

    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.removeCallback(this);
    }

    private void init() {
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (ViewSwitcher)mInflater.inflate(R.layout.over_view_fragment, null);
        mContentView.setInAnimation(mContext, R.anim.alpha_scale_switch_in);
        mContentView.setOutAnimation(mContext, R.anim.alpha_scale_switch_out);
        mLawListContainer = (LinearLayout)mContentView
                .findViewById(R.id.overview_law_list_container);
        initLawList();
        mLawContent = (ListView)mContentView.findViewById(R.id.over_view_law_content);
        mSearchContent = (EditText)mContentView.findViewById(R.id.search_content);
        mOverViewShadow = mContentView.findViewById(R.id.over_view_shadow);
        // mNextSearching =
        // (ImageButton)mContentView.findViewById(R.id.search_next);
        // mPreviousSearching =
        // (ImageButton)mContentView.findViewById(R.id.search_previous);
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
            SpannableString msp = new SpannableString(attr.mContent);
            holder.mHasBeenSearched = getSpannableString(attr.mContent, msp, mSearchingText);
            holder.mContent.setText(msp);
            holder.mLine.setText(attr.mLine);
            if (mSearchingText.length() > 0) {
                if (holder.mHasBeenSearched) {
                    holder.mLine.setBackgroundColor(0xccff8f59);
                    // convertView.setVisibility(View.VISIBLE);
                } else if (MagicFuzzy.Magic(attr.mContent, mSearchingText, 0)) {
                    holder.mLine.setBackgroundColor(0xcc8cea00);
                    // convertView.setVisibility(View.VISIBLE);
                } else {
                    // convertView.setVisibility(View.GONE);
                    holder.mLine.setBackgroundColor(0xcc8080c0);
                }
            } else {
                // convertView.setVisibility(View.VISIBLE);
                holder.mLine.setBackgroundColor(0xcc8080c0);
            }
            return convertView;
        }

        class ViewHolder {
            TextView mLine;

            TextView mContent;

            boolean mHasBeenSearched = false;
        }

        public void notifyDataSetChanged() {
            initData();
            super.notifyDataSetChanged();
        }
    }

    private static boolean getSpannableString(String label, SpannableString msp,
            String searchingText) {
        int index = label.toLowerCase().indexOf(searchingText);
        if (index != -1) {
            msp.setSpan(new BackgroundColorSpan(0x66e1e100), index, index + searchingText.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return index != -1;
    }

    private ValueAnimator mListShadowAnimator;

    private void initContentListView() {
        if (mLawContent != null) {
            mLawContentAdapter = new LawContentAdapter();
            mLawContent.setAdapter(mLawContentAdapter);
            mListShadowAnimator = ValueAnimator.ofFloat(0, 1);
            mListShadowAnimator.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // TODO Auto-generated method stub
                    float v = (Float)animation.getAnimatedValue();
                    mOverViewShadow.setAlpha(v);
                }
            });
            mListShadowAnimator.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    // TODO Auto-generated method stub
                    mOverViewShadow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // TODO Auto-generated method stub

                }
            });
            mLawContent.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mListShadowAnimator.reverse();
                    } else {
                        mListShadowAnimator.start();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                        int totalItemCount) {
                    // TODO Auto-generated method stub

                }
            });
            mLawContent.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new EnlargeOverViewContentDialog(mLawContentAdapter.getItem(position),
                            mDisplayContentType).show(getFragmentManager(), null);
                }
            });
            mSearchContent.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub
                    mSearchingText = s.toString();
                    mLawContentAdapter.notifyDataSetChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub

                }
            });
            // mCurrentSearchingPosition
            // mNextSearching.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // // TODO Auto-generated method stub
            //
            // }
            // });
        }
    }

    private void initLawList() {
        if (mLawListContainer != null) {
            mLawListContainer.removeAllViews();
            ArrayList<Integer> types = mDatabaseHelper.getAllLawTypes();
            for (final int t : types) {
                Button btn = new Button(mContext);
                btn.setBackgroundResource(R.drawable.general_btn_bg);
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
