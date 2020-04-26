/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.smartpack.scriptmanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 26, 2020
 */

public class ApplyScriptActivity extends AppCompatActivity {

    public static final String TITLE_INTENT = "title";
    private static String mTitle;

    private static AppCompatTextView mCancelButton;
    private static AppCompatTextView mScriptTitle;
    private static AppCompatTextView mOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mCancelButton = findViewById(R.id.cancel_button);
        mScriptTitle = findViewById(R.id.script_title);
        mTitle = getIntent().getStringExtra(TITLE_INTENT);
        if (mTitle != null) {
            mScriptTitle.setText(getString(R.string.applying_script, mTitle));
        }
        mOutput = findViewById(R.id.result_text);
        mCancelButton.setOnClickListener(v -> {
            onBackPressed();
        });
        refreshStatus();
    }

    public void refreshStatus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(() -> {
                            if (Scripts.mOutput != null) {
                                mOutput.setText(Scripts.mOutput.toString());
                                mScriptTitle.setVisibility(View.VISIBLE);
                                mOutput.setVisibility(View.VISIBLE);
                                if (!Scripts.mApplyingScript) {
                                    mCancelButton.setVisibility(View.VISIBLE);
                                    mScriptTitle.setText(getString(R.string.script_applied, mTitle));
                                }
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