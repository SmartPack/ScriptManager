/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.smartpack.scriptmanager.fragments.ScriptsFragment;
import com.smartpack.scriptmanager.utils.PagerAdapter;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.root.RootUtils;
import com.smartpack.scriptmanager.views.dialog.Dialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.no_root_Text);
        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            Utils.toast(getString(R.string.no_root_message), this);
            return;
        }

        AppCompatImageView imageView = findViewById(R.id.banner);
        ViewPager viewPager = findViewById(R.id.viewPagerID);
        TextView copyRightText = findViewById(R.id.copyright_Text);

        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new ScriptsFragment(), getString(R.string.app_name));

        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_banner));
        copyRightText.setText(getString(R.string.credits));
        viewPager.setAdapter(adapter);
    }

    public void creditsDialogue(View view) {
        if (!RootUtils.rootAccess()) {
            return;
        }
        new Dialog(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.credits_summary))
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setNegativeButton(getString(R.string.report_issue), (dialogInterface, i) -> {
                    if (!Utils.isNetworkAvailable(this)) {
                        Utils.toast(getString(R.string.update_check_failed) + " " + getString(R.string.no_internet), this);
                        return;
                    }
                    Utils.launchUrl("https://github.com/SmartPack/ScriptManager/issues/new", this);
                })
                .setPositiveButton(getString(R.string.support), (dialogInterface, i) -> {
                    if (!Utils.isNetworkAvailable(this)) {
                        Utils.toast(getString(R.string.update_check_failed) + " " + getString(R.string.no_internet), this);
                        return;
                    }
                    Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                })
                .show();
    }

    public void showSource(View view) {
        if (!Utils.isNetworkAvailable(this)) {
            Utils.toast(getString(R.string.update_check_failed) + " " + getString(R.string.no_internet), this);
            return;
        }
        Utils.launchUrl("https://github.com/SmartPack/ScriptManager", this);
    }
}