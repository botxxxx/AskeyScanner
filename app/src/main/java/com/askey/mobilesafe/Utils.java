package com.askey.mobilesafe;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;


public class Utils {

    public static final String EXTRA_CFS_COPY = "copyFileServer.copy";
    public static final String EXTRA_CFS_PATH = "copyFileServer.path";

    @SuppressLint("DefaultLocale")
    public static String getCalendarTime() {
        String y, m, d, h, i, s;
        Calendar calendar = Calendar.getInstance();
        y = String.format("%02d", calendar.get(Calendar.YEAR));
        m = String.format("%02d", (calendar.get(Calendar.MONTH) - 1));
        d = String.format("%02d", calendar.get(Calendar.DATE));
        h = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        i = String.format("%02d", calendar.get(Calendar.MINUTE));
        s = String.format("%02d", calendar.get(Calendar.SECOND));

        return "" + y + m + d + h + i + s;
    }

    public static String getPath() {
        String path = "/storage/emulated/0/";
        return path;
    }

    public static String getSDPath() {
        String path = "";
        try {
            long start = System.currentTimeMillis();
            long end = start + 5000;
            Runtime run = Runtime.getRuntime();
            String cmd = "ls /storage";
            Process pr = run.exec(cmd);
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = buf.readLine()) != null) {
                if (!line.equals("self") && !line.equals("emulated") && !line.equals("enterprise") && !line.contains("sdcard") ) {
                    path = "/storage/" + line + "/";
                    break;
                }
                if (System.currentTimeMillis() > end) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return path;
        }
        return path;
    }
}
