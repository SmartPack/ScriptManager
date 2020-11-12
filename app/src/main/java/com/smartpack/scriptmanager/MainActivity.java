/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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
import android.view.Menu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.scriptmanager.utils.NoRootActivity;
import com.smartpack.scriptmanager.utils.RecycleViewAdapter;
import com.smartpack.scriptmanager.utils.Scripts;
import com.smartpack.scriptmanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private FloatingActionButton mFab;
    private Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        // Request Root Access
        if (!Utils.rootAccess()) {
            Intent noRoot = new Intent(this, NoRootActivity.class);
            startActivity(noRoot);
            finish();
            return;
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        Utils.mSettings = findViewById(R.id.settings_icon);
        mFab = findViewById(R.id.fab);

        Utils.mSettings.setOnClickListener(v -> {
            Utils.settingsMenu(this);
        });

        mFab.setOnClickListener(v -> {
            if (!Utils.checkWriteStoragePermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mRecyclerView, getString(R.string.permission_denied_write_storage));
                return;
            }
            showOptions();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.getSpanCount(this)));
        mRecyclerView.setAdapter(new RecycleViewAdapter(Scripts.getData()));
    }

    private void showOptions() {
        PopupMenu popupMenu = new PopupMenu(this, mFab);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.create));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.import_item));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (Scripts.mOutput == null) {
                        Scripts.mOutput = new StringBuilder();
                    } else {
                        Scripts.mOutput.setLength(0);
                    }
                    Utils.dialogEditText(null,
                            (dialogInterface, i) -> {
                            }, text -> {
                                if (text.isEmpty()) {
                                    Utils.snackbar(mRecyclerView, getString(R.string.name_empty));
                                    return;
                                }
                                if (!text.endsWith(".sh")) {
                                    text += ".sh";
                                }
                                if (text.contains(" ")) {
                                    text = text.replace(" ", "_");
                                }
                                if (Utils.existFile(Scripts.scriptExistsCheck(text))) {
                                    Utils.snackbar(mRecyclerView, getString(R.string.script_exists, text));
                                    return;
                                }
                                Scripts.mScriptName = text;
                                Scripts.mScriptPath = null;
                                Scripts.createScript(this);
                            }, this).setOnDismissListener(dialogInterface -> {
                    }).show();
                    break;
                case 1:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, 0);
                    break;
            }
            return false;
        });
        popupMenu.show();
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
                Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".sh"));
                return;
            }
            if (!Scripts.isScript(mPath)) {
                Utils.snackbar(mRecyclerView, getString(R.string.wrong_script, file.getName().replace(".sh", "")));
                return;
            }
            if (Utils.existFile(Scripts.scriptExistsCheck(file.getName()))) {
                Utils.snackbar(mRecyclerView, getString(R.string.script_exists, file.getName()));
                return;
            }
            AlertDialog.Builder selectQuestion = new AlertDialog.Builder(this);
            selectQuestion.setMessage(getString(R.string.select_question, file.getName().replace("primary:", "")
                    .replace("file%3A%2F%2F%2F", "").replace("%2F", "/")));
            selectQuestion.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
            });
            selectQuestion.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                Scripts.importScript(mPath);
                Utils.restartApp(this);
            });
            selectQuestion.show();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if (Utils.getBoolean("welcomeMessage", true, this)) {
            Utils.WelcomeDialog(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (Utils.rootAccess()) {
            if (mExit) {
                mExit = false;
                super.onBackPressed();
            } else {
                Utils.snackbar(mRecyclerView, getString(R.string.press_back));
                mExit = true;
                mHandler.postDelayed(() -> mExit = false, 2000);
            }
        } else {
            super.onBackPressed();
        }
    }
}