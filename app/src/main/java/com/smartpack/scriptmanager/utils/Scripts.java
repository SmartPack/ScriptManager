/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import com.smartpack.scriptmanager.utils.root.RootFile;
import com.smartpack.scriptmanager.utils.root.RootUtils;

import java.io.File;
import java.util.List;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class Scripts {

    private static final String SCRIPTS = Utils.getInternalDataStorage();
    private static final String MAGISK_SERVICED = "/data/adb/service.d";
    private static final String MAGISK_POSTFS = "/data/adb/post-fs-data.d";

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
        RootFile file = new RootFile(ScriptFile().toString());
        if (!file.exists()) {
            file.mkdir();
        }
        return file.list();
    }

    public static void makeScriptFolder() {
        if (ScriptFile().exists() && ScriptFile().isFile()) {
            ScriptFile().delete();
        }
        ScriptFile().mkdirs();
    }

    public static void importScript(String string) {
        makeScriptFolder();
        RootUtils.runCommand("cp " + string + " " + SCRIPTS);
    }

    public static void createScript(String file, String text) {
        makeScriptFolder();
        RootFile f = new RootFile(file);
        f.write(text, false);
    }

    public static void deleteScript(String path) {
        File file = new File(path);
        file.delete();
        if (Utils.existFile(MagiskServiceFile() + "/" + file.getName())) {
            RootUtils.runCommand("rm -r " + MagiskServiceFile() + "/" + file.getName());
        }
    }

    public static String applyScript(String file) {
        RootUtils.runCommand("sleep 3");
        return RootUtils.runCommand("sh " + file);
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

    public static boolean isMgiskService() {
        return Utils.existFile(MAGISK_SERVICED) ||
                Utils.existFile(MAGISK_POSTFS);
    }

    public static void setScriptOnServiceD(String string, String name) {
        Utils.copy(string, MAGISK_SERVICED);
        Utils.chmod("755", MAGISK_SERVICED + "/" + name);
    }

    public static void setScriptOnPostFS(String string, String name) {
        Utils.copy(string, MAGISK_POSTFS);
        Utils.chmod("755", MAGISK_POSTFS + "/" + name);
    }

    public static boolean scriptOnBoot(String path) {
        return Utils.existFile(MAGISK_SERVICED + "/" + path) ||
                Utils.existFile(MAGISK_POSTFS + "/" + path);
    }

}