/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.scriptmanager.activities.AboutActivity;
import com.smartpack.scriptmanager.utils.Billing;
import com.smartpack.scriptmanager.utils.Scripts;
import com.smartpack.scriptmanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */
public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();
    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        Scripts.mRecyclerView = findViewById(R.id.recycler_view);
        AppCompatImageButton mSettings = findViewById(R.id.settings_icon);
        FloatingActionButton mFab = findViewById(R.id.fab);
        AppCompatImageButton mDonate = findViewById(R.id.donate_icon);
        AppCompatImageButton mInfo = findViewById(R.id.info_icon);

        mSettings.setOnClickListener(v -> Utils.settingsMenu(mSettings, this));

        mFab.setOnClickListener(v -> {
            if (!Utils.checkWriteStoragePermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.permission_denied_write_storage));
                return;
            }
            Utils.fabMenu(mFab, this);
        });

        mDonate.setOnClickListener(v -> Billing.showDonateOption(this));

        mInfo.setOnClickListener(v -> {
            Intent aboutView = new Intent(this, AboutActivity.class);
            startActivity(aboutView);
        });

        Scripts.loadUI(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == 0) {
            Uri uri = data.getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (Utils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mPath = Environment.getExternalStorageDirectory().toString() + "/Download/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = Utils.getPath(file);
            }
            if (!Utils.getExtension(mPath).equals("sh")) {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.wrong_extension, ".sh"));
                return;
            }
            if (!Scripts.isScript(mPath)) {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.wrong_script, file.getName().replace(".sh", "")));
                return;
            }
            if (Utils.exist(Scripts.scriptExistsCheck(new File(mPath).getName()))) {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.script_exists, file.getName()));
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.select_question, new File(mPath).getName()))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        Scripts.importScript(mPath);
                        Scripts.reloadUI();
                    }).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Utils.rootAccess() && Utils.getBoolean("no_root_message", true, this)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.root_unavailable))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        Utils.saveBoolean("no_root_message", false, this);
                    }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }
}