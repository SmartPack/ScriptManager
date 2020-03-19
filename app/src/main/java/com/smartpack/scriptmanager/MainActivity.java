/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.smartpack.scriptmanager.fragments.ScriptsFragment;
import com.smartpack.scriptmanager.utils.PagerAdapter;
import com.smartpack.scriptmanager.utils.Prefs;
import com.smartpack.scriptmanager.utils.UpdateCheck;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.root.RootUtils;
import com.smartpack.scriptmanager.views.dialog.Dialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize Dark Theme & Google Ads
        Utils.initializeAppTheme();
        Utils.getInstance().initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.no_root_Text);
        AppCompatImageView noroot = findViewById(R.id.no_root_Image);
        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            noroot.setImageDrawable(getResources().getDrawable(R.drawable.ic_help));
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
                .setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.credits_summary))
                .setNegativeButton(getString(R.string.more_apps), (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/developer?id=sunilpaulmathew"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                })
                .setNeutralButton(getString(R.string.report_issue), (dialogInterface, i) -> {
                    Utils.launchUrl("https://github.com/SmartPack/ScriptManager/issues/new", this);
                })
                .setPositiveButton(getString(R.string.support_group), (dialogInterface, i) -> {
                    Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                })
                .show();
    }

    public void showSource(View view) {
        Utils.launchUrl("https://github.com/SmartPack/ScriptManager", this);
    }

    public void androidRooting(View view) {
        Utils.launchUrl("https://www.google.com/search?site=&source=hp&q=android+rooting+magisk", this);
    }

    @Override
    public void onStart(){
        super.onStart();
        if (Prefs.getBoolean("welcomeMessage", true, this)) {
            Utils.getInstance().WelcomeDialog(this);
        }
        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }
        if (UpdateCheck.isPlayStoreInstalled(this)) {
            return;
        }
        if (Utils.isNetworkUnavailable(this)) {
            return;
        }
        if (!Utils.isDownloadBinaries()) {
            return;
        }
        if (!UpdateCheck.hasVersionInfo() || (UpdateCheck.lastModified() + 3720000L < System.currentTimeMillis())) {
            UpdateCheck.getVersionInfo();
        }
        if (UpdateCheck.hasVersionInfo() && BuildConfig.VERSION_CODE < UpdateCheck.versionNumber()) {
            UpdateCheck.updateAvailableDialog(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (RootUtils.rootAccess()) {
            if (mExit) {
                mExit = false;
                super.onBackPressed();
            } else {
                Utils.toast(R.string.press_back, this);
                mExit = true;
                mHandler.postDelayed(() -> mExit = false, 2000);
            }
        } else {
            super.onBackPressed();
        }
    }

}