/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.smartpack.scriptmanager.BuildConfig;
import com.smartpack.scriptmanager.MainActivity;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.EditorActivity;
import com.smartpack.scriptmanager.utils.Prefs;
import com.smartpack.scriptmanager.utils.Scripts;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.ViewUtils;
import com.smartpack.scriptmanager.views.dialog.Dialog;
import com.smartpack.scriptmanager.views.recyclerview.DescriptionView;
import com.smartpack.scriptmanager.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class ScriptsFragment extends RecyclerViewFragment {

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private boolean mShowCreateNameDialog;

    private Dialog mOptionsDialog;

    private String mCreateName;
    private String mEditScript;
    private String mPath;

    @Override
    protected Drawable getBottomFabDrawable() {
        return getResources().getDrawable(R.drawable.ic_add);
    }

    @Override
    protected boolean showBottomFab() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        if (mOptionsDialog != null) {
            mOptionsDialog.show();
        }
        if (mShowCreateNameDialog) {
            showCreateDialog();
        }
    }

    @Override
    public int getSpanCount() {
        int span = Utils.isTablet(requireActivity()) ? Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 4 : 3 : Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        if (itemsSize() != 0 && span > itemsSize()) {
            span = itemsSize();
        }
        return span;
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        if (Utils.checkWriteStoragePermission(requireActivity())) {
            reload();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    private void reload() {
        if (mLoader == null) {
            getHandler().postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    clearItems();
                    mLoader = new AsyncTask<Void, Void, List<RecyclerViewItem>>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgress();
                        }

                        @Override
                        protected List<RecyclerViewItem> doInBackground(Void... voids) {
                            List<RecyclerViewItem> items = new ArrayList<>();
                            load(items);
                            return items;
                        }

                        @Override
                        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            for (RecyclerViewItem item : recyclerViewItems) {
                                addItem(item);
                            }
                            hideProgress();
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    private void load(List<RecyclerViewItem> items) {
        DescriptionView options = new DescriptionView();
        options.setTitle(getString(R.string.app_name) + " " + getString(R.string.settings));
        options.setMenuIcon(getResources().getDrawable(R.drawable.ic_settings));
        options.setFullSpan(true);
        options.setOnMenuListener((optionsMenu, popupMenu) -> {
            Menu menu = popupMenu.getMenu();
            if (!Utils.isNotDonated(requireActivity())) {
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                        .setChecked(Prefs.getBoolean("allow_ads", true, getActivity()));
            }
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true).setChecked(
                    Prefs.getBoolean("dark_theme", true, getActivity()));
            String lang;
            if (Prefs.getBoolean("use_en", false, getActivity())) {
                lang = "en_US";
            } else if (Prefs.getBoolean("use_ko", false, getActivity())) {
                lang = "ko";
            } else if (Prefs.getBoolean("use_in", false, getActivity())) {
                lang = "in";
            } else if (Prefs.getBoolean("use_am", false, getActivity())) {
                lang = "am";
            } else {
                lang = java.util.Locale.getDefault().getLanguage();
            }
            SubMenu language = menu.addSubMenu(Menu.NONE, 2, Menu.NONE, getString(R.string.language, lang));
            language.add(Menu.NONE, 3, Menu.NONE, getString(R.string.language_default)).setCheckable(true).setChecked(
                    Utils.languageDefault(getActivity()));
            language.add(Menu.NONE, 4, Menu.NONE, getString(R.string.language_en)).setCheckable(true).setChecked(
                    Prefs.getBoolean("use_en", false, getActivity()));
            language.add(Menu.NONE, 5, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_ko", false, getActivity()));
            language.add(Menu.NONE, 6, Menu.NONE, getString(R.string.language_in)).setCheckable(true).setChecked(
                    Prefs.getBoolean("use_in", false, getActivity()));
            language.add(Menu.NONE, 7, Menu.NONE, getString(R.string.language_am)).setCheckable(true).setChecked(
                    Prefs.getBoolean("use_am", false, getActivity()));
            SubMenu about = menu.addSubMenu(Menu.NONE, 2, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.support_group));
            about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.more_apps));
            about.add(Menu.NONE, 11, Menu.NONE, getString(R.string.report_issue));
            about.add(Menu.NONE, 12, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        if (Prefs.getBoolean("allow_ads", true, getActivity())) {
                            Prefs.saveBoolean("allow_ads", false, getActivity());
                        } else {
                            Prefs.saveBoolean("allow_ads", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 1:
                        if (Prefs.getBoolean("dark_theme", true, getActivity())) {
                            Prefs.saveBoolean("dark_theme", false, getActivity());
                        } else {
                            Prefs.saveBoolean("dark_theme", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 2:
                        break;
                    case 3:
                        if (!Utils.languageDefault(getActivity())) {
                            Prefs.saveBoolean("use_en", false, getActivity());
                            Prefs.saveBoolean("use_ko", false, getActivity());
                            Prefs.saveBoolean("use_in", false, getActivity());
                            Prefs.saveBoolean("use_am", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 4:
                        if (!Prefs.getBoolean("use_en", false, getActivity())) {
                            Prefs.saveBoolean("use_en", true, getActivity());
                            Prefs.saveBoolean("use_ko", false, getActivity());
                            Prefs.saveBoolean("use_in", false, getActivity());
                            Prefs.saveBoolean("use_am", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 5:
                        if (!Prefs.getBoolean("use_ko", false, getActivity())) {
                            Prefs.saveBoolean("use_en", false, getActivity());
                            Prefs.saveBoolean("use_ko", true, getActivity());
                            Prefs.saveBoolean("use_in", false, getActivity());
                            Prefs.saveBoolean("use_am", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 6:
                        if (!Prefs.getBoolean("use_in", false, getActivity())) {
                            Prefs.saveBoolean("use_en", false, getActivity());
                            Prefs.saveBoolean("use_ko", false, getActivity());
                            Prefs.saveBoolean("use_in", true, getActivity());
                            Prefs.saveBoolean("use_am", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 7:
                        if (!Prefs.getBoolean("use_am", false, getActivity())) {
                            Prefs.saveBoolean("use_en", false, getActivity());
                            Prefs.saveBoolean("use_ko", false, getActivity());
                            Prefs.saveBoolean("use_in", false, getActivity());
                            Prefs.saveBoolean("use_am", true, getActivity());
                            restartApp();
                        }
                        break;
                    case 8:
                        Utils.launchUrl("https://github.com/SmartPack/ScriptManager", getActivity());
                        break;
                    case 9:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", getActivity());
                        break;
                    case 10:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(
                                "https://play.google.com/store/apps/developer?id=sunilpaulmathew"));
                        startActivity(intent);
                        break;
                    case 11:
                        Utils.launchUrl("https://github.com/SmartPack/ScriptManager/issues/new", getActivity());
                        break;
                    case 12:
                        aboutDialogue();
                        break;
                }
                return false;
            });
        });
        items.add(options);

        if (!Scripts.ScriptFile().exists()) {
            return;
        }
        for (final String scriptsItems : Scripts.scriptItems()) {
        File scripts = new File(Scripts.ScriptFile() + "/" + scriptsItems);
            if (Scripts.ScriptFile().length() > 0 && Scripts.isScript(scripts.toString())) {
                DescriptionView script = new DescriptionView();
                script.setDrawable(getResources().getDrawable(R.drawable.ic_shell));
                script.setMenuIcon(getResources().getDrawable(R.drawable.ic_dots));
                script.setTitle(scripts.getName().replace(".sh", ""));
                script.setOnMenuListener(new DescriptionView.OnMenuListener() {
                    @Override
                    public void onMenuReady(DescriptionView script, PopupMenu popupMenu) {
                        Menu menu = popupMenu.getMenu();
                        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.apply));
                        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.edit));
                        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.details));
                        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.share));
                        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.delete));
                        if (Scripts.isMgiskService()) {
                            SubMenu onBoot = menu.addSubMenu(Menu.NONE, 5, Menu.NONE, getString(R.string.apply_on_boot));
                            onBoot.add(Menu.NONE, 6, Menu.NONE, getString(R.string.post_fs)).setCheckable(true)
                                    .setChecked(Scripts.scriptOnPostBoot(scripts.getName()));
                            onBoot.add(Menu.NONE, 7, Menu.NONE, getString(R.string.late_start)).setCheckable(true)
                                    .setChecked(Scripts.scriptOnLateBoot(scripts.getName()));
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @SuppressLint({"StaticFieldLeak", "StringFormatInvalid"})
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case 0:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.apply_question, scripts.getName().replace(".sh", "")))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    if (!Scripts.isScript(scripts.toString())) {
                                                        Utils.toast(getString(R.string.wrong_script, scripts.getName().replace(".sh", "")), getActivity());
                                                        return;
                                                    }
                                                    new AsyncTask<Void, Void, String>() {
                                                        private ProgressDialog mProgressDialog;
                                                        @Override
                                                        protected void onPreExecute() {
                                                            super.onPreExecute();

                                                            mProgressDialog = new ProgressDialog(getActivity());
                                                            mProgressDialog.setMessage(getString(R.string.applying_script, scripts.getName().replace(".sh", "") + "..."));
                                                            mProgressDialog.setCancelable(false);
                                                            mProgressDialog.show();
                                                        }

                                                        @Override
                                                        protected String doInBackground(Void... voids) {
                                                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                                            return Scripts.applyScript(scripts.toString());
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String s) {
                                                            super.onPostExecute(s);
                                                            try {
                                                                mProgressDialog.dismiss();
                                                            } catch (IllegalArgumentException ignored) {
                                                            }
                                                            if (s != null && !s.isEmpty()) {
                                                                new Dialog(requireActivity())
                                                                        .setMessage(s)
                                                                        .setCancelable(false)
                                                                        .setPositiveButton(getString(R.string.cancel), (dialog, id) -> {
                                                                        })
                                                                        .show();
                                                            }
                                                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                        }
                                                    }.execute();
                                                })
                                                .show();
                                        break;
                                    case 1:
                                        if (Scripts.isMgiskService() && (Scripts.scriptOnPostBoot(scripts.getName())
                                                || Scripts.scriptOnLateBoot(scripts.getName()))) {
                                            Dialog onbootwarning = new Dialog(requireActivity());
                                            onbootwarning.setMessage(getString(R.string.on_boot_warning, scripts.getName()));
                                            onbootwarning.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                            });
                                            onbootwarning.setPositiveButton(getString(R.string.edit_anyway), (dialogInterface, i) -> {
                                                showEditDialog(scripts.toString(), scripts.getName());
                                            });
                                            onbootwarning.show();
                                        } else {
                                            showEditDialog(scripts.toString(), scripts.getName());
                                        }
                                        break;
                                    case 2:
                                        new Dialog(requireActivity())
                                                .setTitle(scripts.getName().replace(".sh", ""))
                                                .setMessage(Scripts.readScript(scripts.toString()))
                                                .setPositiveButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .show();
                                        break;
                                    case 3:
                                        Uri uriFile = FileProvider.getUriForFile(requireActivity(),
                                                BuildConfig.APPLICATION_ID + ".provider", new File(scripts.toString()));
                                        Intent shareScript = new Intent(Intent.ACTION_SEND);
                                        shareScript.setType("application/sh");
                                        shareScript.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shared_by, scripts.getName()));
                                        shareScript.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message,BuildConfig.VERSION_NAME));
                                        shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                                        shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivity(Intent.createChooser(shareScript, getString(R.string.share_with)));
                                        break;
                                    case 4:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.sure_question, scripts.getName().replace(".sh", "")))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    Scripts.deleteScript(scripts.toString(), getActivity());
                                                    reload();
                                                })
                                                .show();
                                        break;
                                    case 5:
                                        break;
                                    case 6:
                                        if (Scripts.isMgiskService() && Scripts.scriptOnPostBoot(scripts.getName())) {
                                            Utils.delete(Scripts.MagiskPostFSFile().toString() + "/" + scripts.getName());
                                            Utils.toast(getString(R.string.on_boot_message, scripts.getName()), getActivity());
                                        } else {
                                            Scripts.setScriptOnPostFS(scripts.toString(), scripts.getName(), getActivity());
                                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + scripts.getName());
                                            Utils.toast(getString(R.string.post_fs_message, scripts.getName()), getActivity());
                                        }
                                        reload();
                                        break;
                                    case 7:
                                        if (Scripts.isMgiskService() && Scripts.scriptOnLateBoot(scripts.getName())) {
                                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + scripts.getName());
                                            Utils.toast(getString(R.string.on_boot_message, scripts.getName()), getActivity());
                                        } else {
                                            Scripts.setScriptOnServiceD(scripts.toString(), scripts.getName(), getActivity());
                                            Utils.delete(Scripts.MagiskPostFSFile().toString() + "/" + scripts.getName());
                                            Utils.toast(getString(R.string.late_start_message, scripts.getName()), getActivity());
                                        }
                                        reload();
                                        break;
                                }
                                return false;
                            }
                        });
                    }
                });

                items.add(script);
            }
        }
        if (items.size() == 0) {
            DescriptionView info = new DescriptionView();
            info.setDrawable(getResources().getDrawable(R.drawable.ic_info));
            info.setSummary(getText(R.string.empty_message));
            info.setOnItemClickListener(item -> {
                if (!Utils.checkWriteStoragePermission(requireActivity())) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    Utils.toast(R.string.permission_denied_write_storage, getActivity());
                    return;
                }

                showOptions();
            });

            items.add(info);
        }
    }

    private void aboutDialogue() {
        new Dialog(requireActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.credits_summary))
                .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .show();
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;
        if (requestCode == 0) {
            Scripts.createScript(mEditScript, Objects.requireNonNull(data.getCharSequenceExtra(EditorActivity.TEXT_INTENT)).toString(), getActivity());
            reload();
        } else if (requestCode == 1) {
            Uri uri = data.getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (Utils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mPath = Environment.getExternalStorageDirectory().toString() + "/Download/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = Utils.getPath(file);
            }
            if (!Utils.getExtension(mPath).equals("sh")) {
                Utils.toast(getString(R.string.wrong_extension, ".sh"), getActivity());
                return;
            }
            if (!Scripts.isScript(mPath)) {
                Utils.toast(getString(R.string.wrong_script, file.getName().replace(".sh", "")), getActivity());
                return;
            }
            if (Utils.existFile(Scripts.scriptExistsCheck(file.getName()))) {
                Utils.toast(getString(R.string.script_exists, file.getName()), getActivity());
                return;
            }
            Dialog selectQuestion = new Dialog(requireActivity());
            selectQuestion.setMessage(getString(R.string.select_question, file.getName().replace("primary:", "")
                    .replace("file%3A%2F%2F%2F", "").replace("%2F", "/")));
            selectQuestion.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
            });
            selectQuestion.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                Scripts.importScript(mPath, getActivity());
                reload();
            });
            selectQuestion.show();
        } else if (requestCode == 2) {
            Scripts.createScript(mCreateName, Objects.requireNonNull(data.getCharSequenceExtra(EditorActivity.TEXT_INTENT)).toString(), getActivity());
            mCreateName = null;
            reload();
        }
    }

    @Override
    protected void onBottomFabClick() {
        super.onBottomFabClick();

        if (!Utils.checkWriteStoragePermission(requireActivity())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }

        showOptions();
    }

    private void showOptions() {
        mOptionsDialog = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.script_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            showCreateDialog();
                            break;
                        case 1:
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            startActivityForResult(intent, 1);
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> mOptionsDialog = null);
        mOptionsDialog.show();
    }

    private void showEditDialog(String string, String name) {
        mEditScript = string;
        Intent intent = new Intent(getActivity(), EditorActivity.class);
        intent.putExtra(EditorActivity.TITLE_INTENT, name);
        intent.putExtra(EditorActivity.TEXT_INTENT, Scripts.readScript(string));
        startActivityForResult(intent, 0);
    }

    private void showCreateDialog() {
        mShowCreateNameDialog = true;
        ViewUtils.dialogEditText("",
                (dialogInterface, i) -> {
                }, text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }
                    if (!text.endsWith(".sh")) {
                        text += ".sh";
                    }
                    if (text.contains(" ")) {
                        text = text.replace(" ", "_");
                    }
                    if (Utils.existFile(Scripts.scriptExistsCheck(text))) {
                        Utils.toast(getString(R.string.script_exists, text), getActivity());
                        return;
                    }
                    mCreateName = Utils.getInternalDataStorage() + "/" + text;
                    Intent intent = new Intent(getActivity(), EditorActivity.class);
                    intent.putExtra(EditorActivity.TITLE_INTENT, text);
                    intent.putExtra(EditorActivity.TEXT_INTENT, "#!/system/bin/sh\n\n");
                    startActivityForResult(intent, 2);
                }, getActivity()).setOnDismissListener(dialogInterface -> mShowCreateNameDialog = false).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoader != null) {
            mLoader.cancel(true);
        }
    }

}