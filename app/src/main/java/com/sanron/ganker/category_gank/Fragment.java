package com.sanron.ganker.category_gank;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sanron.ganker.Ganker;
import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.Common;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.DividerItemDecoration;
import com.sanron.ganker.widget.PullAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by sanron on 16-6-28.
 */
public class Fragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, PullAdapter.OnLoadMoreListener, CategoricalGankContract.View {

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    CategoricalGankPresenter mCategoricalGankPresenter;

    private GankAdapter mGankAdapter;
    private boolean mIsLoaded;
    private boolean mIsInited;

    public static final String ARG_CATEGORY = "category";

    public static Fragment newInstance(String category) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        Fragment gankPagerFragment = new Fragment();
        gankPagerFragment.setArguments(args);
        return gankPagerFragment;
    }

    @Override
    public void initView(android.view.View root, Bundle savedInstanceState) {
        super.initView(root, savedInstanceState);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(Common.dpToPx(getContext(), 4)));
        mRecyclerView.setAdapter(mGankAdapter);
        mGankAdapter.setOnLoadMoreListener(this);
        if (!mIsLoaded && getUserVisibleHint()) {
            firstLoad();
        }
        mIsInited = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsInited = false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mIsInited
                && !mIsLoaded) {
            firstLoad();
        }
    }

    public void firstLoad() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                onRefresh();
                mIsLoaded = true;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        String category = args.getString(ARG_CATEGORY);

        DaggerCategoricalGankComponent.builder()
                .gankerDataComponent(((Ganker) getContext().getApplicationContext())
                        .getGankerDataComponent())
                .categoricalGankModule(new CategoricalGankModule(category))
                .build()
                .inject(this);
        mCategoricalGankPresenter.attach(this);
        mGankAdapter = new GankAdapter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCategoricalGankPresenter = null;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.pullrefresh_with_recycler_view;
    }


    @Override
    public void onRefresh() {
        mGankAdapter.setLoadEnable(true);
        mCategoricalGankPresenter.refresh();
    }

    @Override
    public void onLoad() {
        mCategoricalGankPresenter.loadData();
    }

    @Override
    public void onLoadDataSuccess(List<Gank> ganks) {
        mGankAdapter.addAll(ganks);
    }

    @Override
    public void onRefreshData(List<Gank> ganks) {
        mGankAdapter.setData(ganks);
    }

    @Override
    public void onHasNoMore() {
        mGankAdapter.setLoadEnable(false);
    }

    @Override
    public void onLoadDataError() {
        mSwipeRefreshLayout.setRefreshing(false);
        mGankAdapter.onLoadComplete();
        ToastUtil.shortShow(getString(R.string.load_data_failed));
    }
}
