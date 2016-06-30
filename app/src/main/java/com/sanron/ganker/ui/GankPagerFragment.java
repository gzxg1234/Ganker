package com.sanron.ganker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.DimenTool;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.view.DividerItemDecoration;
import com.sanron.ganker.view.PullRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-28.
 */
public class GankPagerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, PullRecyclerView.OnLoadMoreListener {

    @BindView(R.id.recycler_view)
    PullRecyclerView mRecyclerView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private GankAdapter mGankAdapter;
    private List<Gank> mGanks;
    private int page;
    private boolean isLoaded;
    private boolean isInited;

    private static final int PAGE_SIZE = 20;
    public static final String ARG_CREATOR = "creator";
    private Subscription mSubscription;
    private ObservableCreator mObservableCreator;

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
        mSwipeRefreshLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mGankAdapter = new GankAdapter(getContext());
        mGankAdapter.setData(mGanks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(DimenTool.dpToPx(getContext(), 4)));
        mRecyclerView.setAdapter(mGankAdapter);
        mRecyclerView.setOnLoadMoreListener(this);
        if (!isLoaded && getUserVisibleHint()) {
            firstLoad();
        }
        isInited = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isInited = false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isInited
                && !isLoaded) {
            firstLoad();
        }
    }

    public void firstLoad() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                onRefresh();
                isLoaded = true;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mObservableCreator = (ObservableCreator) args.getSerializable(ARG_CREATOR);
    }

    @Override
    public int getLayoutId() {
        return R.layout.pullrefresh_with_recycler_view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null
                && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onRefresh() {
        page = 0;
        mGanks = null;
        mGankAdapter.setEnableLoad(true);
        onLoad();
    }

    public void setObservableCreator(ObservableCreator observableCreator) {
        mObservableCreator = observableCreator;
        if (getUserVisibleHint()
                && isInited) {
            mGankAdapter.setData(null);
            firstLoad();
        } else {
            page = 0;
            mGanks = null;
            mGankAdapter.setData(null);
            mGankAdapter.setEnableLoad(true);
            isLoaded = false;
        }
    }


    @Override
    public void onLoad() {
        mSubscription = mObservableCreator
                .onLoad(PAGE_SIZE, page + 1)
                .defaultIfEmpty(new ArrayList<Gank>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<? extends Gank>>() {
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
                            mGankAdapter.setEnableLoad(false);
                        }
                        page++;
                    }
                });
    }

}
