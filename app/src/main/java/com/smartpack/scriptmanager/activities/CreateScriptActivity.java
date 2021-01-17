/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.R;

import java.util.ConcurrentModificationException;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class CreateScriptActivity extends AppCompatActivity {

    private AppCompatEditText mEditText;
    private MaterialTextView mTestOutput;
    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createscript);

        mEditText = findViewById(R.id.edit_text);
        mScrollView = findViewById(R.id.scroll_view_testing);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatImageButton mSave = findViewById(R.id.save_button);
        MaterialTextView scriptName = findViewById(R.id.script_name);
        MaterialCardView testButton = findViewById(R.id.test_button);
        mTestOutput = findViewById(R.id.test_output);

        if (Scripts.mScriptPath == null) {
            mEditText.setText("#!/system/bin/sh\n\n");
        } else {
            mEditText.setText(Scripts.readScript(Scripts.mScriptPath));
        }

        scriptName.setText(Scripts.mScriptName);
        mBack.setOnClickListener(v -> onBackPressed());
        mSave.setOnClickListener(v -> {
            Scripts.createScript(Scripts.mScriptPath == null ? Scripts.ScriptFile() + "/" + Scripts.mScriptName
                    : Scripts.mScriptPath, Objects.requireNonNull(mEditText.getText()).toString());
            if (Scripts.mScriptPath != null) {
                if (!Scripts.mScriptPath.startsWith(Utils.getInternalDataStorage())) {
                    Scripts.createScript(Scripts.ScriptFile() + "/" + Scripts.mScriptName,
                            Objects.requireNonNull(mEditText.getText()).toString());
                }
                if (Scripts.isMgiskPostFS() && Scripts.scriptOnPostBoot(Scripts.mScriptName)) {
                    Scripts.setScriptOnPostFS(Scripts.mScriptPath, Scripts.mScriptName);
                } else if (Scripts.isMgiskServiceD() && Scripts.scriptOnLateBoot(Scripts.mScriptName)) {
                    Scripts.setScriptOnServiceD(Scripts.mScriptPath, Scripts.mScriptName);
                }
            }
            Scripts.reloadUI();
            onBackPressed();
        });
        testButton.setOnClickListener(v -> {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            testCommands();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        });

        refreshStatus();
    }

    @SuppressLint("StaticFieldLeak")
    private void testCommands() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Scripts.mOutput.clear();
                Scripts.mOutput.add(getString(R.string.testing));
                Scripts.mApplyingScript = true;
                Utils.delete(getFilesDir().getPath() + "/sm");
                Utils.create(Objects.requireNonNull(mEditText.getText()).toString(),getFilesDir().getPath() + "/sm");
                Scripts.mOutput.add("Checking Output!");
                Scripts.mOutput.add("********************");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.runCommand("sleep 1");
                Utils.runAndGetLiveOutput("sh " + getFilesDir().getPath() + "/sm", Scripts.mOutput);
                return null;
            }
            @Override
            protected void onPostExecute(Void recyclerViewItems) {
                super.onPostExecute(recyclerViewItems);
                Scripts.mOutput.add("********************");
                Scripts.mOutput.add(getString(R.string.testing_success));
                Utils.delete(getFilesDir().getPath() + "/sm");
                Scripts.mApplyingScript = false;
            }
        }.execute();
    }

    private void refreshStatus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(() -> {
                            try {
                                if (mTestOutput != null && Scripts.mOutput != null) {
                                    mTestOutput.setText(Utils.getOutput(Scripts.mOutput));
                                }
                                if (Scripts.mApplyingScript) {
                                    mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                }
                            } catch (ConcurrentModificationException ignored) {}
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (Scripts.mApplyingScript) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.script_execute_busy, Scripts.mScriptName))
                    .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    }).show();
        } else {
            super.onBackPressed();
        }
    }

}