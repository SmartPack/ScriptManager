package com.smartpack.scriptmanager.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;

import com.smartpack.scriptmanager.R;
import com.smartpack.scriptmanager.utils.Prefs;
import com.smartpack.scriptmanager.utils.Utils;
import com.smartpack.scriptmanager.utils.ViewUtils;
import com.smartpack.scriptmanager.views.dialog.ViewPagerDialog;
import com.smartpack.scriptmanager.views.recyclerview.RecyclerViewAdapter;
import com.smartpack.scriptmanager.views.recyclerview.RecyclerViewItem;
import com.smartpack.scriptmanager.viewpagerindicator.CirclePageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public abstract class RecyclerViewFragment extends BaseFragment {

    private Handler mHandler;
    private ScheduledThreadPoolExecutor mPoolExecutor;

    private List<RecyclerViewItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Scroller mScroller;

    private View mProgress;

    private List<Fragment> mViewPagerFragments;
    private View mViewPagerParent;
    private ViewPager mViewPager;
    private View mViewPagerShadow;
    private CirclePageIndicator mCirclePageIndicator;

    private FloatingActionButton mTopFab;
    private FloatingActionButton mBottomFab;

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolBar;

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private ValueAnimator mForegroundAnimator;
    private boolean mForegroundVisible;
    private View mForegroundParent;
    private float mForegroundHeight;
    private CharSequence mForegroundStrText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mHandler = new Handler();

        mRecyclerView = mRootView.findViewById(R.id.recyclerview);

        // Initialize Google Ads
        if (Prefs.getBoolean("allow_ads", true, getActivity())) {
            AdView mAdView = mRootView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        if (mViewPagerFragments != null) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : mViewPagerFragments) {
                fragmentTransaction.remove(fragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
            mViewPagerFragments.clear();
        } else {
            mViewPagerFragments = new ArrayList<>();
        }
        mViewPagerParent = mRootView.findViewById(R.id.viewpagerparent);
        mViewPager = mRootView.findViewById(R.id.viewpager);
        mViewPager.setVisibility(View.INVISIBLE);
        mViewPagerShadow = mRootView.findViewById(R.id.viewpager_shadow);
        mViewPagerShadow.setVisibility(View.INVISIBLE);
        mCirclePageIndicator = mRootView.findViewById(R.id.indicator);
        mViewPagerParent.setVisibility(View.INVISIBLE);
        ViewUtils.dismissDialog(getChildFragmentManager());

        mProgress = mRootView.findViewById(R.id.progress);

        if (mAppBarLayout != null && !isForeground()) {
            mAppBarLayout.postDelayed(() -> {
                if (mAppBarLayout != null && isAdded() && getActivity() != null) {
                    ViewCompat.setElevation(mAppBarLayout, showViewPager() ?
                            0 : getResources().getDimension(R.dimen.app_bar_elevation));
                }
            }, 150);
        }

        mTopFab = mRootView.findViewById(R.id.top_fab);
        mBottomFab = mRootView.findViewById(R.id.bottom_fab);

        mRecyclerView.clearOnScrollListeners();
        if (showViewPager()) {
            mScroller = new Scroller();
            mRecyclerView.addOnScrollListener(mScroller);
        }
        mRecyclerView.setAdapter(mRecyclerViewAdapter == null ? mRecyclerViewAdapter
                = new RecyclerViewAdapter(mItems, () -> getHandler().postDelayed(() -> {
                    if (isAdded() && getActivity() != null) {
                        adjustScrollPosition();
                    }
                }, 250)) : mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        mTopFab.setOnClickListener(v -> onTopFabClick());
        {
            Drawable drawable = getTopFabDrawable();
            if (drawable != null) {
                mTopFab.setImageDrawable(drawable);
            }
        }

        mBottomFab.setOnClickListener(v -> onBottomFabClick());
        {
            Drawable drawable = getBottomFabDrawable();
            if (drawable != null) {
                mBottomFab.setImageDrawable(drawable);
            }
        }

        BaseFragment foregroundFragment = getForegroundFragment();
        mForegroundVisible = false;
        if (foregroundFragment != null) {
            mForegroundParent = mRootView.findViewById(R.id.foreground_parent);
            TextView mForegroundText = mRootView.findViewById(R.id.foreground_text);
            mForegroundText.setOnClickListener(v -> dismissForeground());
            getChildFragmentManager().beginTransaction().replace(R.id.foreground_content,
                    foregroundFragment).commit();
            mForegroundHeight = getResources().getDisplayMetrics().heightPixels;
        }

        if (itemsSize() == 0) {
            mLoader = new UILoader(this, savedInstanceState);
            mLoader.execute();
        } else {
            showProgress();
            init();
            hideProgress();
            postInit();
            adjustScrollPosition();

            mViewPager.setVisibility(View.VISIBLE);
            mViewPagerShadow.setVisibility(View.VISIBLE);
        }

        return mRootView;
    }

    private static class UILoader extends AsyncTask<Void, Void, List<RecyclerViewItem>> {

        private WeakReference<RecyclerViewFragment> mRefFragment;
        private Bundle mSavedInstanceState;

        private UILoader(RecyclerViewFragment fragment, Bundle savedInstanceState) {
            mRefFragment = new WeakReference<>(fragment);
            mSavedInstanceState = savedInstanceState;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RecyclerViewFragment fragment = mRefFragment.get();

            fragment.showProgress();
            fragment.init();
        }

        @Override
        protected List<RecyclerViewItem> doInBackground(Void... params) {
            RecyclerViewFragment fragment = mRefFragment.get();

            if (fragment.isAdded() && fragment.getActivity() != null) {
                List<RecyclerViewItem> items = new ArrayList<>();
                fragment.addItems(items);
                return items;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
            super.onPostExecute(recyclerViewItems);
            if (isCancelled() || recyclerViewItems == null) return;

            final RecyclerViewFragment fragment = mRefFragment.get();

            for (RecyclerViewItem item : recyclerViewItems) {
                fragment.addItem(item);
            }
            fragment.hideProgress();
            fragment.postInit();
            if (mSavedInstanceState == null) {
                fragment.mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = fragment.getActivity();
                        if (fragment.isAdded() && activity != null) {
                            fragment.mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    activity, R.anim.slide_in_bottom));

                            int cx = fragment.mViewPager.getWidth();

                            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                                    fragment.mViewPager, cx / 2, 0, 0, cx);
                            animator.addListener(new SupportAnimator.SimpleAnimatorListener() {
                                @Override
                                public void onAnimationStart() {
                                    super.onAnimationStart();
                                    fragment.mViewPager.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd() {
                                    super.onAnimationEnd();
                                    fragment.mViewPagerShadow.setVisibility(View.VISIBLE);
                                }
                            });
                            animator.setDuration(400);
                            animator.start();
                        }
                    }
                });
            } else {
                fragment.mViewPager.setVisibility(View.VISIBLE);
                fragment.mViewPagerShadow.setVisibility(View.VISIBLE);
            }
            fragment.mLoader = null;
        }
    }

    @Override
    public void onViewFinished() {
        super.onViewFinished();
        if (showViewPager()) {
            ViewPagerAdapter mViewPagerAdapter;
            mViewPager.setAdapter(mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(),
                    mViewPagerFragments));
            mCirclePageIndicator.setViewPager(mViewPager);

            setAppBarLayoutAlpha(0);
            adjustScrollPosition();
        } else {
            mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), isForeground() ? 0 : mToolBar.getHeight(),
                    mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
            mRecyclerView.setClipToPadding(true);
            ViewGroup.LayoutParams layoutParams = mViewPagerParent.getLayoutParams();
            layoutParams.height = 0;
            mViewPagerParent.requestLayout();
            setAppBarLayoutAlpha(255);
        }
    }

    protected void init() {
    }

    private void postInit() {
        if (getActivity() != null && isAdded()) {
            for (RecyclerViewItem item : mItems) {
                item.onRecyclerViewCreate(getActivity());
            }
        }
    }

    private void adjustScrollPosition() {
        if (mScroller != null) {
            mScroller.onScrolled(mRecyclerView, 0, 0);
        }
    }

    protected abstract void addItems(List<RecyclerViewItem> items);

    private void setAppBarLayoutAlpha(int alpha) {
        if (isForeground()) return;
        Activity activity;
        if ((activity = getActivity()) != null && mAppBarLayout != null && mToolBar != null) {
            int colorPrimary = ViewUtils.getColorPrimaryColor(activity);
            mAppBarLayout.setBackgroundDrawable(new ColorDrawable(Color.argb(alpha, Color.red(colorPrimary),
                    Color.green(colorPrimary), Color.blue(colorPrimary))));
            mToolBar.setTitleTextColor(Color.argb(alpha, 255, 255, 255));
        }
    }

    void addItem(RecyclerViewItem recyclerViewItem) {
        mItems.add(recyclerViewItem);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyItemInserted(mItems.size() - 1);
        }
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) mLayoutManager).setSpanCount(getSpanCount());
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
    }

    private int getBannerHeight() {
        int min = Math.round(getResources().getDimension(R.dimen.banner_min_height));
        int max = Math.round(getResources().getDimension(R.dimen.banner_max_height));

        int height = Prefs.getInt("banner_size", Math.round(getResources().getDimension(
                R.dimen.banner_min_height)), getActivity());
        if (height > max) {
            height = max;
            Prefs.saveInt("banner_size", max, getActivity());
        } else if (height < min) {
            height = min;
            Prefs.saveInt("banner_size", min, getActivity());
        }
        return height;
    }

    void clearItems() {
        mItems.clear();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
            adjustScrollPosition();
        }
    }

    public int getSpanCount() {
        Activity activity;
        if ((activity = getActivity()) != null) {
            int span = Utils.isTablet(activity) ? Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 3 : 2 : Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
            if (itemsSize() != 0 && span > itemsSize()) {
                span = itemsSize();
            }
            return span;
        }
        return 1;
    }

    int itemsSize() {
        return mItems.size();
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        public ViewPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragments = fragments;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }
    }

    private class Scroller extends RecyclerView.OnScrollListener {

        private int mScrollDistance;
        private int mAppBarLayoutDistance;
        private boolean mFade = true;
        private ValueAnimator mAlphaAnimator;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            View firstItem = mRecyclerView.getChildAt(0);
            if (firstItem == null) {
                if (mRecyclerViewAdapter != null) {
                    firstItem = mRecyclerViewAdapter.getFirstItem();
                }
                if (firstItem == null) {
                    return;
                }
            }

            mScrollDistance = -firstItem.getTop() + mRecyclerView.getPaddingTop();

            int appBarHeight = 0;
            if (mAppBarLayout != null) {
                appBarHeight = mAppBarLayout.getHeight();
            }

            if (mScrollDistance > mViewPagerParent.getHeight() - appBarHeight) {
                mAppBarLayoutDistance += dy;
                fadeAppBarLayout(false);
                if (mTopFab != null && showTopFab()) {
                    mTopFab.hide();
                }
            } else {
                fadeAppBarLayout(true);
                if (mTopFab != null && showTopFab()) {
                    mTopFab.show();
                }
            }

            if (mAppBarLayout != null) {
                if (mAppBarLayoutDistance > mAppBarLayout.getHeight()) {
                    mAppBarLayoutDistance = mAppBarLayout.getHeight();
                } else if (mAppBarLayoutDistance < 0) {
                    mAppBarLayoutDistance = 0;
                }
                mAppBarLayout.setTranslationY(-mAppBarLayoutDistance);
            }

            mViewPagerParent.setTranslationY(-mScrollDistance);
            if (mTopFab != null) {
                mTopFab.setTranslationY(-mScrollDistance);
            }

            if (showBottomFab() && autoHideBottomFab()) {
                if (dy <= 0) {
                    if (mBottomFab.getVisibility() != View.VISIBLE) {
                        mBottomFab.show();
                    }
                } else if (mBottomFab.getVisibility() == View.VISIBLE) {
                    mBottomFab.hide();
                }
            }
        }

        private void fadeAppBarLayout(boolean fade) {
            if (mFade != fade) {
                mFade = fade;

                if (mAlphaAnimator != null) {
                    mAlphaAnimator.cancel();
                }

                mAlphaAnimator = ValueAnimator.ofFloat(fade ? 1f : 0f, fade ? 0f : 1f);
                mAlphaAnimator.addUpdateListener(animation -> setAppBarLayoutAlpha(Math.round(255 * (float) animation.getAnimatedValue())));
                mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mAlphaAnimator = null;
                    }
                });
                mAlphaAnimator.start();
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (mAppBarLayout == null || newState != 0 || mAppBarLayoutDistance == 0
                    || (mAppBarLayoutDistance == mAppBarLayout.getHeight() && mScrollDistance != 0)) {
                return;
            }

            boolean show = mAppBarLayoutDistance < mAppBarLayout.getHeight() * 0.5f
                    || mScrollDistance <= mViewPagerParent.getHeight();
            ValueAnimator animator = ValueAnimator.ofInt(mAppBarLayoutDistance, show ? 0 : mAppBarLayout.getHeight());
            animator.addUpdateListener(animation -> {
                mAppBarLayoutDistance = (int) animation.getAnimatedValue();
                mAppBarLayout.setTranslationY(-mAppBarLayoutDistance);
            });
            animator.start();
        }
    }

    void showProgress() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    mProgress.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    if (mTopFab != null && showTopFab()) {
                        mTopFab.hide();
                    }
                    if (mBottomFab != null && showBottomFab()) {
                        mBottomFab.hide();
                    }
                }
            });
        }
    }

    void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mViewPagerParent.setVisibility(View.VISIBLE);
        if (mTopFab != null && showTopFab()) {
            mTopFab.show();
        }
        if (mBottomFab != null && showBottomFab()) {
            mBottomFab.show();
        }
        adjustScrollPosition();
    }

    private boolean isForeground() {
        return false;
    }

    private BaseFragment getForegroundFragment() {
        return null;
    }

    private void dismissForeground() {
        float translation = mForegroundParent.getTranslationY();
        mForegroundAnimator = ValueAnimator.ofFloat(translation, mForegroundHeight);
        mForegroundAnimator.addUpdateListener(animation -> mForegroundParent.setTranslationY((float) animation.getAnimatedValue()));
        mForegroundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mForegroundParent.setVisibility(View.GONE);
                mForegroundVisible = false;
                mForegroundAnimator = null;
            }
        });
        mForegroundAnimator.start();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (showViewPager()) {
            menu.add(0, 0, Menu.NONE, R.string.options)
                    .setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_launcher_preview))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        if (showTopFab()) {
            menu.add(0, 1, Menu.NONE, R.string.more)
                    .setIcon(getTopFabDrawable())
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else if (showBottomFab()) {
            menu.add(0, 1, Menu.NONE, R.string.more)
                    .setIcon(getBottomFabDrawable())
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                ViewUtils.showDialog(getChildFragmentManager(),
                        ViewPagerDialog.newInstance(getBannerHeight(), mViewPagerFragments));
                return true;
            case 1:
                if (showTopFab()) {
                    onTopFabClick();
                } else if (showBottomFab()) {
                    onBottomFabClick();
                }
                return true;
        }
        return false;
    }

    private boolean showViewPager() {
        return true;
    }

    private boolean showTopFab() {
        return false;
    }

    private Drawable getTopFabDrawable() {
        return null;
    }

    private void onTopFabClick() {
    }

    protected boolean showBottomFab() {
        return false;
    }

    protected Drawable getBottomFabDrawable() {
        return null;
    }

    protected void onBottomFabClick() {
    }

    private boolean autoHideBottomFab() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPoolExecutor == null) {
            mPoolExecutor = new ScheduledThreadPoolExecutor(1);
            mPoolExecutor.scheduleWithFixedDelay(mScheduler, 0, 500,
                    TimeUnit.MILLISECONDS);
        }
        for (RecyclerViewItem item : mItems) {
            item.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPoolExecutor != null) {
            mPoolExecutor.shutdown();
            mPoolExecutor = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onPause();
        }
    }

    private Runnable mScheduler = () -> {
        refreshThread();

        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (getActivity() != null) {
                refresh();
            }
        });
    };

    private void refreshThread() {
    }

    private void refresh() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
        mRecyclerViewAdapter = null;
        setAppBarLayoutAlpha(255);
        if (mAppBarLayout != null && !isForeground()) {
            mAppBarLayout.setTranslationY(0);
            ViewCompat.setElevation(mAppBarLayout, 0);
        }
        if (mLoader != null) {
            mLoader.cancel(true);
            mLoader = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onDestroy();
        }
    }

    Handler getHandler() {
        return mHandler;
    }

}