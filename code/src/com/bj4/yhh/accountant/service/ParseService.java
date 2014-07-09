
package com.bj4.yhh.accountant.service;

import java.util.HashMap;
import java.util.Iterator;

import com.bj4.yhh.accountant.parser.GovLawParser;
import com.bj4.yhh.accountant.utilities.GA;
import com.bj4.yhh.accountant.utilities.ToastHelper;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ParseService extends Service implements GovLawParser.ResultCallback {
    public static final String PARSE_ALL = "parse_all";

    public static final String UPDATE_ALL = "update_all";

    public static final String TAG = "ParseService";

    private int mLoaderCount = 0;

    private boolean mHasFailed = false;

    private Handler mHandler = new Handler();

    private HashMap<Integer, Integer> mProgressMap = new HashMap<Integer, Integer>();

    private int mCurrentLoadingProgress = 0;

    private long mLoadingTimer = 0;

    private final RemoteCallbackList<ParseServiceCallback> mCallbacks = new RemoteCallbackList<ParseServiceCallback>();

    private ParseServiceBinder.Stub mBinder = new ParseServiceBinder.Stub() {

        @Override
        public void unregisterCallback(ParseServiceCallback cb) throws RemoteException {
            if (cb != null)
                mCallbacks.unregister(cb);
        }

        @Override
        public void registerCallback(ParseServiceCallback cb) throws RemoteException {
            if (cb != null)
                mCallbacks.register(cb);
        }
    };

    private void startLoading() {
        mLoadingTimer = System.currentTimeMillis();
        mCurrentLoadingProgress = 0;
        int i = mCallbacks.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                mCallbacks.getBroadcastItem(i).startLoading();
            } catch (RemoteException e) {
            }
        }
        mCallbacks.finishBroadcast();
    }

    private int calculateLoadingProgress() {
        Iterator<Integer> valueIter = mProgressMap.values().iterator();
        int totalProgress = 0;
        while (valueIter.hasNext()) {
            totalProgress += valueIter.next();
        }
        return totalProgress / mProgressMap.size();
    }

    synchronized void updateLoadingProgress() {
        int loadingProgress = calculateLoadingProgress();
        if (loadingProgress != mCurrentLoadingProgress) {
            mCurrentLoadingProgress = loadingProgress;
            int i = mCallbacks.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    mCallbacks.getBroadcastItem(i).loadingProcess(mCurrentLoadingProgress);
                } catch (RemoteException e) {
                }
            }
            mCallbacks.finishBroadcast();
        }
    }

    void parseDone() {
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).loadingDone();
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
        mLoadingTimer = System.currentTimeMillis() - mLoadingTimer;
        if (mHasFailed == false) {
            GA.sendTiming(getApplicationContext(), GA.CATEGORY.CATEGORY_PARSE_DATA_TIME,
                    mLoadingTimer, null, null);
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (mLoaderCount != 0) {
            ToastHelper.makeToast(getApplicationContext(),
                    ToastHelper.TOAST_TYPE_WAITING_FOR_PREVIOUS_PARSING).show();
            return Service.START_NOT_STICKY;
        }
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                startLoading();
                if (data.getBoolean(PARSE_ALL)) {
                    mProgressMap.clear();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_COMPANY, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_LAND, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_TAX_COLLECTION, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_INCOME_TAX,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_INCOME_TAX, 0);
                    ++mLoaderCount;
                    ToastHelper.makeToast(getApplicationContext(),
                            ToastHelper.TOAST_TYPE_START_LOAD).show();
                } else if (data.getBoolean(UPDATE_ALL)) {
                    mProgressMap.clear();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_COMPANY, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_LAND, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_TAX_COLLECTION, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE, 0);
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_INCOME_TAX,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    mProgressMap.put(GovLawParser.PARSE_TYPE_INCOME_TAX, 0);
                    ++mLoaderCount;
                    ToastHelper.makeToast(getApplicationContext(),
                            ToastHelper.TOAST_TYPE_START_UPDATE).show();
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public synchronized void parseDone(int result, Exception e) {
        if (result == GovLawParser.RESULT_FAIL)
            mHasFailed = true;
        --mLoaderCount;
        if (mLoaderCount == 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHasFailed) {
                        ToastHelper.makeToast(getApplicationContext(),
                                ToastHelper.TOAST_TYPE_UPDATE_RESULT_FAIL).show();
                    } else {
                        ToastHelper.makeToast(getApplicationContext(),
                                ToastHelper.TOAST_TYPE_UPDATE_RESULT_OK).show();
                    }
                    parseDone();
                    mHasFailed = false;
                }
            });
            System.gc();
        }
    }

    @Override
    public void loadingProgress(int type, int progress) {
        mProgressMap.put(type, progress);
        updateLoadingProgress();
    }
}
