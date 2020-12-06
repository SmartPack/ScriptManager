/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class Scripts {

    private static AsyncTask<Void, Void, Void> mLoader;
    private static Handler mHandler = new Handler();
    public static RecyclerView mRecyclerView;
    public static RecycleViewAdapter mRecycleViewAdapter;

    private static final String SCRIPTS = Utils.getInternalDataStorage();
    private static final String MAGISK_SERVICED = "/data/adb/service.d";
    private static final String MAGISK_POSTFS = "/data/adb/post-fs-data.d";

    public static String mScriptName;
    public static String mScriptPath;

    public static List<String> mOutput = null;

    public static boolean mApplyingScript = false;

    public static File ScriptFile() {
        return new File(SCRIPTS);
    }
    public static File MagiskServiceFile() {
        return new File(MAGISK_SERVICED);
    }
    public static File MagiskPostFSFile() {
        return new File(MAGISK_POSTFS);
    }

    public static List<String> scriptItems() {
        if (Utils.existFile(ScriptFile().toString())) {
            Utils.mkdir(ScriptFile().toString());
        }
        List<String> list = new ArrayList<>();
        String files = Utils.runAndGetOutput("ls '" + ScriptFile().toString() + "/'");
        if (!files.isEmpty()) {
            // Make sure the files exists
            for (String file : files.split("\\r?\\n")) {
                if (file != null && !file.isEmpty() && Utils.existFile(ScriptFile().toString() + "/" + file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public static List<String> getData() {
        List<String> mData = new ArrayList<>();
        if (ScriptFile().exists()) {
            for (final String scriptsItems : scriptItems()) {
                File scripts = new File(ScriptFile() + "/" + scriptsItems);
                if (ScriptFile().length() > 0 && isScript(scripts.toString())) {
                    mData.add(scripts.getName().replace(".sh", ""));
                }
            }
        }
        return mData;
    }

    private static void makeScriptFolder() {
        if (ScriptFile().exists() && ScriptFile().isFile()) {
            ScriptFile().delete();
        }
        ScriptFile().mkdirs();
    }

    public static void importScript(String string) {
        makeScriptFolder();
        Utils.runCommand("cp " + string + " " + SCRIPTS);
    }

    public static void createScript(String file, String text) {
        makeScriptFolder();
        Utils.create(text, file);
    }

    public static void createScript(Context context) {
        mOutput = new ArrayList<>();
        Intent intent = new Intent(context, CreateScriptActivity.class);
        context.startActivity(intent);
    }

    public static void deleteScript(String path) {
        File file = new File(path);
        file.delete();
        if (Utils.existFile(MagiskServiceFile() + "/" + file.getName())) {
            Utils.runCommand("rm -r " + MagiskServiceFile() + "/" + file.getName());
        }
    }

    private static void applyScript(String file) {
        mOutput.add("Checking Output!");
        Utils.runCommand("sleep 1");
        mOutput.add("********************");
        Utils.runAndGetLiveOutput("sh " + file, mOutput);
    }

    public static void applyScript(Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mApplyingScript = true;
                mOutput = new ArrayList<>();
                mOutput.add("Executing " + mScriptName + "... Please be patient!\n\n");
                Intent applyIntent = new Intent(context, ApplyScriptActivity.class);
                context.startActivity(applyIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                applyScript(mScriptPath);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mOutput.add("********************");
                mOutput.add(context.getString(R.string.script_applied_success, mScriptName));
                mApplyingScript = false;
            }
        }.execute();
    }

    @SuppressLint("StringFormatInvalid")
    public static void shareScript(Context context) {
        Uri uriFile = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", new File(mScriptPath));
        Intent shareScript = new Intent(Intent.ACTION_SEND);
        shareScript.setType("application/sh");
        shareScript.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_by, mScriptName));
        shareScript.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, "v" + BuildConfig.VERSION_NAME));
        shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
        shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareScript, context.getString(R.string.share_with)));
    }

    public static String readScript(String file) {
        return Utils.readFile(file);
    }

    public static boolean isScript(String file) {
        return Utils.getExtension(file).equals("sh") && readScript(file).startsWith("#!/system/bin/sh");
    }

    public static String scriptExistsCheck(String script) {
        return ScriptFile().toString() + "/" + script;
    }

    public static boolean isMgiskServiceD() {
        return Utils.existFile(MAGISK_SERVICED);
    }

    public static boolean isMgiskPostFS() {
        return Utils.existFile(MAGISK_POSTFS);
    }

    public static void setScriptOnServiceD(String path, String name) {
        Utils.copy(path, MAGISK_SERVICED);
        Utils.chmod("755", MAGISK_SERVICED + "/" + name + ".sh");
    }

    public static void setScriptOnPostFS(String path, String name) {
        Utils.copy(path, MAGISK_POSTFS);
        Utils.chmod("755", MAGISK_POSTFS + "/" + name + ".sh");
    }

    public static boolean scriptOnPostBoot(String name) {
        return Utils.existFile(MAGISK_POSTFS + "/" + name + ".sh");
    }

    public static boolean scriptOnLateBoot(String name) {
        return Utils.existFile(MAGISK_SERVICED + "/" + name + ".sh");
    }

    public static void reloadUI() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

}