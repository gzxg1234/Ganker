package com.sanron.ganker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.NetworkUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Func1;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {


    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @BindView(R.id.view_network_none)
    View mNoneNetwork;

    @BindView(R.id.btn_shuffle)
    FloatingActionButton mFabShuffle;

    private static final String[] PAGE_CATEGORIES = {Gank.CATEGORY_ANDROID,
            Gank.CATEGORY_IOS, Gank.CATEGORY_FRONT_END, Gank.CATEGORY_EXPAND};

    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtil.isNetworkAvaialable(context)) {
                mNoneNetwork.setVisibility(View.GONE);
            } else {
                mNoneNetwork.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setPadding();
        registerReceiver(mNetworkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mViewPager.setAdapter(new LocalPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        mToolbar.setOnMenuItemClickListener(this);
    }

    @OnClick(R.id.btn_shuffle)
    public void shuffle() {
        addFragmentToFront(ShuffleGankFragment.newInstance(PAGE_CATEGORIES[mViewPager.getCurrentItem()]));
    }

    private void setPadding() {
        int insetTop = mSystemBarTintManager.getConfig().getPixelInsetTop(false);
        int insetBottom = mSystemBarTintManager.getConfig().getPixelInsetBottom();
        for (int i = 0; i < mDrawerLayout.getChildCount(); i++) {
            View v = mDrawerLayout.getChildAt(i);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + insetTop,
                    v.getPaddingRight(), v.getPaddingBottom() + insetBottom);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public class LocalPagerAdapter extends FragmentPagerAdapter {

        public LocalPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            GankPagerFragment.ObservableCreator observableCreator = new GankPagerFragment.ObservableCreator() {
                @Override
                public Observable<List<? extends Gank>> onLoad(int pageSize, int page) {
                    return GankerRetrofit
                            .get()
                            .getGankService()
                            .getByCategory(PAGE_CATEGORIES[position], pageSize, page)
                            .map(new Func1<GankData, List<? extends Gank>>() {
                                @Override
                                public List<? extends Gank> call(GankData gankData) {
                                    return gankData.results;
                                }
                            });
                }
            };
            return GankPagerFragment.newInstance(observableCreator);
        }

        @Override
        public int getCount() {
            return PAGE_CATEGORIES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PAGE_CATEGORIES[position];
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search: {
                addFragmentToFront(SearchFragment.newInstance());
            }
            break;
        }
        return true;
    }

    private void addFragmentToFront(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                        R.anim.slide_in_right, R.anim.slide_out_right)
                .add(R.id.front_fragment_contanier,
                        fragment,
                        fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }
}
