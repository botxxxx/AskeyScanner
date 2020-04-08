package com.askey.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private String XLS = Utils.getCalendarTime() + ".csv";
    private ArrayList<String> reader;

    @Override
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
        // We don't have permission so prompt the user
        List<String> permissions = new ArrayList();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可授權
                    getStart();
                } else {
                    // 沒有權限
                    showPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getStart() {

        reader = new ArrayList();
        reader.add("uhAHRK5nXBHQ8yBK");
        reader.add("mC5DEWcHVgfbea5C");
        reader.add("FwHsaKTb5KgSTgzf");
        reader.add("C4VZgySR7QDwCGy6");
        reader.add("VgGBH8CxSZ8MGuK8");

        for (String msg : reader) {
            addCVS(msg+ "\r\n");
        }
    }

    private void addCVS(String args) {
        try {

            File file = new File(Utils.getPath(), XLS);
            try {
                FileOutputStream fOut = new FileOutputStream(file,file.exists());
                fOut.write(args.getBytes());
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
