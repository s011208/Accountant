
package com.bj4.yhh.accountant.utilities;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

public class Utilities {
    public static boolean isRamLargerThan1G(Context context) {
        boolean rtn = false;
        int totalMem = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                totalMem = getTotalRAMAPIAbove16(context);
            } else {
                totalMem = getTotalRAMBelowAPI16();
            }
            if (totalMem > 0) {
                rtn = totalMem / 1300000 > 0;
            }
        } catch (Exception e) {

        }
        return rtn;
    }

    public static int getTotalRAMAPIAbove16(Context context) {
        ActivityManager actManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        android.app.ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return (int)memInfo.totalMem;
    }

    public static int getTotalRAMBelowAPI16() {

        RandomAccessFile reader = null;
        String load = null;
        double totRam = 0;
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        }

        return (int)totRam;
    }
}
