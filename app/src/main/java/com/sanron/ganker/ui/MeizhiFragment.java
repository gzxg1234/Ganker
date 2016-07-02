package com.sanron.ganker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.ui.adapter.MeizhiAdapter;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PullRecyclerView;

import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-2.
 */
public class MeizhiFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, PullRecyclerView.OnLoadMoreListener, MeizhiAdapter.OnItemClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view) PullRecyclerView mRecyclerView;

    MeizhiAdapter mAdapter;
    private int page;
    private int PAGE_SIZE = 20;
    private SubscriptionList mSubscriptionList = new SubscriptionList();

    @Override
    public int getLayoutId() {
        return R.layout.fragment_meizhi;
    }

    public static MeizhiFragment newInstance() {
        return new MeizhiFragment();
    }

    @Override
    public void initView(View root, Bundle savedInstanceState) {
        super.initView(root, savedInstanceState);
        mAdapter = new MeizhiAdapter(getContext());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnLoadMoreListener(this);
        mAdapter.setOnItemClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        firstLoad();
    }

    private void firstLoad() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscriptionList.isUnsubscribed()) {
            mSubscriptionList.unsubscribe();
        }
    }

    @Override
    public void onRefresh() {
        page = 0;
        onLoad();
    }

    @Override
    public void onLoad() {
        mSubscriptionList.add(GankerRetrofit
                .get()
                .getGankService()
                .getByCategory(Gank.CATEGORY_FULI, PAGE_SIZE, page + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<GankData, List<Gank>>() {
                    @Override
                    public List<Gank> call(GankData gankData) {
                        return gankData.results;
                    }
                })
                .subscribe(new Subscriber<List<Gank>>() {
                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mRecyclerView.setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mRecyclerView.setLoading(false);
                        ToastUtil.shortShow("加载失败");
                    }

                    @Override
                    public void onNext(List<Gank> ganks) {
                        mAdapter.addData(ganks);
                        if (ganks.size() < PAGE_SIZE) {
                            mRecyclerView.setLoadEnable(false);
                        }
                        page++;
                    }
                }));
    }

    private Subscription mPreLoadSubscription;

    @Override
    public void onItemClick(final View itemView, final int position) {
        if (mPreLoadSubscription != null) {
            mPreLoadSubscription.unsubscribe();
        }
        mPreLoadSubscription = Observable
                .create(new Observable.OnSubscribe<Void>() {
                    @Override
                    public void call(final Subscriber<? super Void> subscriber) {
                        Glide.with(MeizhiFragment.this.getContext())
                                .load(mAdapter.getItem(position).getUrl())
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(null);
                                        }
                                        return true;
                                    }
                                })
                                .preload();
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startShowPic(itemView, mAdapter.getItem(position));
                    }
                });
    }


    private void startShowPic(View view, Gank gank) {
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                view, MeizhiPicActivity.TRANSITION_IMG);
        Intent intent = new Intent(getActivity(), MeizhiPicActivity.class);
        intent.putExtra(MeizhiPicActivity.EXTRA_GANK, gank);
        startActivity(intent, activityOptions.toBundle());
    }
}
