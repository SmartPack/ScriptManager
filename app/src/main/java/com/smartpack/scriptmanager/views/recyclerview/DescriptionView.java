package com.smartpack.scriptmanager.views.recyclerview;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;

import com.smartpack.scriptmanager.R;

/**
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class DescriptionView extends RecyclerViewItem {

    public interface OnMenuListener {
        void onMenuReady(DescriptionView descriptionView, PopupMenu popupMenu);
    }

    private View mRootView;
    private View mMenuButton;
    private AppCompatImageView mImageView;
    private AppCompatTextView mTitleView;

    private Drawable mImage;
    private CharSequence mTitle;
    private PopupMenu mPopupMenu;
    private OnMenuListener mOnMenuListener;

    @Override
    public int getLayoutRes() {
        return R.layout.rv_description_view;
    }

    @Override
    public void onCreateView(View view) {
        mRootView = view;
        mImageView = view.findViewById(R.id.image);
        mTitleView = view.findViewById(R.id.title);
        mMenuButton = view.findViewById(R.id.menu_button);

        if (mTitleView != null) {
            mTitleView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    mRootView.requestFocus();
                }
            });
        }
        mMenuButton.setOnClickListener(v -> {
            if (mPopupMenu != null) {
                mPopupMenu.show();
            }
        });

        super.onCreateView(view);
    }

    public void setDrawable(Drawable drawable) {
        mImage = drawable;
        refresh();
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        refresh();
    }

    public void setOnMenuListener(OnMenuListener onMenuListener) {
        mOnMenuListener = onMenuListener;
        refresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mImageView != null && mImage != null) {
            mImageView.setImageDrawable(mImage);
            mImageView.setVisibility(View.VISIBLE);
        }
        if (mTitleView != null) {
            if (mTitle != null) {
                mTitleView.setText(mTitle);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }
        if (mMenuButton != null && mOnMenuListener != null && mTitleView != null) {
            mMenuButton.setVisibility(View.VISIBLE);
            mPopupMenu = new PopupMenu(mMenuButton.getContext(), mMenuButton);
            mOnMenuListener.onMenuReady(this, mPopupMenu);
        }
        if (mRootView != null && getOnItemClickListener() != null && mTitleView != null) {
            mTitleView.setTextIsSelectable(false);
            mRootView.setOnClickListener(v -> {
                if (getOnItemClickListener() != null) {
                    getOnItemClickListener().onClick(DescriptionView.this);
                }
            });
        }
    }
}