package com.sanron.ganker.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.db.GankerDB;
import com.sanron.ganker.db.entity.SaveGank;
import com.sanron.ganker.event.CollectionUpdateEvent;
import com.sanron.ganker.event.HistoryUpdateEvent;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.Common;
import com.sanron.ganker.util.RxBus;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.DividerItemDecoration;
import com.sanron.ganker.widget.PullAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-4.
 */
public class SaveGankManagerActivity extends BaseActivity implements PullAdapter.OnLoadMoreListener, Toolbar.OnMenuItemClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_history) RecyclerView mRvHistory;

    private int mPage = 0;
    private GankAdapter mGankAdapter;
    private int mType;
    private Subscription mEventSubscription;

    private static final int PAGE_SIZE = 30;

    public static final String EXTRA_TYPE = "type";
    //浏览历史
    public static final int TYPE_HISTORY = 1;
    //收藏
    public static final int TYPE_COLLECTION = 2;

    private class LoadSubscreber extends Subscriber<List<SaveGank>> {
        private boolean mRefresh;

        public LoadSubscreber(boolean refresh) {
            mRefresh = refresh;
        }

        @Override
        public void onCompleted() {
            mGankAdapter.onLoadComplete();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            mGankAdapter.onLoadComplete();
        }

        @Override
        public void onNext(List<SaveGank> saveGanks) {
            if (saveGanks.size() < PAGE_SIZE) {
                mGankAdapter.setLoadEnable(false);
            }
            if (mRefresh) {
                mGankAdapter.setData(saveGanks);
            } else {
                mGankAdapter.addAll(saveGanks);
            }
            mPage++;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_save_gank_manager);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        mType = getIntent().getIntExtra(EXTRA_TYPE, 1);
    }

    private void initView() {
        mGankAdapter = new GankAdapter(this);
        mGankAdapter.setShowCategoryIcon(true);
        mRvHistory.setLayoutManager(new LinearLayoutManager(this));
        mRvHistory.setAdapter(mGankAdapter);
        mRvHistory.addItemDecoration(new DividerItemDecoration(Common.dpToPx(this, 8)));
        mGankAdapter.setOnLoadMoreListener(this);

        if (mType == TYPE_COLLECTION) {
            mToolbar.setTitle(getString(R.string.title_collect));
            mEventSubscription = RxBus.getDefault()
                    .toObservable(CollectionUpdateEvent.class)
                    .subscribe(new Action1<CollectionUpdateEvent>() {
                        @Override
                        public void call(CollectionUpdateEvent event) {
                            loadData(true);
                        }
                    });
        } else {
            mToolbar.setTitle(getString(R.string.title_history));
            mEventSubscription = RxBus.getDefault()
                    .toObservable(HistoryUpdateEvent.class)
                    .subscribe(new Action1<HistoryUpdateEvent>() {
                        @Override
                        public void call(HistoryUpdateEvent event) {
                            loadData(true);
                        }
                    });
        }
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        onLoad();
    }

    @Override
    protected void onDestroy() {
        if (!mEventSubscription.isUnsubscribed()) {
            mEventSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, getString(R.string.menu_clear))
                .setIcon(R.mipmap.ic_delete_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    private void loadData(boolean refresh) {
        if (refresh) {
            mPage = 0;
            mGankAdapter.setData(null);
        }
        Observable<List<SaveGank>> observable;
        if (mType == TYPE_COLLECTION) {
            observable = GankerDB.get(this).getCollectionsTableHelper()
                    .getByPage(PAGE_SIZE, mPage + 1);
        } else {
            observable = GankerDB.get(this).getHistoryTableHelper()
                    .getByPage(PAGE_SIZE, mPage + 1);
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadSubscreber(refresh));
    }

    @Override
    public void onLoad() {
        loadData(false);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (mGankAdapter.getRealItemCount() == 0) {
            ToastUtil.shortShow(getString(R.string.empty_list));
            return true;
        }
        String msg;
        if (mType == TYPE_COLLECTION) {
            msg = getString(R.string.cancel_all_collection);
        } else {
            msg = getString(R.string.clear_history);
        }
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Observable<Integer> observable;
                        if (mType == TYPE_COLLECTION) {
                            observable = GankerDB.get(SaveGankManagerActivity.this)
                                    .getCollectionsTableHelper()
                                    .deleteAll();

                        } else {
                            observable = GankerDB.get(SaveGankManagerActivity.this)
                                    .getHistoryTableHelper()
                                    .deleteAll();
                        }
                        observable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(Integer integer) {
                                        if (integer > 0) {
                                            loadData(true);
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
        return true;
    }
}
