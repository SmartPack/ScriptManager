/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.Scripts;
import com.smartpack.scriptmanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 05, 2020
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private List<String> data;

    public RecycleViewAdapter (List<String> data){
        this.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
        holder.scriptTitle.setText(this.data.get(position));
        if (Utils.isDarkTheme(holder.scriptIcon.getContext())) {
            holder.scriptIcon.setColorFilter(Utils.getThemeAccentColor(holder.scriptIcon.getContext()));
            holder.scriptTitle.setTextColor(Utils.getThemeAccentColor(holder.scriptIcon.getContext()));
            holder.onBootIcon.setColorFilter(Color.WHITE);
        }
        if (Scripts.isMgiskServiceD() && Scripts.scriptOnLateBoot(holder.scriptTitle.getText().toString())
                || Scripts.isMgiskPostFS() && Scripts.scriptOnPostBoot(holder.scriptTitle.getText().toString())) {
            holder.onBootIcon.setVisibility(View.VISIBLE);
        } else {
            holder.onBootIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppCompatImageButton scriptIcon, onBootIcon;
        private MaterialTextView scriptTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.scriptIcon = view.findViewById(R.id.script_icon);
            this.scriptTitle = view.findViewById(R.id.script_title);
            this.onBootIcon = view.findViewById(R.id.onboot_icon);
        }

        @Override
        public void onClick(View view) {
            Scripts.mScriptName = this.scriptTitle.getText().toString();
            Scripts.mScriptPath = Scripts.ScriptFile() + "/" + Scripts.mScriptName + ".sh";
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.apply);
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.edit);
            menu.add(Menu.NONE, 2, Menu.NONE, R.string.share);
            menu.add(Menu.NONE, 3, Menu.NONE, R.string.delete);
            if (Utils.rootAccess()) {
                SubMenu onBoot = menu.addSubMenu(Menu.NONE, 4, Menu.NONE, R.string.apply_on_boot);
                if (Scripts.isMgiskPostFS()) {
                    onBoot.add(Menu.NONE, 5, Menu.NONE, R.string.post_fs).setCheckable(true)
                            .setChecked(Scripts.scriptOnPostBoot(Scripts.mScriptName));
                }
                if (Scripts.isMgiskServiceD()) {
                    onBoot.add(Menu.NONE, 6, Menu.NONE, R.string.late_start).setCheckable(true)
                            .setChecked(Scripts.scriptOnLateBoot(Scripts.mScriptName));
                }
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setMessage(view.getContext().getString(R.string.apply_question, Scripts.mScriptName.replace(".sh", "")))
                                .setNegativeButton(R.string.cancel, (dialogInterfacei, ii) -> {
                                })
                                .setPositiveButton(R.string.yes, (dialogInterfacei, ii) -> {
                                    if (!Scripts.isScript(Scripts.mScriptPath)) {
                                        Utils.snackbar(view, view.getContext().getString(R.string.wrong_script, Scripts.mScriptName.replace(".sh", "")));
                                        return;
                                    }
                                    Scripts.applyScript(view.getContext());
                                })
                                .show();
                        break;
                    case 1:
                        Scripts.createScript(view.getContext());
                        break;
                    case 2:
                        Scripts.shareScript(view.getContext());
                        break;
                    case 3:
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setMessage(view.getContext().getString(R.string.sure_question, Scripts.mScriptName.replace(".sh", "")))
                                .setNegativeButton(R.string.cancel, (dialogInterfacei, ii) -> {
                                })
                                .setPositiveButton(R.string.yes, (dialogInterfacei, ii) -> {
                                    Scripts.deleteScript(Scripts.mScriptPath);
                                    data.remove(getLayoutPosition());
                                    notifyDataSetChanged();
                                })
                                .show();
                        break;
                    case 4:
                        break;
                    case 5:
                        if (Scripts.isMgiskPostFS() && Scripts.scriptOnPostBoot(Scripts.mScriptName)) {
                            Utils.delete(Scripts.MagiskPostFSFile().toString() + "/" + Scripts.mScriptName + ".sh");
                            Utils.snackbar(view, view.getContext().getString(R.string.on_boot_message, Scripts.mScriptName));
                        } else {
                            Scripts.setScriptOnPostFS(Scripts.mScriptPath, Scripts.mScriptName);
                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + Scripts.mScriptName + ".sh");
                            Utils.snackbar(view, view.getContext().getString(R.string.post_fs_message, Scripts.mScriptName));
                        }
                        notifyItemChanged(getLayoutPosition());
                        notifyDataSetChanged();
                        break;
                    case 6:
                        if (Scripts.isMgiskServiceD() && Scripts.scriptOnLateBoot(Scripts.mScriptName)) {
                            Utils.delete(Scripts.MagiskServiceFile().toString() + "/" + Scripts.mScriptName + ".sh");
                            Utils.snackbar(view, view.getContext().getString(R.string.on_boot_message, Scripts.mScriptName));
                        } else {
                            Scripts.setScriptOnServiceD(Scripts.mScriptPath, Scripts.mScriptName);
                            Utils.delete(Scripts.MagiskPostFSFile().toString() + "/" + Scripts.mScriptName + ".sh");
                            Utils.snackbar(view, view.getContext().getString(R.string.late_start_message, Scripts.mScriptName));
                        }
                        notifyItemChanged(getLayoutPosition());
                        notifyDataSetChanged();
                        break;
                }
                return false;
            });
            popupMenu.show();
        }
    }

}