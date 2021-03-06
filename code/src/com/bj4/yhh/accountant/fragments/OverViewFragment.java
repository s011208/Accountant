
package com.bj4.yhh.accountant.fragments;

import java.util.ArrayList;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.SettingManager;
import com.bj4.yhh.accountant.activities.MainActivity;
import com.bj4.yhh.accountant.database.DatabaseHelper;
import com.bj4.yhh.accountant.dialogs.ApiUnder16DialogHelper;
import com.bj4.yhh.accountant.dialogs.EnlargeOverViewContentDialog;
import com.bj4.yhh.accountant.dialogs.LawParagraphDialog;
import com.bj4.yhh.accountant.parser.GovLawParser;
import com.bj4.yhh.accountant.utilities.GA;
import com.bj4.yhh.accountant.utilities.MagicFuzzy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class OverViewFragment extends BaseFragment implements DatabaseHelper.RefreshLawCallback {
    public static final int OVERVIEW_FRAGMENT_LAW_LIST = 0;

    public static final int OVERVIEW_FRAGMENT_LAW_CONTENT = 1;

    private int mCurrentDisplayChild = OVERVIEW_FRAGMENT_LAW_LIST;

    private static final int SORT_TYPE_ORDER = 0;

    private static final int SORT_TYPE_WRONG_COUNT = 1;

    private int mSortType = SORT_TYPE_ORDER;

    private Context mContext;

    private ViewSwitcher mContentView;

    private MainActivity mMainActivity;

    private ListView mLawList;

    private LawListAdapter mLawListAdapter;

    private DatabaseHelper mDatabaseHelper;

    private ListView mLawContent;

    private static int sDisplayContentType = -1;

    private LawContentAdapter mLawContentAdapter;

    private LayoutInflater mInflater;

    private EditText mSearchContent;

    private Spinner mSorter;

    private String mSearchingText = "";

    private View mOverViewShadow;

    private TextView mNoDataHint;

    private ImageView mLawParagraph;

    public OverViewFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
            mMainActivity = (MainActivity)getActivity();
            mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
            init();
        }
        mDatabaseHelper = AccountantApplication.getDatabaseHelper(mContext);
        mDatabaseHelper.addCallback(this);
        themeColorChanged(SettingManager.getInstance(mContext).getThemeColor());
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
        mNoDataHint = (TextView)mContentView.findViewById(R.id.overview_law_list_no_data_hint);
        mLawList = (ListView)mContentView.findViewById(R.id.overview_law_list);
        initLawList();
        mLawContent = (ListView)mContentView.findViewById(R.id.over_view_law_content);
        mSearchContent = (EditText)mContentView.findViewById(R.id.search_content);
        mOverViewShadow = mContentView.findViewById(R.id.over_view_shadow);
        mSorter = (Spinner)mContentView.findViewById(R.id.over_view_sorter);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, mContext.getResources()
                        .getStringArray(R.array.over_view_list_sorter));
        mSorter.setAdapter(sortAdapter);
        mSorter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mSortType = arg2;
                mLawContentAdapter.notifyDataSetChanged();
                GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_OVER_VIEW_FRAGMENT,
                        GA.ACTIONS.ACTIONS_OVERVIEW_SORT, null, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        mLawParagraph = (ImageView)mContentView.findViewById(R.id.law_paragh);
        mLawParagraph.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_OVER_VIEW_FRAGMENT,
                        GA.ACTIONS.ACTIONS_SHOW_LAW_PARAGRAPH, null, null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    new LawParagraphDialog(sDisplayContentType, new LawParagraphDialog.Callback() {

                        @Override
                        public void onItemSelected(String content) {
                            content = content.substring(content.lastIndexOf("��") + 1);
                            if (mLawContentAdapter != null && mLawContent != null) {
                                for (int i = 0; i < mLawContentAdapter.getCount(); i++) {
                                    if (mLawContentAdapter.getItem(i).mLine.replace("��", "")
                                            .replace("��", "").trim().equals(content)) {
                                        mLawContent.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }).show(getFragmentManager(), null);
                } else {
                    ApiUnder16DialogHelper.LawParagraphDialog.getNewInstanceDialog(mContext,
                            sDisplayContentType, new LawParagraphDialog.Callback() {

                                @Override
                                public void onItemSelected(String content) {
                                    content = content.substring(content.lastIndexOf("��") + 1);
                                    if (mLawContentAdapter != null && mLawContent != null) {
                                        for (int i = 0; i < mLawContentAdapter.getCount(); i++) {
                                            if (mLawContentAdapter.getItem(i).mLine
                                                    .replace("��", "").replace("��", "").trim()
                                                    .equals(content)) {
                                                mLawContent.setSelection(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }).show();
                }
            }
        });
        initContentListView();
    }

    class LawContentAdapter extends BaseAdapter {
        private ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

        public LawContentAdapter() {
            initData();
        }

        private void initData() {
            if (mSortType == SORT_TYPE_ORDER) {
                mData = mDatabaseHelper.getPlanDataFromLawTable(sDisplayContentType, false, false);
            } else if (mSortType == SORT_TYPE_WRONG_COUNT) {
                mData = mDatabaseHelper.getPlanDataFromLawTable(sDisplayContentType, false, true);
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public LawAttrs getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
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
                } else if (MagicFuzzy.Magic(attr.mContent, mSearchingText, 0)) {
                    holder.mLine.setBackgroundColor(0xcc8cea00);
                } else {
                    holder.mLine.setBackgroundColor(0xcc8080c0);
                }
            } else {
                holder.mLine.setBackgroundColor(0xcc8080c0);
            }
            if (!mEnableHighPerformance) {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.list_alpha);
                convertView.startAnimation(animation);
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
                    float v = (Float)animation.getAnimatedValue();
                    mOverViewShadow.setAlpha(v);
                }
            });
            mListShadowAnimator.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mOverViewShadow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
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
                }
            });
            mLawContent.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GA.sendEvents(mContext, GA.CATEGORY.CATEGORY_OVER_VIEW_FRAGMENT,
                            GA.ACTIONS.ACTIONS_SHOW_LARGE_CONTENT, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        new EnlargeOverViewContentDialog(mLawContentAdapter.getItem(position),
                                sDisplayContentType).show(getFragmentManager(), null);
                    } else {
                        ApiUnder16DialogHelper.EnlargeOverViewContentDialog
                                .getNewInstanceDialog(mContext,
                                        mLawContentAdapter.getItem(position), sDisplayContentType)
                                .show();
                    }

                }
            });
            mSearchContent.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mSearchingText = s.toString();
                    mLawContentAdapter.notifyDataSetChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void initLawList() {
        if (mLawList != null) {
            mLawListAdapter = new LawListAdapter();
            mLawList.setAdapter(mLawListAdapter);
            mLawList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    sDisplayContentType = mLawListAdapter.getItem(position);
                    mLawContentAdapter.notifyDataSetChanged();
                    mLawContent.setSelection(0);
                    setDisplayedChild(OVERVIEW_FRAGMENT_LAW_CONTENT);
                }
            });
        }
    }

    private class LawListAdapter extends BaseAdapter {
        private ArrayList<Integer> mTypeData;

        public LawListAdapter() {
            init();
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            init();
        }

        private void init() {
            mTypeData = mDatabaseHelper.getAllLawTypes();
            if (mNoDataHint != null) {
                if (mTypeData.isEmpty()) {
                    mNoDataHint.setVisibility(View.VISIBLE);
                } else {
                    mNoDataHint.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getCount() {
            return mTypeData.size();
        }

        @Override
        public Integer getItem(int position) {
            return mTypeData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.over_view_fragment_law_list_row, null);
                holder = new ViewHolder();
                holder.mLawText = (TextView)convertView
                        .findViewById(R.id.overview_law_list_type_txt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            int type = getItem(position);
            holder.mLawText.setText(GovLawParser.getTypeTextResource(type));
            return convertView;
        }

        private class ViewHolder {
            TextView mLawText;
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
    public void onDestroyView() {
        super.onDestroyView();
        if (mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                ((ViewGroup)parent).removeView(mContentView);
            }
        }
    }

    @Override
    public void notifyDataChanged() {
        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mLawListAdapter.notifyDataSetChanged();
                mLawContentAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void themeColorChanged(int newColor) {
        // TODO Auto-generated method stub

    }
}
