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
import android.content.Intent;
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
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.ApplyScriptActivity;
import com.smartpack.scriptmanager.utils.EditScriptActivity;
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
                            if (isAdded()) {
                                clearItems();
                                for (RecyclerViewItem item : recyclerViewItems) {
                                    addItem(item);
                                }

                                hideProgress();
                                mLoader = null;
                            }
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    private void load(List<RecyclerViewItem> items) {
        if (!Scripts.ScriptFile().exists()) {
            return;
        }
        for (final String scriptsItems : Scripts.scriptItems()) {
        File scripts = new File(Scripts.ScriptFile() + "/" + scriptsItems);
            if (Scripts.ScriptFile().length() > 0 && Scripts.isScript(scripts.toString())) {
                DescriptionView script = new DescriptionView();
                script.setDrawable(Utils.getColoredIcon(R.drawable.ic_shell, requireActivity()));
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
                                                        Utils.snackbar(getRootView(), getString(R.string.wrong_script, scripts.getName().replace(".sh", "")));
                                                        return;
                                                    }
                                                    new AsyncTask<Void, Void, Void>() {
                                                        @Override
                                                        protected void onPreExecute() {
                                                            super.onPreExecute();
                                                            Scripts.mApplyingScript = true;
                                                            if (Scripts.mOutput == null) {
                                                                Scripts.mOutput = new StringBuilder();
                                                            } else {
                                                                Scripts.mOutput.setLength(0);
                                                            }
                                                            Scripts.mOutput.append("Executing ").append(scripts.getName()).append("... Please be patient!\n\n");
                                                            Intent applyIntent = new Intent(getActivity(), ApplyScriptActivity.class);
                                                            applyIntent.putExtra(ApplyScriptActivity.TITLE_INTENT, scripts.getName());
                                                            startActivityForResult(applyIntent, 3);
                                                        }
                                                        @Override
                                                        protected Void doInBackground(Void... voids) {
                                                            Scripts.mOutput.append(Scripts.applyScript(scripts.toString()));
                                                            return null;
                                                        }

                                                        @Override
                                                        protected void onPostExecute(Void aVoid) {
                                                            super.onPostExecute(aVoid);
                                                            Scripts.mApplyingScript = false;
                                                        }
                                                    }.execute();
                                                })
                                                .show();
                                        break;
                                    case 1:
                                        if (Scripts.mOutput == null) {
                                            Scripts.mOutput = new StringBuilder();
                                        } else {
                                            Scripts.mOutput.setLength(0);
                                        }
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
                                            Utils.snackbar(getRootView(), getString(R.string.on_boot_message, scripts.getName()));
                                        } else {
                                            Scripts.setScriptOnPostFS(scripts.toString(), scripts.getName(), getActivity());
                                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + scripts.getName());
                                            Utils.snackbar(getRootView(), getString(R.string.post_fs_message, scripts.getName()));
                                        }
                                        reload();
                                        break;
                                    case 7:
                                        if (Scripts.isMgiskService() && Scripts.scriptOnLateBoot(scripts.getName())) {
                                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + scripts.getName());
                                            Utils.snackbar(getRootView(), getString(R.string.on_boot_message, scripts.getName()));
                                        } else {
                                            Scripts.setScriptOnServiceD(scripts.toString(), scripts.getName(), getActivity());
                                            Utils.delete(Scripts.MagiskPostFSFile().toString() + "/" + scripts.getName());
                                            Utils.snackbar(getRootView(), getString(R.string.late_start_message, scripts.getName()));
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
            info.setDrawable(Utils.getColoredIcon(R.drawable.ic_info, requireActivity()));
            info.setSummary(getText(R.string.empty_message));
            info.setFullSpan(true);
            info.setOnItemClickListener(item -> {
                if (!Utils.checkWriteStoragePermission(requireActivity())) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    Utils.snackbar(getRootView(), getString(R.string.permission_denied_write_storage));
                    return;
                }

                showOptions();
            });

            items.add(info);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;
        if (requestCode == 0) {
            Scripts.createScript(mEditScript, Objects.requireNonNull(data.getCharSequenceExtra(EditScriptActivity.TEXT_INTENT)).toString(), getActivity());
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
                Utils.snackbar(getRootView(), getString(R.string.wrong_extension, ".sh"));
                return;
            }
            if (!Scripts.isScript(mPath)) {
                Utils.snackbar(getRootView(), getString(R.string.wrong_script, file.getName().replace(".sh", "")));
                return;
            }
            if (Utils.existFile(Scripts.scriptExistsCheck(file.getName()))) {
                Utils.snackbar(getRootView(), getString(R.string.script_exists, file.getName()));
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
            Scripts.createScript(mCreateName, Objects.requireNonNull(data.getCharSequenceExtra(EditScriptActivity.TEXT_INTENT)).toString(), getActivity());
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
            Utils.snackbar(getRootView(), getString(R.string.permission_denied_write_storage));
            return;
        }

        showOptions();
    }

    private void showOptions() {
        mOptionsDialog = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.script_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (Scripts.mOutput == null) {
                                Scripts.mOutput = new StringBuilder();
                            } else {
                                Scripts.mOutput.setLength(0);
                            }
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
        Intent intent = new Intent(getActivity(), EditScriptActivity.class);
        intent.putExtra(EditScriptActivity.TITLE_INTENT, name);
        intent.putExtra(EditScriptActivity.TEXT_INTENT, Scripts.readScript(string));
        startActivityForResult(intent, 0);
    }

    private void showCreateDialog() {
        mShowCreateNameDialog = true;
        ViewUtils.dialogEditText("",
                (dialogInterface, i) -> {
                }, text -> {
                    if (text.isEmpty()) {
                        Utils.snackbar(getRootView(), getString(R.string.name_empty));
                        return;
                    }
                    if (!text.endsWith(".sh")) {
                        text += ".sh";
                    }
                    if (text.contains(" ")) {
                        text = text.replace(" ", "_");
                    }
                    if (Utils.existFile(Scripts.scriptExistsCheck(text))) {
                        Utils.snackbar(getRootView(), getString(R.string.script_exists, text));
                        return;
                    }
                    mCreateName = Utils.getInternalDataStorage() + "/" + text;
                    Intent intent = new Intent(getActivity(), EditScriptActivity.class);
                    intent.putExtra(EditScriptActivity.TITLE_INTENT, text);
                    intent.putExtra(EditScriptActivity.TEXT_INTENT, "#!/system/bin/sh\n\n");
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