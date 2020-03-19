/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.AppCompatEditText;

import com.smartpack.scriptmanager.R;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class EditorActivity extends AppCompatActivity {

    public static final String TITLE_INTENT = "title";
    public static final String TEXT_INTENT = "text";
    private static final String EDITTEXT_INTENT = "edittext";

    private AppCompatEditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initToolBar();
        String title = getIntent().getStringExtra(TITLE_INTENT);
        if (title != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        }

        CharSequence text = getIntent().getCharSequenceExtra(TEXT_INTENT);
        mEditText = findViewById(R.id.edittext);
        if (text != null) {
            mEditText.append(text);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(EDITTEXT_INTENT, mEditText.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_save);
        assert drawable != null;
        DrawableCompat.setTint(drawable, Color.WHITE);
        menu.add(0, Menu.FIRST, Menu.FIRST, getString(R.string.save)).setIcon(drawable)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent();
        intent.putExtra(TEXT_INTENT, mEditText.getText());
        setResult(0, intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    public void initToolBar() {
        Toolbar toolbar = getToolBar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }

}