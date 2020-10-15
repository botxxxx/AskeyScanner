package com.askey.safe;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import static com.askey.safe.MainActivity.read;
import static com.askey.safe.MainActivity.updateUI;
import static com.askey.safe.Utils.getSDPath;

public class DownloadApkAsyncTask extends AsyncTask<apkManager, String, String> {
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    private ProgressDialog pDialog;
    private String point = "";
    private String apkName;

    public DownloadApkAsyncTask(Context context) {
        mContext = context;
    }

    @SuppressLint("AuthLeak")
    private static final String SMB_SERVER = "smb://hongren_su:id40123@10.1.96.111/";

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    protected void onPreExecute() {
        super.onPreExecute();
//        pDialog = new ProgressDialog(mContext);
//        pDialog.setMessage("Downloading file. Please wait...");
//        pDialog.setIndeterminate(false);
//        pDialog.setMax(100);
//        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        pDialog.setCancelable(true);
//        pDialog.show();
    }

    /**
     * Downloading file in background thread
     */
    protected String doInBackground(apkManager... apk) {
        File file = null;
        int count;
        InputStream input = null;
        OutputStream output = null;
        try {
            apkName = apk[0].apkName;
            if (point.equals("")) {
                point = read.size() + "";
                read.add(new apkItem("Download:" + apkName, true));
            }
            SmbFile smbfile = new SmbFile(SMB_SERVER + apk[0].apkUrl + apk[0].apkName);
            file = new File(getSDPath() + apk[0].apkName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int lenghtOfFile = smbfile.getContentLength();
            // download the file
            input = new BufferedInputStream(new SmbFileInputStream(smbfile), 8192);
            output = new BufferedOutputStream(new FileOutputStream(file.getPath()));
            byte[] data = new byte[1024];
            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int) (total * 100) / lenghtOfFile + "");

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error ", e.getMessage());
            file.delete();
            return "";
        } finally {
            // closing streams
            try {
                if (output != null)
                    output.close();
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getName();
    }

    /**
     * Updating progress bar
     */

    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0) {
            int progress = Integer.parseInt(values[0]);
            read.set(Integer.parseInt(point), new apkItem("Download:" + apkName, progress));
            updateUI(mContext);
            Log.e("Download", apkName + " " + progress + "%");
        }
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    protected void onPostExecute(String progress) {
        if (progress.equals(""))
            Log.e("Download", apkName + " filed");
        // dismiss the dialog after the file was downloaded

        read.set(Integer.parseInt(point), new apkItem(apkName + (apkName.equals(progress) ? " completed" : " filed")));
        updateUI(mContext);
//        Toast.makeText(mContext, "apk:"+file_url, Toast.LENGTH_SHORT).show();
    }
}