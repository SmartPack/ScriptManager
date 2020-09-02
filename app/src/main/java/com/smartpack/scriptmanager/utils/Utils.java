/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.root.RootFile;
import com.smartpack.scriptmanager.utils.root.RootUtils;
import com.smartpack.scriptmanager.views.dialog.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class Utils {

    public static AppCompatImageButton mBack;
    public static AppCompatImageView mAppIcon;
    public static AppCompatTextView mCardTitle;
    public static AppCompatTextView mAppName;
    public static AppCompatTextView mAboutApp;
    public static AppCompatTextView mDevelopedBy;
    public static AppCompatTextView mCreditsTitle;
    public static AppCompatTextView mCredits;
    public static AppCompatTextView mForegroundText;
    public static AppCompatTextView mCancel;
    public static AppCompatImageView mDeveloper;
    public static boolean mForegroundActive = false;
    public static CardView mForegroundCard;

    private static final String TAG = Utils.class.getSimpleName();

    private static boolean mWelcomeDialog = true;

    public static boolean isNotDonated(Context context) {
        if (BuildConfig.DEBUG) return false;
        try {
            context.getPackageManager().getApplicationInfo("com.smartpack.donate", 0);
            return false;
        } catch (PackageManager.NameNotFoundException ignored) {
            return true;
        }
    }

    public static void initializeAppTheme(Context context) {
        if (Prefs.getBoolean("dark_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else if (Prefs.getBoolean("light_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void initializeGoogleAds(Context context) {
        MobileAds.initialize(context, "ca-app-pub-7791710838910455~1734786052");
    }

    public static boolean isTv(Context context) {
        return ((UiModeManager) Objects.requireNonNull(context.getSystemService(Context.UI_MODE_SERVICE)))
                .getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static String getInternalDataStorage() {
        return Environment.getExternalStorageDirectory().toString() + "/scripts";
    }

    public static void create(String text, String path) {
        RootUtils.runCommand("echo '" + text + "' > " + path);
    }

    public static void delete(String path) {
        if (Utils.existFile(path)) {
            RootUtils.runCommand("rm -r " + path);
        }
    }

    static void copy(String source, String dest) {
        RootUtils.runCommand("cp -r " + source + " " + dest);
    }

    static void chmod(String permission, String path) {
        RootUtils.runCommand("chmod " + permission + " " + path);
    }

    // MD5 code from
    // https://github.com/CyanogenMod/android_packages_apps_CMUpdater/blob/cm-12.1/src/com/cyanogenmod/updater/utils/MD5.java
    public static boolean checkMD5(String md5, File updateFile) {
        if (md5 == null || updateFile == null || md5.isEmpty()) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    private static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static void launchUrl(String url, Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Drawable getColoredIcon(int icon, Context context) {
        Drawable drawable = context.getResources().getDrawable(icon);
        drawable.setTint(ViewUtils.getThemeAccentColor(context));
        return drawable;
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static boolean isDownloadBinaries() {
        return Utils.existFile("/system/bin/curl") || Utils.existFile("/system/bin/wget");
    }

    static void downloadFile(String path, String url) {
        if (isDownloadBinaries()) {
            RootUtils.runCommand((Utils.existFile("/system/bin/curl") ?
                    "curl -L -o " : "wget -O ") + path + " " + url);
        } else {
            /*
             * Based on the following stackoverflow discussion
             * Ref: https://stackoverflow.com/questions/15758856/android-how-to-download-file-from-webserver
             */
            try (InputStream input = new URL(url).openStream();
                 OutputStream output = new FileOutputStream(path)) {
                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static String readFile(String file) {
        return readFile(file, true);
    }

    private static String readFile(String file, boolean root) {
        if (root) {
            return new RootFile(file).readFile();
        }

        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(file));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean existFile(String file) {
        return existFile(file, true);
    }

    private static boolean existFile(String file, boolean root) {
        return !root ? new File(file).exists() : new RootFile(file).exists();
    }

    public static boolean isNetworkUnavailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return (cm.getActiveNetworkInfo() == null) || !cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static String getPath(File file) {
        String path = file.getAbsolutePath();
        if (path.startsWith("/document/raw:")) {
            path = path.replace("/document/raw:", "");
        } else if (path.startsWith("/document/primary:")) {
            path = (Environment.getExternalStorageDirectory() + ("/") + path.replace("/document/primary:", ""));
        } else if (path.startsWith("/document/")) {
            path = path.replace("/document/", "/storage/").replace(":", "/");
        }
        if (path.startsWith("/storage_root/storage/emulated/0")) {
            path = path.replace("/storage_root/storage/emulated/0", "/storage/emulated/0");
        } else if (path.startsWith("/storage_root")) {
            path = path.replace("storage_root", "storage/emulated/0");
        }
        if (path.startsWith("/external")) {
            path = path.replace("external", "storage/emulated/0");
        } if (path.startsWith("/root/")) {
            path = path.replace("/root", "");
        }
        if (path.contains("file%3A%2F%2F%2F")) {
            path = path.replace("file%3A%2F%2F%2F", "").replace("%2F", "/");
        }
        return path;
    }

    public static boolean isDocumentsUI(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /*
     * Taken and used almost as such from the following stackoverflow discussion
     * Ref: https://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
     */
    public static boolean checkWriteStoragePermission(Context context) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Taken and used almost as such from the following stackoverflow discussion
     * https://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
     */
    public static String getExtension(String string) {
        return android.webkit.MimeTypeMap.getFileExtensionFromUrl(string);
    }

    /*
     * Taken and used almost as such from https://github.com/morogoku/MTweaks-KernelAdiutorMOD/
     * Ref: https://github.com/morogoku/MTweaks-KernelAdiutorMOD/blob/dd5a4c3242d5e1697d55c4cc6412a9b76c8b8e2e/app/src/main/java/com/moro/mtweaks/fragments/kernel/BoefflaWakelockFragment.java#L133
     */
    public static void WelcomeDialog(Context context) {
        View checkBoxView = View.inflate(context, R.layout.rv_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        checkBox.setText(context.getString(R.string.always_show));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                -> mWelcomeDialog = isChecked);

        new Dialog(Objects.requireNonNull(context))
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(context.getString(R.string.app_name))
                .setMessage(context.getText(R.string.welcome_message))
                .setCancelable(false)
                .setView(checkBoxView)
                .setNeutralButton(context.getString(R.string.examples), (dialog, id) -> {
                    Utils.launchUrl("https://github.com/SmartPack/ScriptManager/tree/master/examples", context);
                })
                .setPositiveButton(context.getString(R.string.got_it), (dialog, id)
                        -> Prefs.saveBoolean("welcomeMessage", mWelcomeDialog, context))

                .show();
    }

    private static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) {
                s.append(str).append("\n");
            }
            return s.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public static void aboutDialogue(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mCardTitle.setText(R.string.about);
        mAppName.setText(activity.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mCredits.setText(activity.getString(R.string.credits_summary));
        mCardTitle.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mAppIcon.setVisibility(View.VISIBLE);
        mAppName.setVisibility(View.VISIBLE);
        mAboutApp.setVisibility(View.VISIBLE);
        mDevelopedBy.setVisibility(View.VISIBLE);
        mDeveloper.setVisibility(View.VISIBLE);
        mCreditsTitle.setVisibility(View.VISIBLE);
        mCredits.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mForegroundActive = true;
        mForegroundCard.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public static void changeLogs(Activity activity) {
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(readAssetFile(
                    activity, "update_info.json"))).getString("changelogFull");
        } catch (JSONException ignored) {
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mCardTitle.setText(R.string.change_log);
        mAppName.setText(activity.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mForegroundText.setText(change_log);
        mBack.setVisibility(View.VISIBLE);
        mCardTitle.setVisibility(View.VISIBLE);
        mAppIcon.setVisibility(View.VISIBLE);
        mAppName.setVisibility(View.VISIBLE);
        mForegroundText.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mForegroundActive = true;
        mForegroundCard.setVisibility(View.VISIBLE);
    }

    public static void closeForeground(Activity activity) {
        mCardTitle.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        mAppIcon.setVisibility(View.GONE);
        mAppName.setVisibility(View.GONE);
        mAboutApp.setVisibility(View.GONE);
        mDevelopedBy.setVisibility(View.GONE);
        mDeveloper.setVisibility(View.GONE);
        mCreditsTitle.setVisibility(View.GONE);
        mCredits.setVisibility(View.GONE);
        mForegroundText.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mForegroundCard.setVisibility(View.GONE);
        mForegroundActive = false;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static boolean languageDefault(Context context) {
        return !Prefs.getBoolean("use_en", false, context)
                && !Prefs.getBoolean("use_ko", false, context)
                && !Prefs.getBoolean("use_in", false, context)
                && !Prefs.getBoolean("use_am", false, context)
                && !Prefs.getBoolean("use_el", false, context)
                && !Prefs.getBoolean("use_pt", false, context)
                && !Prefs.getBoolean("use_ru", false, context);
    }

    public static String getLang(Context context) {
        if (Prefs.getBoolean("use_en", false, context)) {
            return  "en_US";
        } else if (Prefs.getBoolean("use_ko", false, context)) {
            return  "ko";
        } else if (Prefs.getBoolean("use_in", false, context)) {
            return  "in";
        } else if (Prefs.getBoolean("use_am", false, context)) {
            return  "am";
        } else if (Prefs.getBoolean("use_el", false, context)) {
            return "el";
        } else if (Prefs.getBoolean("use_pt", false, context)) {
            return  "pt";
        } else if (Prefs.getBoolean("use_ru", false, context)) {
            return  "ru";
        } else {
            return java.util.Locale.getDefault().getLanguage();
        }
    }

    public static void setLanguage(Context context) {
        Locale myLocale = new Locale(getLang(context));
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}