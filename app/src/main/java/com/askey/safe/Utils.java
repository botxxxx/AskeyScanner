package com.askey.safe;

import android.annotation.SuppressLint;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static com.askey.safe.MainActivity.SD_Mode;


public class Utils {

    public static final apkManager mainAPK =
            new apkManager("com.askey.safe", "");

    public static final apkManager[] apkList = new apkManager[]{
            /** must be used packageName.apk or packageName_version.apk */
            new apkManager("com.askey.record_v1.7.7.apk", "TempData/Jake_su/apps/"),
            new apkManager("com.askey.bit_v1.0.6.apk", "TempData/Jake_su/apps/"),
            new apkManager("com.luutinhit.assistivetouch.apk", "TempData/Jake_su/apps/"),
            new apkManager("com.askey.sensors_v1.0.2.apk", "TempData/Jake_su/apps/")
    };
    public static final String testFile = "testFile.ini";

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
        return "/storage/emulated/0/";
    }

    public static String getSDPath() {
        String path = "";
        if (SD_Mode) {
            try {
                long start = (System.currentTimeMillis() / 1000) % 60;
                long end = start + 10;
                Runtime run = Runtime.getRuntime();
                String cmd = "ls /storage";
                Process pr = run.exec(cmd);
                InputStreamReader input = new InputStreamReader(pr.getInputStream());
                BufferedReader buf = new BufferedReader(input);
                String line;
                while ((line = buf.readLine()) != null) {
                    if (!line.equals("self") && !line.equals("emulated") && !line.equals("enterprise") && !line.contains("sdcard")) {
                        path = "/storage/" + line + "/";
                        break;
                    }
                    if ((System.currentTimeMillis() / 1000) % 60 > end) {
                        break;
                    }
                }
                buf.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            path = getPath();
        }
        return path;
    }

    public static String readConfigFile(File file) {
        String tmp = "";
        try {
            byte[] buffer = new byte[100];
            int length;
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while ((length = bis.read(buffer)) != -1) {
                bytes.write(buffer, 0, length);
            }
            tmp += bytes.toString();
            bytes.close();
            bis.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return tmp;
        }
        return tmp;
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        } catch (MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }
}
