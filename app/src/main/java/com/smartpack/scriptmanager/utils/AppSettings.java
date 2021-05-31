/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 13, 2020
 */
import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.scriptmanager.R;

public class AppSettings {

    public static void setAppTheme(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.app_theme), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (!Utils.getBoolean("theme_auto", true, context)) {
                        Utils.saveBoolean("dark_theme", false, context);
                        Utils.saveBoolean("light_theme", false, context);
                        Utils.saveBoolean("theme_auto", true, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 1:
                    if (!Utils.getBoolean("dark_theme", false, context)) {
                        Utils.saveBoolean("dark_theme", true, context);
                        Utils.saveBoolean("light_theme", false, context);
                        Utils.saveBoolean("theme_auto", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 2:
                    if (!Utils.getBoolean("light_theme", false, context)) {
                        Utils.saveBoolean("dark_theme", false, context);
                        Utils.saveBoolean("light_theme", true, context);
                        Utils.saveBoolean("theme_auto", false, context);
                        Utils.restartApp(context);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static String getAppThemeDescription(Context context) {
        if (Utils.getBoolean("dark_theme", false, context)) {
            return context.getString(R.string.dark_theme_enable);
        } else if (Utils.getBoolean("light_theme", false, context)) {
            return context.getString(R.string.dark_theme_disable);
        } else {
            return context.getString(R.string.dark_theme_auto);
        }
    }

    public static void setLanguage(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.app_language), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (!Utils.getLanguage(context).equals(java.util.Locale.getDefault().getLanguage())) {
                        Utils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
                        Utils.restartApp(context);
                    }
                    break;
                case 1:
                    if (!Utils.getLanguage(context).equals("en_US")) {
                        Utils.saveString("appLanguage", "en_US", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 2:
                    if (!Utils.getLanguage(context).equals("ko")) {
                        Utils.saveString("appLanguage", "ko", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 3:
                    if (!Utils.getLanguage(context).equals("am")) {
                        Utils.saveString("appLanguage", "am", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 4:
                    if (!Utils.getLanguage(context).equals("el")) {
                        Utils.saveString("appLanguage", "el", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 5:
                    if (!Utils.getLanguage(context).equals("in")) {
                        Utils.saveString("appLanguage", "in", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 6:
                    if (!Utils.getLanguage(context).equals("pt")) {
                        Utils.saveString("appLanguage", "pt", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 7:
                    if (!Utils.getLanguage(context).equals("ru")) {
                        Utils.saveString("appLanguage", "ru", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 8:
                    if (!Utils.getLanguage(context).equals("pl")) {
                        Utils.saveString("appLanguage", "pl", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 9:
                    if (!Utils.getLanguage(context).equals("zh")) {
                        Utils.saveString("appLanguage", "zh", context);
                        Utils.restartApp(context);
                    }
                    break;
                case 10:
                    if (!Utils.getLanguage(context).equals("uk")) {
                        Utils.saveString("appLanguage", "uk", context);
                        Utils.restartApp(context);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static String getLanguage(Context context) {
        switch (Utils.getLanguage(context)) {
            case "en_US":
                return context.getString(R.string.language_en);
            case "ko":
                return context.getString(R.string.language_ko);
            case "am":
                return context.getString(R.string.language_am);
            case "el":
                return context.getString(R.string.language_el);
            case "pt":
                return context.getString(R.string.language_pt);
            case "ru":
                return context.getString(R.string.language_ru);
            case "in":
                return context.getString(R.string.language_in);
            case "uk":
                return context.getString(R.string.language_uk);
            case "zh":
                return context.getString(R.string.language_zh);
            case "pl":
                return context.getString(R.string.language_pl);
            default:
                return context.getString(R.string.language_default) + " (" + java.util.Locale.getDefault().getLanguage() + ")";
        }
    }

}