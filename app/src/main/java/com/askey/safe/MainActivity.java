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
import static com.askey.safe.Utils.testFile;

public class MainActivity extends Activity {
    public static final boolean SD_Mode = true;
    public static final String TAG = "com.askey.safe";
    public static final String[] apkName = new String[]{
            /** must be used packageName.apk or packageName_version.apk */
            "com.askey.record_v1.7.6.apk",
            "com.askey.bit_v1.0.5.apk",
            "com.luutinhit.assistivetouch.apk",
            "com.askey.sensors_v1.0.2.apk"
    };
    public static final  String[] apkUrl = new String[]{
            "https://drive.google.com/uc?id=1_0X1tVE5kP7aVvwAmyURC1KdfzFvU0PI&export=download",
            "https://drive.google.com/uc?id=1NGykpRiTL-LpkBOyvsH7qoktNPfLBBpK&export=download",
            "https://drive.google.com/uc?id=1-pno6yyNxIu_u-rHyHdTSfkxyU0NT-r8&export=download",
            "https://drive.google.com/uc?id=1OKcNZFqcnoQKNt9YNvnWc_Sjp2VlNMvn&export=download"
    };
    public static final String NO_SD_CARD = "SD card is not available!";
    public static boolean success = false;
    public static ArrayList<String> read, apkPackage;


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
                    read.add("delete fail");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!success) {
            read.add(NO_SD_CARD);
        } else {
            //read.add("ready");
            getListOfApplications(this);
            apkPackage = new ArrayList<>();
            for (String s : apkName) {
                apkPackage.add(getPackageName(s));
            }
        }
        updateUI(this);
        findViewById(R.id.btn_install).setOnClickListener((View v) -> {
            read.clear();
            updateUI(this);
            boolean installed = false;
            for (String s : apkName)
                if (apkInstall(s))
                    installed = true;
            if (!installed) {
                getListOfApplications(this);
                Toast.makeText(this, "You has installed", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_remove).setOnClickListener((View v) -> {
            read.clear();
            updateUI(this);
            for (String s : apkPackage)
                apkUninstall(s);
        });
    }

    private String getPackageName(String s) {
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
            read.add(ap.loadLabel(context.getPackageManager()).toString()+
                    "("+ap.activityInfo.packageName+")");/*application List*/
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

    private boolean apkInstall(String apkName) {
        boolean apkExists = false;
        String packageName = getPackageName(apkName);
        for (ResolveInfo ap : getApplications(this)) {
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
                    new SilentInstallApkAsyncTask(this).execute(apkFile);
                } else {
                    read.clear();
                    read.add("The APK file does not exist");
                    Toast.makeText(getApplicationContext(), "The APK file does not exist", Toast.LENGTH_SHORT).show();
                    updateUI(this);
                }
            }
        }
        return !apkExists;
    }
}
