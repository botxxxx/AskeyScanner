package com.askey.safe;

import android.content.Context;
import android.content.pm.ResolveInfo;

public class apkItem {
    String msg;
    ResolveInfo info;
    boolean download;
    int progress;

    apkItem(String msg) {
        this.msg = msg;
        this.info = null;
        this.download = false;
        this.progress = 0;
    }

    apkItem(String msg, boolean download) {
        this.msg = msg;
        this.info = null;
        this.download = download;
        this.progress = 0;
    }

    apkItem(String msg, int progress) {
        this.msg = msg;
        this.info = null;
        this.download = true;
        this.progress = progress;
    }

    apkItem(ResolveInfo info) {
        this.info = info;
        this.download = false;
        this.progress = 0;
    }

    public String getApkName(Context context) {
        if (info != null)
            return info.loadLabel(context.getPackageManager()).toString();
        else
            return msg;
    }
}
