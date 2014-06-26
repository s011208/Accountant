
package com.bj4.yhh.accountant.service;

import java.util.ArrayList;

import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ParseService extends Service implements GovLawParser.ResultCallback {
    public static final String PARSE_ALL = "parse_all";

    public static final String UPDATE_ALL = "update_all";

    public static final String TAG = "ParseService";

    private int mLoaderCount = 0;

    private boolean mHasFailed = false;

    private Handler mHandler = new Handler();

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                if (data.getBoolean(PARSE_ALL)) {
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_INSERT, this)).start();
                    ++mLoaderCount;
                    Toast.makeText(this, "Start to generate data -- insert all", Toast.LENGTH_LONG)
                            .show();
                } else if (data.getBoolean(UPDATE_ALL)) {
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_UPDATE, this)).start();
                    ++mLoaderCount;
                    Toast.makeText(this, "Start to check update -- update all", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                    Toast.makeText(getApplicationContext(),
                            mHasFailed ? "Internet not stable" : "Retrieve data success",
                            Toast.LENGTH_LONG).show();
                    mHasFailed = false;
                }
            });
        }
    }
}
