
package com.bj4.yhh.accountant.service;

import com.bj4.yhh.accountant.parser.GovLawParser;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ParseService extends Service {
    public static final String PARSE_ALL = "parse_all";

    public static final String UPDATE_ALL = "update_all";

    public static final String TAG = "ParseService";

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
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_INSERT)).start();
                    Toast.makeText(this, "Start to generate data -- insert all", Toast.LENGTH_LONG)
                            .show();
                } else if (data.getBoolean(UPDATE_ALL)) {
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TAX_COLLECTION,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this,
                            GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE,
                            GovLawParser.BEHAVIOR_UPDATE)).start();
                    Toast.makeText(this, "Start to check update -- update all", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
