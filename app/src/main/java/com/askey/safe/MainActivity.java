package com.askey.safe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.askey.safe.Utils.getSDPath;
import static com.askey.safe.Utils.mainAPK;
import static com.askey.safe.Utils.testFile;

public class MainActivity extends Activity {
    public static final boolean sync = false;
    public static final boolean SD_Mode = true;
    public static final String TAG = "com.askey.safe";
    public static final String NO_SD_CARD = "SD card is not available!";
    public static boolean success = false;
    public static ArrayList<String> apkPackage;
    public static ArrayList<apkItem> read;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            showPermission();
        } else {
            getStart();
        }
    }

    private boolean checkPermission() {
        int STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission(STORAGE);
    }

    private boolean permission(int mp) {
        return mp != PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23)
    @SuppressLint("NewApi")
    private void showPermission() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getStart();
            } else {
                showPermission();
            }
        }
    }

    private void getStart() {
        read = new ArrayList<>();
        if (!getSDPath().equals("")) {
            File file = new File(getSDPath(), testFile);
            try {
                if (!file.exists()) {
                    success = file.createNewFile();
                } else {
                    success = true;
                }
                boolean delete = file.delete();
                if (!delete) {
                    read.add(new apkItem("delete fail"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!success) {//NO_SD_CARD
            read.add(new apkItem(NO_SD_CARD));
        } else {
            //read.add("ready");
            getListOfApplications(this);
            apkPackage = new ArrayList<>();
            for (apkManager s : Utils.apkList) {
                apkPackage.add(getPackageName(s.apkName));
            }
        }
        updateUI(this);
        findViewById(R.id.btn_install).setOnClickListener((View v) -> {
            read.clear();
            updateUI(this);
            if (checkApkFile())
                checkInstall(this);
            else
                for (apkManager s : Utils.apkList)
                    new DownloadApkAsyncTask(this).execute(s);
        });
        findViewById(R.id.btn_remove).setOnClickListener((View v) -> {
            read.clear();
            updateUI(this);
            for (apkManager s : Utils.apkList) // Remove apk
                removeAPK(s);
            for (String s : apkPackage) // Uninstall application
                apkUninstall(s);
            apkUninstall(mainAPK.apkName); // Uninstall ManagerApplication
        });
    }

    public static boolean checkApkFile() {
        for (apkManager s : Utils.apkList)
            if (!new File(getSDPath() + s.apkName).exists()) {
                return false;
            }
        return true;
    }

    public static void checkInstall(Context context) {
        boolean installed = false;
        for (apkManager s : Utils.apkList)
            if (apkInstall(s.apkName, context))
                installed = true;
        if (!installed) {
            getListOfApplications(context);
            Toast.makeText(context, "You has installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAPK(apkManager apk) {
        File rm = new File(getSDPath(), apk.apkName);
        if (rm.exists()) {
            if (!rm.delete()) {
                Toast.makeText(this, "apk delete failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String getPackageName(String s) {
        String packageName = s.split("_")[0];
        if (s.split("_").length == 1)
            packageName = s.substring(0, s.length() - 4);
        return packageName;
    }

    public static void updateUI(Context context) {
        ((ListView) ((Activity) context).findViewById(R.id.listView)).setAdapter(new mListAdapter(context, read));
    }

    public static void getListOfApplications(Context context) {
        read.clear();
        for (ResolveInfo ap : getApplications(context))
            read.add(new apkItem(ap));/*application List*/
        updateUI(context);
    }

    public static ArrayList<ResolveInfo> getApplications(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        Collections.sort(pkgAppsList,
                (o1, o2) -> o1.activityInfo.loadLabel(context.getPackageManager()).toString().compareTo(
                        o2.activityInfo.loadLabel(context.getPackageManager()).toString()));
        return new ArrayList<>(pkgAppsList);
    }

    private void apkUninstall(String packageName) {
        Log.d(TAG, "apkUninstall, packageName = " + packageName);
        for (ResolveInfo ap : getApplications(this)) {
            if (ap.activityInfo.packageName.equals(packageName)) {
                Log.d(TAG, "apkUninstall, silent mode");
                new SilentUninstallApkAsyncTask(this).execute(packageName);
                break;
            }
        }
    }

    private static boolean apkInstall(String apkName, Context context) {
        boolean apkExists = false;
        String packageName = getPackageName(apkName);
        for (ResolveInfo ap : getApplications(context)) {
            if (ap.activityInfo.packageName.equals(packageName)) {
                apkExists = true;
                break;
            }
        }
        if (!apkExists) {
            String apkAbsolutePath = getSDPath() + apkName;
            Log.d(TAG, "apkInstall, path = " + apkAbsolutePath);
            if (!TextUtils.isEmpty(apkAbsolutePath)) {
                File apkFile = new File(apkAbsolutePath);
                if (apkFile.exists()) {
                    Log.d(TAG, "apkInstall, silent mode");
                    new SilentInstallApkAsyncTask(context).execute(apkFile);
                } else {
                    read.clear();
                    read.add(new apkItem("The APK file does not exist"));
                    Toast.makeText(context, "The APK file does not exist", Toast.LENGTH_SHORT).show();
                    updateUI(context);
                }
            }
        }
        return !apkExists;
    }
}
