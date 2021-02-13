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
                    if (!Utils.languageDefault(context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 1:
                    if (!Utils.getBoolean("use_en", false, context)) {
                        Utils.saveBoolean("use_en", true, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 2:
                    if (!Utils.getBoolean("use_ko", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", true, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 3:
                    if (!Utils.getBoolean("use_am", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", true, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 4:
                    if (!Utils.getBoolean("use_el", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", true, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 5:
                    if (!Utils.getBoolean("use_in", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", true, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 6:
                    if (!Utils.getBoolean("use_pt", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", true, context);
                        Utils.saveBoolean("use_ru", false, context);
                        Utils.restartApp(context);
                    }
                    break;
                case 7:
                    if (!Utils.getBoolean("use_ru", false, context)) {
                        Utils.saveBoolean("use_en", false, context);
                        Utils.saveBoolean("use_ko", false, context);
                        Utils.saveBoolean("use_in", false, context);
                        Utils.saveBoolean("use_am", false, context);
                        Utils.saveBoolean("use_el", false, context);
                        Utils.saveBoolean("use_pt", false, context);
                        Utils.saveBoolean("use_ru", true, context);
                        Utils.restartApp(context);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static String getLanguage(Context context) {
        if (Utils.getBoolean("use_english", false, context)) {
            return context.getString(R.string.language_en);
        } else if (Utils.getBoolean("use_korean", false, context)) {
            return context.getString(R.string.language_ko);
        } else if (Utils.getBoolean("use_am", false, context)) {
            return context.getString(R.string.language_am);
        } else if (Utils.getBoolean("use_el", false, context)) {
            return context.getString(R.string.language_el);
        }else if (Utils.getBoolean("use_in", false, context)) {
            return context.getString(R.string.language_in);
        } else if (Utils.getBoolean("use_pt", false, context)) {
            return context.getString(R.string.language_pt);
        } else if (Utils.getBoolean("use_ru", false, context)) {
            return context.getString(R.string.language_ru);
        } else {
            return context.getString(R.string.language_default);
        }
    }

}