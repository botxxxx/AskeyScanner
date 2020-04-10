package com.askey.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Scanner extends Activity {

    private int debug = 0;
    private String TAG = "Ready";
    private String XLS = "AskeyScan.csv";
    private EditText mView;

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

        Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String args = mView.getText().toString();
                if (!(args.equals((debug == 0) ? "Ready" : TAG))) {
                    String[] TAGs = args.split((debug == 0) ? "Ready" : TAG);
                    runOnUiThread(() -> {
                        mView.setText(TAGs.length == 1 ? TAGs[0] : TAG);
                        TAG = mView.getText().toString();
                        debug++;
                        addCVS(TAG + "\r\n");
                        ((TextView) findViewById(R.id.mCount)).setText(debug + "");
                    });
                }
            }
        };
        mView = findViewById(R.id.mView);
        mView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                mHandler.obtainMessage().sendToTarget();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void addCVS(String args) {
        try {

            File file = new File(Utils.getPath(), XLS);
            try {
                FileOutputStream fOut = new FileOutputStream(file, file.exists());
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
