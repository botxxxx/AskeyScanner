package com.askey.safe;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;
import static com.askey.safe.MainActivity.getListOfApplications;
import static com.askey.safe.Utils.getSDPath;

public class SilentUninstallApkAsyncTask extends AsyncTask<String, Void, Boolean> {
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static final String ACTION_UNINSTALL_RESULT = "com.askey.ACTION_UNINSTALL_RESULT";

    public SilentUninstallApkAsyncTask(Context context) {
        mContext = context;
    }

    protected Boolean doInBackground(String... params) {
        if (params != null && params.length > 0) {
            String packageName = params[0];
            PackageInstaller packageInstaller = mContext.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sessionParams =
                    new PackageInstaller.SessionParams(
                            PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            sessionParams.setAppPackageName(packageName);
            PackageInstaller.Session session = null;
            try {
                //根据 sessionParams 创建 Session
                int sessionId = packageInstaller.createSession(sessionParams);
                session = packageInstaller.openSession(sessionId);
                try {
                    Intent intent = new Intent(ACTION_UNINSTALL_RESULT);
                    intent.setComponent(new ComponentName(mContext.getPackageName(),
                            SilentUninstallApkAsyncTask.UninstallResultBroadcastReceiver.class.getName()));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                            2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    packageInstaller.uninstall(packageName, pendingIntent.getIntentSender());
                    Log.d(TAG, "starting uninstall apk");
                    return true;
                } catch (Exception e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    Toast.makeText(mContext, "uninstall is failed!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d(TAG, Log.getStackTraceString(e));
                Toast.makeText(mContext, "uninstall is failed!", Toast.LENGTH_SHORT).show();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
        return false;
    }

    protected void onPostExecute(Boolean success) {
        if (!success)
            Toast.makeText(mContext, "background uninstall is failed!", Toast.LENGTH_SHORT).show();
    }

    public static class UninstallResultBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UNINSTALL_RESULT.equals(intent.getAction())) {
                getListOfApplications(mContext);
            }
        }
    }
}