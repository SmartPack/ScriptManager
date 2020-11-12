/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.snackbar.Snackbar;
import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.MainActivity;
import com.smartpack.scriptmanager.R;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class Utils {

    static {
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    public static AppCompatImageButton mSettings;
    private static boolean mWelcomeDialog = true;

    /*
     * The following code is partly taken from https://github.com/SmartPack/SmartPack-Kernel-Manager
     * Ref: https://github.com/SmartPack/SmartPack-Kernel-Manager/blob/beta/app/src/main/java/com/smartpack/kernelmanager/utils/root/RootUtils.java
     */
    public static boolean rootAccess() {
        return Shell.rootAccess();
    }

    public static void runCommand(String command) {
        Shell.su(command).exec();
    }

    @NonNull
    static String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> outputs = Shell.su(command).exec().getOut();
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    @NonNull
    public static String runAndGetError(String command) {
        StringBuilder sb = new StringBuilder();
        List<String> outputs = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.su(command).to(outputs, stderr).exec();
            outputs.addAll(stderr);
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String removeSuffix(@Nullable String s) {
        if (s != null && s.endsWith("\n")) {
            return s.substring(0, s.length() - "\n".length());
        }
        return s;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/ViewUtils.java
     */

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public interface OnDialogEditTextListener {
        void onClick(String text);
    }

    public static AlertDialog.Builder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                             final OnDialogEditTextListener onDialogEditTextListener,
                                             Context context) {
        return dialogEditText(text, negativeListener, onDialogEditTextListener, -1, context);
    }

    public static AlertDialog.Builder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                        final OnDialogEditTextListener onDialogEditTextListener, int inputType,
                                        Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(75, 75, 75, 75);

        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (text != null) {
            editText.append(text);
        }
        editText.setSingleLine(true);
        if (inputType >= 0) {
            editText.setInputType(inputType);
        }

        layout.addView(editText);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setView(layout);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (onDialogEditTextListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), (dialog1, which)
                    -> onDialogEditTextListener.onClick(Objects.requireNonNull(editText.getText()).toString()))
                    .setOnDismissListener(dialog1 -> {
                        if (negativeListener != null) {
                            negativeListener.onClick(dialog1, 0);
                        }
                    });
        }
        return dialog;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Prefs.java
     */
    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Utils.java
     */
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
        if (getBoolean("dark_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else if (getBoolean("light_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void mkdir(String path) {
        runCommand("mkdir -p '" + path + "'");
    }

    public static String getInternalDataStorage() {
        return Environment.getExternalStorageDirectory().toString() + "/scripts";
    }

    public static void create(String text, String path) {
        runCommand("echo '" + text + "' > " + path);
    }

    public static void delete(String path) {
        if (existFile(path)) {
            runCommand("rm -r " + path);
        }
    }

    public static void copy(String source, String dest) {
        runCommand("cp -r " + source + " " + dest);
    }

    public static void chmod(String permission, String path) {
        runCommand("chmod " + permission + " " + path);
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }

    public static void launchUrl(String url, Activity context) {
        if (isNetworkUnavailable(context)) {
            snackbar(mSettings, context.getString(R.string.no_internet));
        } else {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String readFile(String file) {
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
        String output = runAndGetOutput("[ -e " + file + " ] && echo true");
        return !output.isEmpty() && output.equals("true");
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

    public static void settingsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSettings);
        Menu menu = popupMenu.getMenu();
        SubMenu appTheme = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, activity.getString(R.string.dark_theme));
        appTheme.add(Menu.NONE, 16, Menu.NONE, activity.getString(R.string.dark_theme_auto)).setCheckable(true)
                .setChecked(getBoolean("theme_auto", true, activity));
        appTheme.add(Menu.NONE, 1, Menu.NONE, activity.getString(R.string.dark_theme_enable)).setCheckable(true)
                .setChecked(getBoolean("dark_theme", false, activity));
        appTheme.add(Menu.NONE, 17, Menu.NONE, activity.getString(R.string.dark_theme_disable)).setCheckable(true)
                .setChecked(getBoolean("light_theme", false, activity));
        SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, activity.getString(R.string.language, getLang(activity)));
        language.add(Menu.NONE, 2, Menu.NONE, activity.getString(R.string.language_default)).setCheckable(true).setChecked(
                languageDefault(activity));
        language.add(Menu.NONE, 3, Menu.NONE, activity.getString(R.string.language_en)).setCheckable(true).setChecked(
                getBoolean("use_en", false, activity));
        language.add(Menu.NONE, 4, Menu.NONE, activity.getString(R.string.language_ko)).setCheckable(true)
                .setChecked(getBoolean("use_ko", false, activity));
        language.add(Menu.NONE, 5, Menu.NONE, activity.getString(R.string.language_in)).setCheckable(true).setChecked(
                getBoolean("use_in", false, activity));
        language.add(Menu.NONE, 6, Menu.NONE, activity.getString(R.string.language_am)).setCheckable(true).setChecked(
                getBoolean("use_am", false, activity));
        language.add(Menu.NONE, 13, Menu.NONE, activity.getString(R.string.language_el)).setCheckable(true).setChecked(
                getBoolean("use_el", false, activity));
        language.add(Menu.NONE, 14, Menu.NONE, activity.getString(R.string.language_pt)).setCheckable(true).setChecked(
                getBoolean("use_pt", false, activity));
        language.add(Menu.NONE, 15, Menu.NONE, activity.getString(R.string.language_ru)).setCheckable(true).setChecked(
                getBoolean("use_ru", false, activity));
        SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, activity.getString(R.string.about));
        about.add(Menu.NONE, 12, Menu.NONE, activity.getString(R.string.examples));
        about.add(Menu.NONE, 7, Menu.NONE, activity.getString(R.string.source_code));
        about.add(Menu.NONE, 8, Menu.NONE, activity.getString(R.string.support_group));
        about.add(Menu.NONE, 9, Menu.NONE, activity.getString(R.string.more_apps));
        about.add(Menu.NONE, 10, Menu.NONE, activity.getString(R.string.report_issue));
        about.add(Menu.NONE, 11, Menu.NONE, activity.getString(R.string.about));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (!getBoolean("dark_theme", false, activity)) {
                        saveBoolean("dark_theme", true, activity);
                        saveBoolean("light_theme", false, activity);
                        saveBoolean("theme_auto", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 2:
                    if (!languageDefault(activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 3:
                    if (!getBoolean("use_en", false, activity)) {
                        saveBoolean("use_en", true, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 4:
                    if (!getBoolean("use_ko", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", true, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 5:
                    if (!getBoolean("use_in", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", true, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 6:
                    if (!getBoolean("use_am", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", true, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 7:
                    launchUrl("https://github.com/SmartPack/ScriptManager", activity);
                    break;
                case 8:
                    launchUrl("https://t.me/smartpack_kmanager", activity);
                    break;
                case 9:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/dev?id=5836199813143882901"));
                    activity.startActivity(intent);
                    break;
                case 10:
                    launchUrl("https://github.com/SmartPack/ScriptManager/issues/new", activity);
                    break;
                case 11:
                    Intent aboutView = new Intent(activity, AboutActivity.class);
                    activity.startActivity(aboutView);
                    break;
                case 12:
                    launchUrl("https://github.com/SmartPack/ScriptManager/tree/master/examples", activity);
                    break;
                case 13:
                    if (!getBoolean("use_el", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", true, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 14:
                    if (!getBoolean("use_pt", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", true, activity);
                        saveBoolean("use_ru", false, activity);
                        restartApp(activity);
                    }
                    break;
                case 15:
                    if (!getBoolean("use_ru", false, activity)) {
                        saveBoolean("use_en", false, activity);
                        saveBoolean("use_ko", false, activity);
                        saveBoolean("use_in", false, activity);
                        saveBoolean("use_am", false, activity);
                        saveBoolean("use_el", false, activity);
                        saveBoolean("use_pt", false, activity);
                        saveBoolean("use_ru", true, activity);
                        restartApp(activity);
                    }
                    break;
                case 16:
                    if (!getBoolean("theme_auto", true, activity)) {
                        saveBoolean("dark_theme", false, activity);
                        saveBoolean("light_theme", false, activity);
                        saveBoolean("theme_auto", true, activity);
                        restartApp(activity);
                    }
                    break;
                case 17:
                    if (!getBoolean("light_theme", false, activity)) {
                        saveBoolean("dark_theme", false, activity);
                        saveBoolean("light_theme", true, activity);
                        saveBoolean("theme_auto", false, activity);
                        restartApp(activity);
                    }
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    /*
     * The following code is partly taken from https://github.com/morogoku/MTweaks-KernelAdiutorMOD/
     * Ref: https://github.com/morogoku/MTweaks-KernelAdiutorMOD/blob/dd5a4c3242d5e1697d55c4cc6412a9b76c8b8e2e/app/src/main/java/com/moro/mtweaks/fragments/kernel/BoefflaWakelockFragment.java#L133
     */
    public static void WelcomeDialog(Activity activity) {
        View checkBoxView = View.inflate(activity, R.layout.rv_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        checkBox.setText(activity.getString(R.string.always_show));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                -> mWelcomeDialog = isChecked);

        new AlertDialog.Builder(Objects.requireNonNull(activity))
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(activity.getString(R.string.app_name))
                .setMessage(activity.getText(R.string.welcome_message))
                .setCancelable(false)
                .setView(checkBoxView)
                .setNeutralButton(activity.getString(R.string.examples), (dialog, id) -> {
                    launchUrl("https://github.com/SmartPack/ScriptManager/tree/master/examples", activity);
                })
                .setPositiveButton(activity.getString(R.string.got_it), (dialog, id)
                        -> saveBoolean("welcomeMessage", mWelcomeDialog, activity))

                .show();
    }

    static String readAssetFile(Context context, String file) {
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

    public static int getSpanCount(Activity activity) {
        int span = isTablet(activity) ? getOrientation(activity) ==
                Configuration.ORIENTATION_LANDSCAPE ? 4 : 3 : getOrientation(activity) ==
                Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        return span;
    }

    public static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static boolean languageDefault(Context context) {
        return !getBoolean("use_en", false, context)
                && !getBoolean("use_ko", false, context)
                && !getBoolean("use_in", false, context)
                && !getBoolean("use_am", false, context)
                && !getBoolean("use_el", false, context)
                && !getBoolean("use_pt", false, context)
                && !getBoolean("use_ru", false, context);
    }

    public static String getLang(Context context) {
        if (getBoolean("use_en", false, context)) {
            return  "en_US";
        } else if (getBoolean("use_ko", false, context)) {
            return  "ko";
        } else if (getBoolean("use_in", false, context)) {
            return  "in";
        } else if (getBoolean("use_am", false, context)) {
            return  "am";
        } else if (getBoolean("use_el", false, context)) {
            return "el";
        } else if (getBoolean("use_pt", false, context)) {
            return  "pt";
        } else if (getBoolean("use_ru", false, context)) {
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