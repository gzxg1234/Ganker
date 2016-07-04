package com.sanron.ganker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.ui.adapter.MeizhiAdapter;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PullRecyclerView;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-3.
 */
public class MeizhiActivity extends BaseActivity implements MeizhiAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, PullRecyclerView.OnLoadMoreListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view) PullRecyclerView mRecyclerView;

    private SharedElementCallback mSharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReentering) {
                MeizhiAdapter.Holder holder = (MeizhiAdapter.Holder) mRecyclerView.findViewHolderForAdapterPosition(mEndPos);
                if (holder == null) {
                    sharedElements.remove(MeizhiDetailActivity.ELEMENT_IMG);
                } else {
                    View v = holder.ivImg;
                    sharedElements.put(MeizhiDetailActivity.ELEMENT_IMG, v);
                }
                mIsReentering = false;
            }
        }
    };

    private MeizhiAdapter mAdapter;
    private int mPage;
    private int PAGE_SIZE = 20;

    private int mEndPos = 0;
    private boolean mIsReentering = false;

    private Subscription mPreLoadSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meizhi);
        ButterKnife.bind(this);
        setExitSharedElementCallback(mSharedElementCallback);
        initView();
        firstLoad();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        mAdapter = new MeizhiAdapter(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnLoadMoreListener(this);
        mAdapter.setOnItemClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mIsReentering = true;
        mEndPos = data.getIntExtra(MeizhiDetailActivity.EXTRA_END_POS, 0);
        mRecyclerView.scrollToPosition(mEndPos);
        ActivityCompat.postponeEnterTransition(this);
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                ActivityCompat.startPostponedEnterTransition(MeizhiActivity.this);
                return false;
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData(true);
    }

    @Override
    public void onLoad() {
        loadData(false);
    }

    public void loadData(final boolean refresh) {
        addSub(GankerRetrofit
                .get()
                .getGankService()
                .getByCategory(Gank.CATEGORY_FULI, PAGE_SIZE, refresh ? 1 : mPage + 1)
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
                        if (refresh) {
                            mAdapter.setData(ganks);
                            mPage = 1;
                        } else {
                            mAdapter.addAll(ganks);
                            mPage++;
                        }
                        if (ganks.size() < PAGE_SIZE) {
                            mRecyclerView.setLoadEnable(false);
                        }
                    }
                }));
    }


    @Override
    public void onItemClick(final View itemView, final int position) {
        if (mPreLoadSubscription != null) {
            mPreLoadSubscription.unsubscribe();
        }
        mPreLoadSubscription = Observable
                .create(new Observable.OnSubscribe<Void>() {
                    @Override
                    public void call(final Subscriber<? super Void> subscriber) {
                        //预先加载原图到内存中
                        Glide.with(MeizhiActivity.this)
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
                        showMeizhiDetail(itemView, position);
                    }
                });
    }

    private void showMeizhiDetail(View view, int position) {
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                view, MeizhiDetailActivity.ELEMENT_IMG);
        Intent intent = new Intent(this, MeizhiDetailActivity.class);
        intent.putExtra(MeizhiDetailActivity.EXTRA_GANKS, mAdapter.getData().toArray());
        intent.putExtra(MeizhiDetailActivity.EXTRA_START_POS, position);
        ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
    }
}
