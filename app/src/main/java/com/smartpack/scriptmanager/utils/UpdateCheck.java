/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.views.dialog.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class UpdateCheck {

    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/SmartPack/ScriptManager/master/release/update_info.json";
    private static final String DOWNLOAD_URL = "https://github.com/SmartPack/ScriptManager/tree/master/release";
    private static final String UPDATE_INFO = Utils.getInternalDataStorage() + "/update_info.json";

    public static boolean isPlayStoreInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.android.vending", 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private static void prepareInternalStorage() {
        File file = new File(Utils.getInternalDataStorage());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static void getVersionInfo() {
        prepareInternalStorage();
        Utils.downloadFile(UPDATE_INFO, LATEST_VERSION_URL);
    }

    public static int versionNumber() {
        try {
            JSONObject obj = new JSONObject(Utils.readFile(UPDATE_INFO));
            return (obj.getInt("versionCode"));
        } catch (JSONException e) {
            return BuildConfig.VERSION_CODE;
        }
    }

    private static String versionName() {
        try {
            JSONObject obj = new JSONObject(Utils.readFile(UPDATE_INFO));
            return (obj.getString("versionName"));
        } catch (JSONException e) {
            return BuildConfig.VERSION_NAME;
        }
    }

    private static String changelogs() {
        try {
            JSONObject obj = new JSONObject(Utils.readFile(UPDATE_INFO));
            return (obj.getString("releaseNotes"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static boolean hasVersionInfo() {
        return Utils.existFile(UPDATE_INFO);
    }

    public static long lastModified() {
        return new File(UPDATE_INFO).lastModified();
    }

    public static void updateAvailableDialog(Context context) {
        new Dialog(context)
                .setTitle(context.getString(R.string.update_available, UpdateCheck.versionName()))
                .setMessage(context.getString(R.string.update_available_summary, changelogs()))
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.get_it), (dialog, id) -> {
                    Utils.launchUrl(DOWNLOAD_URL, context);
                })
                .show();
    }

}