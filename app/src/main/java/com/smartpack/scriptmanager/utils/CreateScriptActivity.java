/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.smartpack.scriptmanager.R;

import java.lang.ref.WeakReference;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class CreateScriptActivity extends AppCompatActivity {

    private static AppCompatEditText mEditText;
    private static AppCompatTextView mTestOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_tasks);

        mEditText = findViewById(R.id.edit_text);
        if (Scripts.mScriptPath == null) {
            mEditText.setText("#!/system/bin/sh\n\n");
        } else {
            mEditText.setText(Scripts.readScript(Scripts.mScriptPath));
        }
        mEditText.setVisibility(View.VISIBLE);
        FrameLayout mAppBar = findViewById(R.id.app_bar);
        mAppBar.setVisibility(View.VISIBLE);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatImageButton mSave = findViewById(R.id.save_button);
        AppCompatTextView scriptName = findViewById(R.id.script_name);
        scriptName.setText(Scripts.mScriptName);
        AppCompatTextView testButton = findViewById(R.id.test_button);
        testButton.setText(R.string.test);
        testButton.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(v -> onBackPressed());
        mSave.setOnClickListener(v -> {
            Scripts.createScript(Scripts.mScriptPath == null ? Scripts.ScriptFile() + "/" + Scripts.mScriptName
                    : Scripts.mScriptPath, Objects.requireNonNull(mEditText.getText()).toString());
            if (Scripts.mScriptPath != null) {
                if (Scripts.isMgiskPostFS() && Scripts.scriptOnPostBoot(Scripts.mScriptName)) {
                    Scripts.setScriptOnPostFS(Scripts.mScriptPath, Scripts.mScriptName);
                } else if (Scripts.isMgiskServiceD() && Scripts.scriptOnLateBoot(Scripts.mScriptName)) {
                    Scripts.setScriptOnServiceD(Scripts.mScriptPath, Scripts.mScriptName);
                }
            }
            onBackPressed();
            if (Scripts.mScriptPath == null) Utils.restartApp(this);
        });
        testButton.setOnClickListener(v -> {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            testCommands(new WeakReference<>(this));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        });
        mTestOutput = findViewById(R.id.test_output);
        refreshStatus();
    }

    private static void testCommands(WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.testing));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                Scripts.mApplyingScript = true;
                if (Scripts.mOutput == null) {
                    Scripts.mOutput = new StringBuilder();
                } else {
                    Scripts.mOutput.setLength(0);
                }
                Utils.delete("/data/local/tmp/sm");
                Utils.create(Objects.requireNonNull(mEditText.getText()).toString(),"/data/local/tmp/sm");
                String output = Utils.runAndGetError("sh  /data/local/tmp/sm");
                if (output.isEmpty()) {
                    output = activityRef.get().getString(R.string.testing_success);
                }
                Scripts.mOutput.append(output);
                Utils.delete("/data/local/tmp/sm");
                Scripts.mApplyingScript = false;
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
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
                            if (mTestOutput != null && Scripts.mOutput != null) {
                                mTestOutput.setVisibility(View.VISIBLE);
                                mTestOutput.setText(Scripts.mOutput.toString());
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (Scripts.mApplyingScript) return;
        super.onBackPressed();
    }

}