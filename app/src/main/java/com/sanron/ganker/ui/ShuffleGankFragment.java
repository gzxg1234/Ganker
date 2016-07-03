package com.sanron.ganker.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.data.GankService;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.decoration.DividerItemDecoration;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.CommonUtil;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PullRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func4;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-29.
 */
public class ShuffleGankFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recycler_view) PullRecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fab_choice_category) FloatingActionButton mFabChoice;

    private GankAdapter mGankAdapter;
    private Subscription mSubscription;
    private String mCategory;

    public static final String ARG_CATEGORY = "category";
    private static int REQUEST_CUNNT = 20;//请求数量
    private static int EACH_COUNT = REQUEST_CUNNT / 4;//都看看时每个分类获取数量
    private static final String[] CATEGORIES = new String[]{
            "都看看",
            Gank.CATEGORY_ANDROID,
            Gank.CATEGORY_IOS,
            Gank.CATEGORY_FRONT_END,
            Gank.CATEGORY_EXPAND
    };

    public static ShuffleGankFragment newInstance(String category) {
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        ShuffleGankFragment shuffleGankFragment = new ShuffleGankFragment();
        shuffleGankFragment.setArguments(args);
        return shuffleGankFragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_shuffle_rank;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mCategory = args.getString(ARG_CATEGORY, "");
    }

    Observable.Transformer<GankData, List<Gank>> gankTransformer = new Observable.Transformer<GankData, List<Gank>>() {
        @Override
        public Observable<List<Gank>> call(Observable<GankData> gankDataObservable) {
            return gankDataObservable.map(new Func1<GankData, List<Gank>>() {
                @Override
                public List<Gank> call(GankData gankData) {
                    if (gankData.results != null) {
                        Collections.shuffle(gankData.results);
                    }
                    return gankData.results;
                }
            });
        }
    };

    @OnClick(R.id.fab_choice_category)
    public void onChoiceClick() {
        int checkedItem = 0;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(mCategory)) {
                checkedItem = i;
            }
        }
        final int[] select = {checkedItem};
        new AlertDialog.Builder(getContext())
                .setSingleChoiceItems(
                        CATEGORIES,
                        checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                select[0] = which;
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (CATEGORIES[select[0]].equals(mCategory)) {
                            return;
                        } else {
                            mCategory = CATEGORIES[select[0]];
                            initLoad();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mFabChoice.show();
                    }
                })
                .show();
        mFabChoice.hide();
    }


    @Override
    public void initView(View root, Bundle savedInstanceState) {
        super.initView(root, savedInstanceState);

        mSwipeRefreshLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mGankAdapter = new GankAdapter(getContext());
        mGankAdapter.setShowCategoryIcon(true);
        mRecyclerView.setLoadEnable(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(CommonUtil.dpToPx(getContext(), 4)));
        mRecyclerView.setAdapter(mGankAdapter);

        initLoad();
    }

    private void initLoad() {
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
        if (mSubscription != null
                && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onRefresh() {

        Observable<GankData> observable;
        GankService gankService = GankerRetrofit.get().getGankService();
        switch (mCategory) {
            case Gank.CATEGORY_ANDROID: {
                observable = gankService.shuffleGank(Gank.CATEGORY_ANDROID, REQUEST_CUNNT);
            }
            break;
            case Gank.CATEGORY_IOS: {
                observable = gankService.shuffleGank(Gank.CATEGORY_IOS, REQUEST_CUNNT);
            }
            break;
            case Gank.CATEGORY_FRONT_END: {
                observable = gankService.shuffleGank(Gank.CATEGORY_FRONT_END, REQUEST_CUNNT);
            }
            break;
            case Gank.CATEGORY_EXPAND: {
                observable = gankService.shuffleGank(Gank.CATEGORY_EXPAND, REQUEST_CUNNT);
            }
            break;
            default: {
                observable = Observable
                        .zip(
                                gankService.shuffleGank(Gank.CATEGORY_ANDROID, EACH_COUNT),
                                gankService.shuffleGank(Gank.CATEGORY_IOS, EACH_COUNT),
                                gankService.shuffleGank(Gank.CATEGORY_FRONT_END, EACH_COUNT),
                                gankService.shuffleGank(Gank.CATEGORY_EXPAND, EACH_COUNT),
                                new Func4<GankData, GankData, GankData, GankData, GankData>() {
                                    @Override
                                    public GankData call(GankData gankData, GankData gankData2, GankData gankData3, GankData gankData4) {
                                        GankData result = new GankData();
                                        result.results = new ArrayList<>();
                                        if (gankData.results != null) {
                                            result.results.addAll(gankData.results);
                                        }
                                        if (gankData2.results != null) {
                                            result.results.addAll(gankData2.results);
                                        }
                                        if (gankData3.results != null) {
                                            result.results.addAll(gankData3.results);
                                        }
                                        if (gankData4.results != null) {
                                            result.results.addAll(gankData4.results);
                                        }
                                        return result;
                                    }
                                }
                        );
            }
            break;
        }
        mSubscription = observable
                .compose(gankTransformer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Gank>>() {
                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ToastUtil.shortShow("获取数据失败");
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<Gank> ganks) {
                        mGankAdapter.setData(ganks);
                    }
                });
    }

}
