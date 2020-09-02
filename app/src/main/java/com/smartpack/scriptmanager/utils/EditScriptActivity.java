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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.root.RootUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class EditScriptActivity extends AppCompatActivity {

    public static final String TITLE_INTENT = "title";
    public static final String TEXT_INTENT = "text";
    private static final String EDITTEXT_INTENT = "edittext";

    private static AppCompatEditText mEditText;

    private static AppCompatTextView mTestOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initToolBar();
        String title = getIntent().getStringExtra(TITLE_INTENT);
        if (title != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        }

        CharSequence text = getIntent().getCharSequenceExtra(TEXT_INTENT);
        mEditText = findViewById(R.id.edit_text);
        if (text != null) {
            mEditText.append(text);
            mEditText.setVisibility(View.VISIBLE);
        }
        AppCompatTextView testButton = findViewById(R.id.test_button);
        testButton.setText(R.string.test);
        testButton.setVisibility(View.VISIBLE);
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
                String output = RootUtils.runAndGetError("sh  /data/local/tmp/sm");
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(EDITTEXT_INTENT, mEditText.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_save);
        assert drawable != null;
        DrawableCompat.setTint(drawable, Color.BLACK);
        menu.add(0, Menu.FIRST, Menu.FIRST, getString(R.string.save)).setIcon(drawable)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent();
        intent.putExtra(TEXT_INTENT, mEditText.getText());
        setResult(0, intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    public void initToolBar() {
        Toolbar toolbar = getToolBar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setNavigationOnClickListener(v -> finish());
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (Scripts.mApplyingScript) return;
        super.onBackPressed();
    }

}