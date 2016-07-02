package com.sanron.ganker.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
import android.view.ViewGroup;

import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.CommonUtil;
import com.sanron.ganker.util.PermissionUtil;
import com.sanron.ganker.widget.PermissionDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Func1;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {


    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.appbar) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.view_network_none) View mNoneNetwork;
    @BindView(R.id.btn_shuffle) FloatingActionButton mFabShuffle;
    @BindView(R.id.navigation) NavigationView mNavigationView;
    @BindView(R.id.root) ViewGroup mRoot;


    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CommonUtil.isNetworkAvaialable(context)) {
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
        initView();
        registerReceiver(mNetworkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        requestPermission();
    }

    private void initView() {
        mViewPager.setAdapter(new LocalPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(this);
        mToolbar.setOnMenuItemClickListener(this);
    }

    private void requestPermission() {
        List<String> deniedPermissions = PermissionUtil.getDeniedPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        for (final String p : deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, p)) {
                String alert = null;
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(p)) {
                    alert = "应用缓存网络资源,节省网络流量,需要授予以下权限";
                }
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(p)) {
                    new PermissionDialog(this)
                            .setMessage(alert)
                            .setPermissions(deniedPermissions)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{p}, 0);
                                }
                            })
                            .show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{p}, 0);
            }
        }
    }

    @OnClick(R.id.btn_shuffle)
    public void onShuffle() {
        addFragmentToFront(
                ShuffleGankFragment.newInstance(
                        LocalPagerAdapter.CATEGORIES[mViewPager.getCurrentItem()]));
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_meizhi: {
                addFragmentToFront(MeizhiFragment.newInstance());
            }
            break;

            case R.id.menu_history: {

            }
            break;

            case R.id.menu_collections: {

            }
            break;

            case R.id.menu_feedback: {

            }
            break;

            case R.id.menu_setting: {

            }
            break;
        }
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        return true;
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
                .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down,
                        R.anim.slide_in_down, R.anim.slide_out_down)
                .add(R.id.root,
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


    public static class LocalPagerAdapter extends FragmentPagerAdapter {

        public static final String[] CATEGORIES = {Gank.CATEGORY_ANDROID,
                Gank.CATEGORY_IOS, Gank.CATEGORY_FRONT_END, Gank.CATEGORY_EXPAND};

        public static class CategoryGankCreator extends GankPagerFragment.ObservableCreator {
            private String category;

            public CategoryGankCreator(String category) {
                this.category = category;
            }

            @Override
            public Observable<List<? extends Gank>> onLoad(int pageSize, int page) {
                return GankerRetrofit
                        .get()
                        .getGankService()
                        .getByCategory(category, pageSize, page)
                        .map(new Func1<GankData, List<? extends Gank>>() {
                            @Override
                            public List<? extends Gank> call(GankData gankData) {
                                return gankData.results;
                            }
                        });
            }
        }

        public LocalPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            return GankPagerFragment.newInstance(new CategoryGankCreator(CATEGORIES[position]));
        }

        @Override
        public int getCount() {
            return CATEGORIES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CATEGORIES[position];
        }
    }

}
