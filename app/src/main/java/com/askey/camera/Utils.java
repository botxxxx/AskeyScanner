package com.askey.camera;

import android.annotation.SuppressLint;
import java.util.Calendar;

public class Utils {


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
        String path = "/storage/emulated/0/DCIM/";
        return path;
    }

}
