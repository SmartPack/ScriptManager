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
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smartpack.scriptmanager.fragments.ScriptsFragment;
import com.smartpack.scriptmanager.utils.PagerAdapter;
import com.smartpack.scriptmanager.utils.Prefs;
import com.smartpack.scriptmanager.utils.UpdateCheck;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.root.RootUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.getInstance().initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        AppCompatTextView textView = findViewById(R.id.no_root_Text);
        AppCompatImageView noroot = findViewById(R.id.no_root_Image);

        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            noroot.setImageDrawable(getResources().getDrawable(R.drawable.ic_help));
            Utils.toast(getString(R.string.no_root_message), this);
            return;
        }

        AppCompatImageView imageView = findViewById(R.id.banner);
        ViewPager viewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);

        if (Prefs.getBoolean("allow_ads", true, this)) {
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new ScriptsFragment(), getString(R.string.app_name));

        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_banner));

        // Allow changing Copyright Text
        if (Utils.existFile(Utils.getInternalDataStorage() + "/copyright") &&
                Utils.readFile(Utils.getInternalDataStorage() + "/copyright") != null) {
            copyRightText.setText(Utils.readFile(Utils.getInternalDataStorage() + "/copyright"));
        } else {
            copyRightText.setText(R.string.copyright);
        }
        copyRightText.setOnLongClickListener(item -> {
            if (Utils.checkWriteStoragePermission(this)) {
                Utils.setCopyRightText(this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
            return false;
        });

        viewPager.setAdapter(adapter);
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