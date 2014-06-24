
package com.bj4.yhh.accountant;

import com.bj4.yhh.accountant.database.DatabaseHelper;

import android.app.Application;
import android.content.Context;

public class AccountantApplication extends Application {

    private static DatabaseHelper sDatabaseHelper;

    public synchronized static final DatabaseHelper getDatabaseHelper(Context context) {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return sDatabaseHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
