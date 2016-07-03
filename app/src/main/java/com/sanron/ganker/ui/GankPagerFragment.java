package com.sanron.ganker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.decoration.DividerItemDecoration;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.CommonUtil;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PullRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-28.
 */
public class GankPagerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, PullRecyclerView.OnLoadMoreListener {

    @BindView(R.id.recycler_view) PullRecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    private GankAdapter mGankAdapter;
    private List<Gank> mGanks;
    private int mPage;
    private boolean mIsLoaded;
    private boolean mIsInited;

    private static final int PAGE_SIZE = 20;
    public static final String ARG_CREATOR = "creator";
    private SubscriptionList mSubscriptionList = new SubscriptionList();
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

        mGankAdapter = new GankAdapter(getContext());
        mGankAdapter.setData(mGanks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(CommonUtil.dpToPx(getContext(), 4)));
        mRecyclerView.setAdapter(mGankAdapter);
        mRecyclerView.setOnLoadMoreListener(this);
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
    }

    @Override
    public int getLayoutId() {
        return R.layout.pullrefresh_with_recycler_view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptionList.isUnsubscribed()) {
            mSubscriptionList.unsubscribe();
        }
    }

    @Override
    public void onRefresh() {
        mPage = 0;
        mGanks = null;
        mRecyclerView.setLoadEnable(true);
        onLoad();
    }

    public void setObservableCreator(ObservableCreator observableCreator) {
        mObservableCreator = observableCreator;
        mPage = 0;
        mGanks = null;
        mGankAdapter.setData(null);
        mRecyclerView.setLoadEnable(true);
        mIsLoaded = false;
        if (getUserVisibleHint()
                && mIsInited) {
            firstLoad();
        }
    }


    @Override
    public void onLoad() {
        mSubscriptionList.add(
                mObservableCreator
                        .onLoad(PAGE_SIZE, mPage + 1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new LocalSubscriber()));
    }

    private class LocalSubscriber extends Subscriber<List<? extends Gank>> {
        @Override
        public void onCompleted() {
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerView.setLoading(false);
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerView.setLoading(false);
            ToastUtil.shortShow("获取数据失败");
        }

        @Override
        public void onNext(List<? extends Gank> ganks) {
            if (mGanks == null) {
                mGanks = new ArrayList<>();
            }
            mGanks.addAll(ganks);
            mGankAdapter.setData(mGanks);
            if (ganks.size() < PAGE_SIZE) {
                //没有更多
                mRecyclerView.setLoadEnable(false);
            }
            mPage++;
        }
    }

}
