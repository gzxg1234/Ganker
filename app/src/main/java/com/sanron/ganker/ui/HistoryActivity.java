package com.sanron.ganker.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sanron.ganker.R;
import com.sanron.ganker.db.HistoryTableHelper;
import com.sanron.ganker.db.entity.SaveGank;
import com.sanron.ganker.ui.adapter.GankAdapter;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.Common;
import com.sanron.ganker.widget.DividerItemDecoration;
import com.sanron.ganker.widget.PullRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-4.
 */
public class HistoryActivity extends BaseActivity implements PullRecyclerView.OnLoadMoreListener, Toolbar.OnMenuItemClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_history) PullRecyclerView mRvHistory;

    private int mPage = 0;
    private GankAdapter mGankAdapter;
    private HistoryTableHelper mHistoryTableHelper;

    private static final int PAGE_SIZE = 30;

    private class LoadSubscreber extends Subscriber<List<SaveGank>> {
        private boolean mRefresh;

        public LoadSubscreber(boolean refresh) {
            mRefresh = refresh;
        }

        @Override
        public void onCompleted() {
            mRvHistory.setLoading(false);
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            mRvHistory.setLoading(false);
        }

        @Override
        public void onNext(List<SaveGank> saveGanks) {
            if (saveGanks.size() < PAGE_SIZE) {
                mRvHistory.setLoadEnable(false);
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
        setContentView(R.layout.activty_history);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mHistoryTableHelper = new HistoryTableHelper(this);
        mGankAdapter = new GankAdapter(this);
        mGankAdapter.setShowCategoryIcon(true);
        mRvHistory.setLayoutManager(new LinearLayoutManager(this));
        mRvHistory.setAdapter(mGankAdapter);
        mRvHistory.addItemDecoration(new DividerItemDecoration(Common.dpToPx(this, 8)));
        mRvHistory.setOnLoadMoreListener(this);
        mRvHistory.setLoading(true);

        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "清空记录")
                .setIcon(R.mipmap.ic_delete_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    private void loadData(boolean refresh) {
        if (refresh) {
            mPage = 0;
            mGankAdapter.setData(null);
        }
        mHistoryTableHelper
                .getByPage(PAGE_SIZE, mPage + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadSubscreber(refresh));
    }

    @Override
    public void onLoad() {
        loadData(false);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("清空记录?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mHistoryTableHelper.deleteAll()
                                .subscribeOn(Schedulers.io())
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
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
        return true;
    }
}
