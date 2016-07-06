package com.sanron.ganker.category_gank;

import com.sanron.ganker.data.GankService;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.GankData;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sanron on 16-7-6.
 */
public class CategoricalGankPresenter implements CategoricalGankContract.Presenter {

    private GankService mGankService;
    private CategoricalGankContract.View mView;
    private CompositeSubscription mCompositeSubscription;
    private String mCategory;
    private int mPage;
    private static final int PAGE_SIZE = 20;

    @Inject
    public CategoricalGankPresenter(GankService gankService, String category) {
        mGankService = gankService;
        mCategory = category;
    }

    @Override
    public void attach(CategoricalGankContract.View view) {
        mView = view;
    }

    @Override
    public void refresh() {
        loadData(true);
    }

    @Override
    public void loadData() {
        loadData(false);
    }

    void loadData(final boolean refresh) {
        addSub(mGankService.getByCategory(mCategory, PAGE_SIZE, mPage + 1)
                .map(new Func1<GankData, List<Gank>>() {
                    @Override
                    public List<Gank> call(GankData gankData) {
                        return gankData.results;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Gank>>() {
                    @Override
                    public void call(List<Gank> ganks) {
                        if (refresh) {
                            mPage = 0;
                            mView.onRefreshData(ganks);
                        } else if (ganks.size() < PAGE_SIZE) {
                            //没有更多
                            mView.onLoadDataSuccess(ganks);
                        } else {
                            mView.onHasNoMore();
                        }
                        mPage++;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.onLoadDataError();
                    }
                }));
    }

    @Override
    public void onDestory() {
        if (mCompositeSubscription != null
                && mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }

    protected void addSub(Subscription subscription) {
        if (mCompositeSubscription == null
                || mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }
}
