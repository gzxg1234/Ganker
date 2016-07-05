package com.sanron.ganker.widget;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

public abstract class PullAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    private boolean mIsLoading = false;
    private boolean mLoadEnable = true;
    private boolean mLastItemVisiable = true;
    private OnLoadMoreListener mOnLoadMoreListener;
    private View mFooterView;
    private static final int TYPE_FOOTER = -1;

    public abstract VH onCreateRealViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindRealViewHolder(VH holder, int position);

    public abstract int getRealItemCount();

    public abstract View onCreateFooterView(ViewGroup parent);

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            mFooterView = onCreateFooterView(parent);
            return new FooterHolder(mFooterView);
        } else {
            return onCreateRealViewHolder(parent, viewType);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        final RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof GridLayoutManager) {
            ((GridLayoutManager) lm).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isFooter(position)) {
                        return ((GridLayoutManager) lm).getSpanCount();
                    }
                    return 1;
                }
            });
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                int lastVisiablePosition = RecyclerView.NO_POSITION;
                if (lm instanceof StaggeredGridLayoutManager) {
                    int[] positions = ((StaggeredGridLayoutManager) lm).findLastVisibleItemPositions(null);
                    if (positions != null) {
                        lastVisiablePosition = positions[0];
                        for (int v : positions) {
                            if (v > lastVisiablePosition) {
                                lastVisiablePosition = v;
                            }
                        }
                    }
                } else if (lm instanceof GridLayoutManager) {
                    lastVisiablePosition = ((GridLayoutManager) lm).findLastVisibleItemPosition();
                } else if (lm instanceof LinearLayoutManager) {
                    lastVisiablePosition = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
                } else {
                    throw new IllegalStateException("not support " + lm.getClass().getName());
                }
                if (lastVisiablePosition != RecyclerView.NO_POSITION
                        && lastVisiablePosition >= getRealItemCount() - 1) {
                    if (mLoadEnable
                            && !mIsLoading
                            && mOnLoadMoreListener != null
                            && !mLastItemVisiable) {
                        mLastItemVisiable = true;
                        mIsLoading = true;
                        mOnLoadMoreListener.onLoad();
                    }
                } else {
                    //当recyclerview滑动到最后时,此时如果没有网络,且只在最后一个view的高度范围内滑动,会一直触发onload
                    //设置此标识是否从最后一个view滑动到上方
                    mLastItemVisiable = false;
                }
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                && isFooter(holder.getAdapterPosition())) {
            ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
        }
    }

    private boolean isFooter(int position) {
        return getItemViewType(position) == TYPE_FOOTER;
    }

    public int getReadlItemViewType(int position) {
        return 0;
    }

    public void onLoadComplete() {
        mIsLoading = false;
    }

    @Override
    public final int getItemViewType(int position) {
        if (mLoadEnable
                && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return getReadlItemViewType(position);
        }
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type != TYPE_FOOTER) {
            onBindRealViewHolder((VH) holder, position);
        }
    }

    @Override
    public final int getItemCount() {
        int itemCount = getRealItemCount();
        if (itemCount > 0
                && mLoadEnable) {
            return 1 + itemCount;
        }
        return itemCount;
    }


    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoad();
    }

    public boolean isLoadEnable() {
        return mLoadEnable;
    }

    public void setLoadEnable(boolean loadEnable) {
        mLoadEnable = loadEnable;
        if (!mLoadEnable) {
            notifyDataSetChanged();
        }
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    static class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}