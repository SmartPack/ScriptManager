package com.smartpack.scriptmanager.views.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.fragments.RecyclerViewFragment;
import com.smartpack.scriptmanager.viewpagerindicator.CirclePageIndicator;

import java.util.List;

/**
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class ViewPagerDialog extends DialogFragment {

    public static ViewPagerDialog newInstance(int height, List<Fragment> fragments) {
        ViewPagerDialog fragment = new ViewPagerDialog();
        fragment.mHeight = height;
        fragment.mFragments = fragments;
        return fragment;
    }

    private int mHeight;
    private List<Fragment> mFragments;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.viewpager_view, container, false);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        CirclePageIndicator indicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        viewPager.setAdapter(new RecyclerViewFragment.ViewPagerAdapter(getChildFragmentManager(), mFragments));
        indicator.setViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = mHeight;
                view.requestLayout();
            }
        });
    }
}
