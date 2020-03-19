/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils.root;
import androidx.annotation.NonNull;

import com.smartpack.scriptmanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class RootFile {

    private final String mFile;
    private RootUtils.SU mSU;

    public RootFile(String file) {
        mFile = file;
        mSU = RootUtils.getSU();
    }

    public RootFile(String file, RootUtils.SU su) {
        mFile = file;
        mSU = su;
    }

    public String getName() {
        return new File(mFile).getName();
    }

    public void mkdir() {
        mSU.runCommand("mkdir -p '" + mFile + "'");
    }

    public void write(String text, boolean append) {
        String[] array = text.split("\\r?\\n");
        if (!append) mSU.runCommand("rm -r '" + mFile + "'");
        for (String line : array) {
            mSU.runCommand("echo '" + line + "' >> " + mFile);
        }
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        String files = mSU.runCommand("ls '" + mFile + "/'");
        if (files != null) {
            // Make sure the files exists
            for (String file : files.split("\\r?\\n")) {
                if (file != null && !file.isEmpty() && Utils.existFile(mFile + "/" + file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public boolean exists() {
        String output = mSU.runCommand("[ -e " + mFile + " ] && echo true");
        return output != null && output.equals("true");
    }

    public String readFile() {
        return mSU.runCommand("cat '" + mFile + "'");
    }

    @Override
    @NonNull
    public String toString() {
        return mFile;
    }
}