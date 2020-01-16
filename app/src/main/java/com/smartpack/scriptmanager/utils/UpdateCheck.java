/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.FileProvider;

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
    private static final String LATEST_APK = Utils.getInternalDataStorage() + "/com.smartpack.scriptmanager.apk";
    private static final String DOWNLOAD_URL = "https://github.com/SmartPack/ScriptManager/raw/master/release/com.smartpack.scriptmanager.apk";
    private static final String UPDATE_INFO = Utils.getInternalDataStorage() + "/update_info.json";
    private static final String UPDATE_INFO_STRING = Utils.readFile(UPDATE_INFO);

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

    private static void getLatestApp() {
        prepareInternalStorage();
        Utils.downloadFile(LATEST_APK, DOWNLOAD_URL);
    }

    public static String versionName() {
        try {
            JSONObject obj = new JSONObject(UPDATE_INFO_STRING);
            return (obj.getString("versionName"));
        } catch (JSONException e) {
            return BuildConfig.VERSION_NAME;
        }
    }

    private static String changelogs() {
        try {
            JSONObject obj = new JSONObject(UPDATE_INFO_STRING);
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
                .setMessage(UpdateCheck.changelogs())
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.get_it), (dialog, id) -> {
                    updaterTask(context);
                })
                .show();
    }

    private static void updaterTask(Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.downloading) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                getLatestApp();
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                File apk = new File(LATEST_APK);
                if (apk.exists() && apk.length() > 500000) {
                    installUpdate(context);
                } else {
                    Utils.toast(R.string.download_failed, context);
                }

            }
        }.execute();
    }

    private static void installUpdate(Context context) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uriFile;
        uriFile = FileProvider.getUriForFile(context, "com.smartpack.scriptmanager.provider",
                new File(Utils.getInternalDataStorage() + "/com.smartpack.scriptmanager.apk"));
        intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
        context.startActivity(Intent.createChooser(intent, ""));
    }

}