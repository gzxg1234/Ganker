package com.sanron.ganker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.Common;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.DividerItemDecoration;
import com.sanron.ganker.widget.PullAdapter;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-28.
 */
public class GankPagerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, PullAdapter.OnLoadMoreListener {

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    private GankAdapter mGankAdapter;
    private int mPage;
    private boolean mIsLoaded;
    private boolean mIsInited;

    private static final int PAGE_SIZE = 20;
    public static final String ARG_CREATOR = "creator";
    private ObservableCreator mObservableCreator;

    /**
     * Observable生产者
     */
    public static abstract class ObservableCreator implements Serializable {
        public abstract Observable<List<? extends Gank>> onLoad(int pageSize, int page);
    }

    public static GankPagerFragment newInstance(ObservableCreator observableCreator) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CREATOR, observableCreator);
        GankPagerFragment gankPagerFragment = new GankPagerFragment();
        gankPagerFragment.setArguments(args);
        return gankPagerFragment;
    }

    @Override
    public void initView(View root, Bundle savedInstanceState) {
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
        mObservableCreator = (ObservableCreator) args.get(ARG_CREATOR);
        mGankAdapter = new GankAdapter(getContext());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.pullrefresh_with_recycler_view;
    }


    @Override
    public void onRefresh() {
        mGankAdapter.setLoadEnable(true);
        loadData(true);
    }

    @Override
    public void onLoad() {
        loadData(false);
    }

    private class LoadSubscreber extends Subscriber<List<? extends Gank>> {

        private boolean mRefresh;

        public LoadSubscreber(boolean refresh) {
            mRefresh = refresh;
        }

        @Override
        public void onCompleted() {
            mSwipeRefreshLayout.setRefreshing(false);
            mGankAdapter.onLoadComplete();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            mSwipeRefreshLayout.setRefreshing(false);
            mGankAdapter.onLoadComplete();
            ToastUtil.shortShow(getString(R.string.load_data_failed));
        }

        @Override
        public void onNext(List<? extends Gank> ganks) {
            if (mRefresh) {
                mGankAdapter.setData(ganks);
                mPage = 1;
            } else {
                mGankAdapter.addAll(ganks);
                mPage++;
            }
            if (ganks.size() < PAGE_SIZE) {
                //没有更多
                mGankAdapter.setLoadEnable(false);
            }
        }
    }

    public void loadData(final boolean refresh) {
        addSub(mObservableCreator
                .onLoad(PAGE_SIZE, refresh ? 1 : mPage + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadSubscreber(refresh)));
    }

    public void setObservableCreator(ObservableCreator observableCreator) {
        mObservableCreator = observableCreator;
        mGankAdapter.setData(null);
        mIsLoaded = false;
        if (getUserVisibleHint()
                && mIsInited) {
            firstLoad();
        }
    }

}
