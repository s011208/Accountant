
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

    public static final String TAG = "QQQQ";

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                if (data.getBoolean(PARSE_ALL)) {
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_COMPANY)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_LAND)).start();
                    new Thread(new GovLawParser(this, GovLawParser.PARSE_TYPE_TEXT_COLLECTION))
                            .start();
                    Toast.makeText(this, "Start to parse", Toast.LENGTH_LONG).show();
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
