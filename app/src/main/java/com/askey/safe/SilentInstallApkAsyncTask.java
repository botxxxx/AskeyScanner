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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;
import static com.askey.safe.MainActivity.getListOfApplications;

public class SilentInstallApkAsyncTask extends AsyncTask<File, Void, Boolean> {
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static final String ACTION_INSTALL_RESULT = "com.askey.ACTION_INSTALL_RESULT";

    public SilentInstallApkAsyncTask(Context context) {
        mContext = context;
    }

    protected Boolean doInBackground(File... params) {
        if (params != null && params.length > 0) {
            File apkFile = params[0];
            if (apkFile != null && apkFile.exists()) {
                PackageInstaller packageInstaller = mContext.getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams sessionParams =
                        new PackageInstaller.SessionParams(
                                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                sessionParams.setSize(apkFile.length());
                PackageInstaller.Session session = null;
                try {
                    //根据 sessionParams 创建 Session
                    int sessionId = packageInstaller.createSession(sessionParams);
                    session = packageInstaller.openSession(sessionId);
                    //将 apk 文件输入 session
                    if (readApkFileToSession(session, apkFile)) {
                        //提交 session，并且设置回调
                        try {
                            Intent intent = new Intent(ACTION_INSTALL_RESULT);
                            intent.setComponent(new ComponentName(mContext.getPackageName(),
                                    InstallResultBroadcastReceiver.class.getName()));
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                                    1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            session.commit(pendingIntent.getIntentSender());
                            Log.d(TAG, "starting install apk");
                            return true;
                        } catch (Exception e) {
                            Log.d(TAG, Log.getStackTraceString(e));
                            Toast.makeText(mContext, "install is failed!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "read apk file is failed!");
                        Toast.makeText(mContext, "readApkFileToSession is failed!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    Toast.makeText(mContext, "createSession is failed!", Toast.LENGTH_SHORT).show();
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }
        }
        return false;
    }

    protected void onPostExecute(Boolean success) {
        if (!success)
            Toast.makeText(mContext, "background install is failed!", Toast.LENGTH_SHORT).show();
    }

    private boolean readApkFileToSession(PackageInstaller.Session session, File apkFile) {
        OutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        try {
            outputStream = session.openWrite(apkFile.getName(), 0, apkFile.length());
            fileInputStream = new FileInputStream(apkFile);
            int read;
            byte[] buffer = new byte[1024 * 1024];
            while ((read = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            session.fsync(outputStream);
            fileInputStream.close();
            return true;
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Toast.makeText(mContext, "readApkFileToSession is failed!", Toast.LENGTH_SHORT).show();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    Toast.makeText(mContext, "fileInputStream is failed!", Toast.LENGTH_SHORT).show();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    Toast.makeText(mContext, "outputStream is failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return false;
    }

    public static class InstallResultBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (ACTION_INSTALL_RESULT.equals(intent.getAction())) {
                int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                try {
                    Log.d(TAG, status == PackageInstaller.STATUS_SUCCESS ? "install complete!" : "install failed!");
                    if (status != PackageInstaller.STATUS_SUCCESS)
                        Toast.makeText(mContext, "install failed!", Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                    Toast.makeText(mContext, "install failed!", Toast.LENGTH_SHORT).show();
                } finally {
                    getListOfApplications(mContext);
                }
            }
        }
    }
}