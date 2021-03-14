/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.Scripts;
import com.smartpack.scriptmanager.utils.Utils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 02, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private AppCompatImageButton mPathIcon;
    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;
    private String mPath = Environment.getExternalStorageDirectory().toString() + "/Download";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mPathIcon = findViewById(R.id.path);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(mPath.equals("/storage/emulated/0/") ? getString(R.string.path_sdcard) : new File(mPath).getName());

        mBack.setOnClickListener(v -> super.onBackPressed());
        mPathIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mPathIcon);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.path_sdcard);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    mPath = Environment.getExternalStorageDirectory().toString();
                    reload();
                }
                return false;
            });
            popupMenu.show();
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                mPath = mData.get(position);
                reload();
            } else {
                if (!Utils.getExtension(mData.get(position)).equals("sh")) {
                    Utils.snackbar(findViewById(android.R.id.content), getString(R.string.wrong_extension, ".sh"));
                    return;
                }
                if (!Scripts.isScript(mData.get(position))) {
                    Utils.snackbar(findViewById(android.R.id.content), getString(R.string.wrong_script, new File(mData.get(position)).getName()));
                    return;
                }
                if (Utils.exist(Scripts.scriptExistsCheck(new File(mData.get(position)).getName()))) {
                    Utils.snackbar(findViewById(android.R.id.content), getString(R.string.script_exists, new File(mData.get(position)).getName()));
                    return;
                }
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.select_question, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                            Scripts.importScript(mData.get(position));
                            Scripts.reloadUI();
                            finish();
                        }).show();
            }
        });
    }

    private int getSpanCount(Activity activity) {
        return Utils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    private File[] getFilesList() {
        if (!mPath.endsWith(File.separator)) {
            mPath = mPath + File.separator;
        }
        if (!new File(mPath).exists()) {
            mPath = Environment.getExternalStorageDirectory().toString();
        }
        return new File(mPath).listFiles();
    }

    private List<String> getData() {
        try {
            mData.clear();
            // Add directories
            for (File mFile : getFilesList()) {
                if (mFile.isDirectory()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
            // Add files
            for (File mFile : getFilesList()) {
                if (mFile.isFile()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        } catch (NullPointerException ignored) {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.file_picker_failed_message));
        }
        return mData;
    }

    private static String getSize(File file) {
        long size = file.length() / 1024;
        String timeCreated = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
        if (size < 1024) {
            return timeCreated + ", " + size + " kB";
        } else {
            return timeCreated + ", " + size / 1024 + " MB";
        }
    }

    private void reload() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mData.clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mTitle.setText(mPath.equals("/storage/emulated/0/") ? getString(R.string.path_sdcard)
                                    : new File(mPath).getName());
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.snackbar(findViewById(android.R.id.content), getString(R.string.use_file_picker_message));
    }

    @Override
    public void onBackPressed() {
        if (mPath.equals("/storage/emulated/0/")) {
            super.onBackPressed();
        } else {
            mPath = Objects.requireNonNull(new File(mPath).getParentFile()).getPath();
            reload();
        }
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static ClickListener clickListener;

        private List<String> data;

        public RecycleViewAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            if (new File(this.data.get(position)).isDirectory()) {
                holder.mIcon.setImageDrawable(holder.mTitle.getContext().getResources().getDrawable(R.drawable.ic_folder));
                holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mTitle.getContext()));
                holder.mDescription.setVisibility(View.GONE);
            } else {
                if (Utils.getExtension(this.data.get(position)).equals("sh")) {
                    holder.mDescription.setVisibility(View.VISIBLE);
                    holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_script));
                    holder.mLinearLayout.setOnLongClickListener(v -> {
                        Scripts.mScriptName = new File(this.data.get(position)).getName();
                        Scripts.mScriptPath = this.data.get(position);
                        Scripts.createScript(holder.mLinearLayout.getContext());
                        return false;
                    });
                } else {
                    holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file));
                }
                holder.mIcon.setColorFilter(Utils.isDarkTheme(holder.mIcon.getContext()) ? holder.mIcon.getContext()
                        .getResources().getColor(R.color.white) : holder.mIcon.getContext().getResources().getColor(R.color.black));
                holder.mDescription.setText(getSize(new File(this.data.get(position))));
            }
            holder.mTitle.setText(new File(this.data.get(position)).getName());
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageButton mIcon;
            private LinearLayout mLinearLayout;
            private MaterialTextView mTitle;
            private MaterialTextView mDescription;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
                this.mLinearLayout = view.findViewById(R.id.rv_filepicker);
                this.mTitle = view.findViewById(R.id.title);
                this.mDescription = view.findViewById(R.id.description);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        public void setOnItemClickListener(ClickListener clickListener) {
            RecycleViewAdapter.clickListener = clickListener;
        }

        public interface ClickListener {
            void onItemClick(int position, View v);
        }
    }

}