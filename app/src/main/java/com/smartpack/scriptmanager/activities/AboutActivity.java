/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 05, 2020
 */
public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AppCompatImageView mDeveloper = findViewById(R.id.developer);
        MaterialTextView mCancelButton = findViewById(R.id.cancel_button);
        MaterialTextView mAppTitle = findViewById(R.id.app_title);
        MaterialTextView mChangeLog = findViewById(R.id.changelog);

        boolean isProUser = Utils.getBoolean("support_received", false, this) || !Utils.isNotDonated(this);
        mDeveloper.setOnClickListener(v -> Utils.launchUrl("https://github.com/sunilpaulmathew", this));
        mAppTitle.setText(getString(R.string.app_name) + (isProUser ? " Pro " : " ") + BuildConfig.VERSION_NAME);
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "update_info.json"))).getString("changelogFull");
        } catch (JSONException ignored) {
        }
        mChangeLog.setText(change_log);
        mCancelButton.setOnClickListener(v -> onBackPressed());
    }

}