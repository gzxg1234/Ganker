package com.sanron.ganker.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sanron.ganker.R;
import com.sanron.ganker.model.GankerRetrofit;
import com.sanron.ganker.model.entity.Gank;
import com.sanron.ganker.model.entity.GankData;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.DimenTool;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.view.DividerItemDecoration;
import com.sanron.ganker.view.PullRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private String category;
    private boolean isLoaded;
    private boolean isInited;

    private static final int PAGE_SIZE = 20;
    public static final String ARG_CATEGORY = "category";


    private Subscription mSubscription;

    public static GankPagerFragment newInstance(String category) {
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
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
        category = args.getString(ARG_CATEGORY);
    }

    @Override
    public int getLayoutId() {
        return R.layout.gank_pager_fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null
                && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    public void load() {
        mSubscription = GankerRetrofit.get()
                .getGankService()
                .getByCategory(category, PAGE_SIZE, page + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankData>() {
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
                    public void onNext(GankData gankData) {
                        if (gankData.results == null) {
                            return;
                        }
                        if (mGanks == null) {
                            mGanks = new ArrayList<>();
                        }
                        mGanks.addAll(gankData.results);
                        mGankAdapter.setData(mGanks);
                        if (gankData.results.size() < PAGE_SIZE) {
                            mGankAdapter.setEnableLoad(false);
                        }
                        page++;
                    }
                });
    }

    @Override
    public void onRefresh() {
        isLoaded = false;
        page = 0;
        mGanks = null;
        mGankAdapter.setEnableLoad(true);
        load();
    }

    @Override
    public void onLoad() {
        load();
    }

    public static class GankAdapter extends PullRecyclerView.PullAdapter<GankAdapter.Holder> {

        private Context mContext;
        private List<Gank> mGanks = new ArrayList<>();

        public GankAdapter(Context context) {
            mContext = context;
        }

        public void setData(List<Gank> data) {
            mGanks.clear();
            if (data != null) {
                mGanks.addAll(data);
            }
            notifyDataSetChanged();
        }

        public void addData(List<Gank> data) {
            if (data != null) {
                mGanks.addAll(data);
                notifyDataSetChanged();
            }
        }

        @Override
        public Holder onCreateRealViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_gank_item, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindRealViewHolder(Holder holder, int position) {
            final Gank gank = mGanks.get(position);
            holder.setGank(gank);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GankWebActivity.startFrom(mContext, gank);
                }
            });
        }

        @Override
        public int getRealItemCount() {
            return mGanks.size();
        }

        @Override
        public View onCreateFooterView(ViewGroup parent) {
            return LayoutInflater.from(mContext).inflate(R.layout.loading_footer_layout, parent, false);
        }

        static class Holder extends RecyclerView.ViewHolder {
            @BindView(R.id.tv_desc)
            TextView tvDesc;
            @BindView(R.id.tv_published_time)
            TextView tvPublishedTime;
            @BindView(R.id.tv_who)
            TextView tvWho;
            Gank gank;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void setGank(Gank gank) {
                this.gank = gank;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                tvDesc.setText(gank.desc);
                tvPublishedTime.setText(sdf.format(gank.publishedAt));
                tvWho.setText(gank.who);
            }
        }
    }
}
