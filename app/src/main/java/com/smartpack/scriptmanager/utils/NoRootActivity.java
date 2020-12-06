/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 16, 2020
 */

public class NoRootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroot);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mBack.setOnClickListener(v -> onBackPressed());
        mCancel.setOnClickListener(v -> super.onBackPressed());
    }

}