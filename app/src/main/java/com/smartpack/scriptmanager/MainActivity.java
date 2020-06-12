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
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.smartpack.scriptmanager.fragments.ScriptsFragment;
import com.smartpack.scriptmanager.utils.PagerAdapter;
import com.smartpack.scriptmanager.utils.Prefs;
import com.smartpack.scriptmanager.utils.UpdateCheck;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.ViewUtils;
import com.smartpack.scriptmanager.utils.root.RootUtils;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatImageButton mSettings;
    private boolean mExit;
    private Handler mHandler = new Handler();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme & FaceBook Ads
        Utils.initializeAppTheme(this);
        Utils.initializeFaceBookAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        mSettings = findViewById(R.id.settings_icon);
        mViewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView textView = findViewById(R.id.no_root_Text);
        AppCompatImageView noroot = findViewById(R.id.no_root_Image);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);
        Utils.mForegroundCard = findViewById(R.id.foreground_card);
        Utils.mBack = findViewById(R.id.back);
        Utils.mAppIcon = findViewById(R.id.app_image);
        Utils.mCardTitle = findViewById(R.id.card_title);
        Utils.mAppName = findViewById(R.id.app_title);
        Utils.mAboutApp = findViewById(R.id.about_app);
        Utils.mCreditsTitle = findViewById(R.id.credits_title);
        Utils.mCredits = findViewById(R.id.credits);
        Utils.mForegroundText = findViewById(R.id.foreground_text);
        Utils.mCancel = findViewById(R.id.cancel_button);
        Utils.mBack.setOnClickListener(v -> {
            Utils.closeForeground();
        });
        Utils.mCancel.setOnClickListener(v -> {
            Utils.closeForeground();
        });
        mSettings.setOnClickListener(v -> {
            if (Utils.mForegroundActive) return;
            settingsMenu();
        });

        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            noroot.setImageDrawable(Utils.getColoredIcon(R.drawable.ic_help, this));
            Utils.snackbar(mViewPager, getString(R.string.no_root_message));
            return;
        }

        if (Prefs.getBoolean("allow_ads", true, this)) {
            AdView mAdView = new AdView(this, "804059380003092_804060013336362", AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = findViewById(R.id.banner_container);
            adContainer.addView(mAdView);
            mAdView.loadAd();
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new ScriptsFragment(), getString(R.string.app_name));

        // Allow changing Copyright Text
        if (Utils.existFile(Utils.getInternalDataStorage() + "/copyright") &&
                Utils.readFile(Utils.getInternalDataStorage() + "/copyright") != null) {
            copyRightText.setText(Utils.readFile(Utils.getInternalDataStorage() + "/copyright"));
        } else {
            copyRightText.setText(R.string.copyright);
        }
        copyRightText.setOnLongClickListener(item -> {
            if (Utils.checkWriteStoragePermission(this)) {
                setCopyRightText();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
            return false;
        });

        mViewPager.setAdapter(adapter);
    }

    private void settingsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mSettings);
        Menu menu = popupMenu.getMenu();
        if (!Utils.isNotDonated(this)) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("allow_ads", true, this));
        }
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true).setChecked(
                Prefs.getBoolean("dark_theme", true, this));
        SubMenu language = menu.addSubMenu(Menu.NONE, 2, Menu.NONE, getString(R.string.language, Utils.getLang(this)));
        language.add(Menu.NONE, 3, Menu.NONE, getString(R.string.language_default)).setCheckable(true).setChecked(
                Utils.languageDefault(this));
        language.add(Menu.NONE, 4, Menu.NONE, getString(R.string.language_en)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_en", false, this));
        language.add(Menu.NONE, 5, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                .setChecked(Prefs.getBoolean("use_ko", false, this));
        language.add(Menu.NONE, 6, Menu.NONE, getString(R.string.language_in)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_in", false, this));
        language.add(Menu.NONE, 7, Menu.NONE, getString(R.string.language_am)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_am", false, this));
        language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_el)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_el", false, this));
        language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_pt)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_pt", false, this));
        language.add(Menu.NONE, 17, Menu.NONE, getString(R.string.language_ru)).setCheckable(true).setChecked(
                Prefs.getBoolean("use_ru", false, this));
        SubMenu about = menu.addSubMenu(Menu.NONE, 2, Menu.NONE, getString(R.string.about));
        about.add(Menu.NONE, 13, Menu.NONE, getString(R.string.examples));
        about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.source_code));
        about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.support_group));
        about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.more_apps));
        about.add(Menu.NONE, 11, Menu.NONE, getString(R.string.report_issue));
        about.add(Menu.NONE, 16, Menu.NONE, getString(R.string.change_log));
        about.add(Menu.NONE, 12, Menu.NONE, getString(R.string.about));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (Prefs.getBoolean("allow_ads", true, this)) {
                        Prefs.saveBoolean("allow_ads", false, this);
                    } else {
                        Prefs.saveBoolean("allow_ads", true, this);
                    }
                    restartApp();
                    break;
                case 1:
                    if (Prefs.getBoolean("dark_theme", true, this)) {
                        Prefs.saveBoolean("dark_theme", false, this);
                    } else {
                        Prefs.saveBoolean("dark_theme", true, this);
                    }
                    restartApp();
                    break;
                case 2:
                    break;
                case 3:
                    if (!Utils.languageDefault(this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 4:
                    if (!Prefs.getBoolean("use_en", false, this)) {
                        Prefs.saveBoolean("use_en", true, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 5:
                    if (!Prefs.getBoolean("use_ko", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", true, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 6:
                    if (!Prefs.getBoolean("use_in", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", true, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 7:
                    if (!Prefs.getBoolean("use_am", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", true, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 14:
                    if (!Prefs.getBoolean("use_el", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", true, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                    break;
                case 15:
                    if (!Prefs.getBoolean("use_pt", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", true, this);
                        Prefs.saveBoolean("use_ru", false, this);
                        restartApp();
                    }
                case 17:
                    if (!Prefs.getBoolean("use_ru", false, this)) {
                        Prefs.saveBoolean("use_en", false, this);
                        Prefs.saveBoolean("use_ko", false, this);
                        Prefs.saveBoolean("use_in", false, this);
                        Prefs.saveBoolean("use_am", false, this);
                        Prefs.saveBoolean("use_el", false, this);
                        Prefs.saveBoolean("use_pt", false, this);
                        Prefs.saveBoolean("use_ru", true, this);
                        restartApp();
                    }
                    break;
                case 8:
                    launchURL("https://github.com/SmartPack/ScriptManager");
                    break;
                case 9:
                    launchURL("https://t.me/smartpack_kmanager");
                    break;
                case 10:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/dev?id=5836199813143882901"));
                    startActivity(intent);
                    break;
                case 11:
                    launchURL("https://github.com/SmartPack/ScriptManager/issues/new");
                    break;
                case 12:
                    Utils.aboutDialogue(this);
                    break;
                case 13:
                    launchURL("https://github.com/SmartPack/ScriptManager/tree/master/examples");
                    break;
                case 16:
                    Utils.changeLogs(this);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    public void setCopyRightText() {
        String copyright = Utils.getInternalDataStorage() + "/copyright";
        File file = new File(Utils.getInternalDataStorage());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
        ViewUtils.dialogEditText(Utils.existFile(copyright) ?
                        Utils.readFile(copyright) : null,
                (dialogInterface, i) -> {
                }, text -> {
                    if (text.equals(Utils.readFile(copyright))) return;
                    if (text.isEmpty()) {
                        Utils.delete(copyright);
                        Utils.snackbar(mViewPager, getString(R.string.copyright_default, getString(R.string.copyright)));
                        return;
                    }
                    Utils.create(text, copyright);
                    Utils.snackbar(mViewPager, getString(R.string.copyright_message, text));
                }, this).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    private void launchURL(String url) {
        if (Utils.isNetworkUnavailable(this)) {
            Utils.snackbar(mViewPager, getString(R.string.no_internet));
        } else {
            Utils.launchUrl(url, this);
        }
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void androidRooting(View view) {
        launchURL("https://www.google.com/search?site=&source=hp&q=android+rooting+magisk");
    }

    @Override
    public void onStart(){
        super.onStart();
        if (Prefs.getBoolean("welcomeMessage", true, this)) {
            Utils.WelcomeDialog(this);
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
            if (Utils.mForegroundActive) {
                Utils.closeForeground();
            } else if (mExit) {
                mExit = false;
                super.onBackPressed();
            } else {
                Utils.snackbar(mViewPager, getString(R.string.press_back));
                mExit = true;
                mHandler.postDelayed(() -> mExit = false, 2000);
            }
        } else {
            super.onBackPressed();
        }
    }

}