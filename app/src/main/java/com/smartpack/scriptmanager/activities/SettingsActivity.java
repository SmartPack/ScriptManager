/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.adapters.RecycleViewSettingsAdapter;
import com.smartpack.scriptmanager.utils.AppSettings;
import com.smartpack.scriptmanager.utils.Billing;
import com.smartpack.scriptmanager.utils.RecycleViewItem;
import com.smartpack.scriptmanager.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 13, 2020
 */
public class SettingsActivity extends AppCompatActivity {

    private final ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        LinearLayout mAppInfo = findViewById(R.id.app_info);
        MaterialTextView mAppTitle = findViewById(R.id.title);
        MaterialTextView mAppDescription = findViewById(R.id.description);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppTitle.setText(getString(R.string.app_name) + (Utils.isProUser(this) ? " Pro " :  " ") + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(Utils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);
        mAppDescription.setText(BuildConfig.APPLICATION_ID);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        RecycleViewSettingsAdapter mRecycleViewAdapter = new RecycleViewSettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mAppInfo.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            settings.setData(uri);
            startActivity(settings);
            finish();
        });

        mData.add(new RecycleViewItem(getString(R.string.language), AppSettings.getLanguage(this), getResources().getDrawable(R.drawable.ic_language), null));
        mData.add(new RecycleViewItem(getString(R.string.file_picker), getString(Utils.getBoolean("use_file_picker", true, this) ? R.string.file_picker_inbuilt
                : R.string.file_picker_external), getResources().getDrawable(R.drawable.ic_folder), null));
        mData.add(new RecycleViewItem(getString(R.string.dark_theme), AppSettings.getAppThemeDescription(this), getResources().getDrawable(R.drawable.ic_theme), null));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(
                R.drawable.ic_github), "https://github.com/SmartPack/ScriptManager"));
        mData.add(new RecycleViewItem(getString(R.string.support_group), getString(R.string.support_group_summary), getResources().getDrawable(R.drawable.ic_support),
                "https://t.me/smartpack_kmanager"));
        mData.add(new RecycleViewItem(getString(R.string.report_issue), getString(R.string.report_issue_summary), getResources().getDrawable(R.drawable.ic_issue),
                "https://github.com/SmartPack/ScriptManager/issues/new/choose"));
        mData.add(new RecycleViewItem(getString(R.string.support_development), null, getResources().getDrawable(R.drawable.ic_donate), null));
        mData.add(new RecycleViewItem(getString(R.string.more_apps), getString(R.string.more_apps_summary), getResources().getDrawable(
                R.drawable.ic_playstore), "https://play.google.com/store/apps/dev?id=5836199813143882901"));
        mData.add(new RecycleViewItem(getString(R.string.translations), getString(R.string.translations_summary), getResources().getDrawable(
                R.drawable.ic_translate), "https://poeditor.com/join/project?hash=w47nhsNDL7"));
        mData.add(new RecycleViewItem(getString(R.string.share_app), getString(R.string.share_app_Summary), getResources().getDrawable(R.drawable.ic_share), null));
        mData.add(new RecycleViewItem(getString(R.string.rate_us), getString(R.string.rate_us_Summary), getResources().getDrawable(R.drawable.ic_rate),
                "https://play.google.com/store/apps/details?id=com.smartpack.scriptmanager"));
        mData.add(new RecycleViewItem(getString(R.string.licence), null, getResources().getDrawable(R.drawable.ic_licence),
                "https://www.gnu.org/licenses/gpl-3.0-standalone.html"));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getUrl() != null) {
                Utils.launchUrl(mData.get(position).getUrl(), this);
            } else if (position == 0) {
                AppSettings.setLanguage(this);
            } else if (position == 1) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.file_picker), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.saveBoolean("use_file_picker", true, this);
                            mData.set(position, new RecycleViewItem(getString(R.string.file_picker), getString(R.string.file_picker_inbuilt),
                                    getResources().getDrawable(R.drawable.ic_folder), null));
                            mRecycleViewAdapter.notifyItemChanged(position);
                            break;
                        case 1:
                            Utils.saveBoolean("use_file_picker", false, this);
                            mData.set(position, new RecycleViewItem(getString(R.string.file_picker), getString(R.string.file_picker_external),
                                    getResources().getDrawable(R.drawable.ic_folder), null));
                            mRecycleViewAdapter.notifyItemChanged(position);
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 2) {
                AppSettings.setAppTheme(this);
            } else if (position == 6) {
                Billing.showDonateOption(this);
            } else if (position == 9) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message_app, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

}