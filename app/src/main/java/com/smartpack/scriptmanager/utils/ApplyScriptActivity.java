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
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.R;

import java.util.ConcurrentModificationException;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 26, 2020
 */

public class ApplyScriptActivity extends AppCompatActivity {

    private MaterialTextView mScriptTitle, mOutput;
    private MaterialCardView mCancelButton;
    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applyscript);

        mCancelButton = findViewById(R.id.cancel_button);
        mScriptTitle = findViewById(R.id.script_title);
        mOutput = findViewById(R.id.result_text);
        mScrollView = findViewById(R.id.scroll_view);

        mCancelButton.setOnClickListener(v -> onBackPressed());

        mScriptTitle.setText(getString(R.string.applying_script, Scripts.mScriptName));

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
                            try {
                                mOutput.setText(Utils.getOutput(Scripts.mOutput));
                                if (!Scripts.mApplyingScript) {
                                    mCancelButton.setVisibility(View.VISIBLE);
                                    mScriptTitle.setText(getString(R.string.script_applied_success, Scripts.mScriptName));
                                } else {
                                    mScriptTitle.setText(getString(R.string.applying_script, Scripts.mScriptName));
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