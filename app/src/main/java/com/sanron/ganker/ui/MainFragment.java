package com.sanron.ganker.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.ui.base.BaseFragment;

import butterknife.BindView;

/**
 * Created by sanron on 16-6-28.
 */
public class MainFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener {

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


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void initView(View root, Bundle savedInstanceState) {
        mViewPager.setAdapter(new LocalPagerAdapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        mToolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity_option, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search: {
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class LocalPagerAdapter extends FragmentPagerAdapter {

        private final String[] CATEGORIES = {"Android", "iOS", "前端", "拓展资源"};

        public LocalPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return GankPagerFragment.newInstance(CATEGORIES[position]);
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_meizhi: {

            }
            break;

            case R.id.menu_history: {

            }
            break;

            case R.id.menu_collections: {

            }
            break;
        }
        return false;
    }
}
