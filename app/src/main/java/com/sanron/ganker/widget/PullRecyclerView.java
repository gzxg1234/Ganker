package com.sanron.ganker.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sanron on 16-6-28.
 */
public class PullRecyclerView extends RecyclerView {

    private boolean mIsLoading = false;
    private boolean mLoadEnable = true;
    private OnLoadMoreListener mOnLoadMoreListener;

    public PullRecyclerView(Context context) {
        this(context, null);
    }

    public PullRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LayoutManager lm = recyclerView.getLayoutManager();
                int lastVisiablePosition = NO_POSITION;
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
                    throw new IllegalStateException("not support " + lm.getClass());
                }
                if (lastVisiablePosition != NO_POSITION
                        && lastVisiablePosition >= getAdapter().getItemCount() - 1
                        && mLoadEnable) {
                    setLoading(true);
                }
            }
        });
    }


    public boolean isLoadEnable() {
        return mLoadEnable;
    }

    public void setLoadEnable(boolean loadEnable) {
        mLoadEnable = loadEnable;
        if (!mLoadEnable) {
            setLoading(false);
        }
    }

    public void setLoading(boolean isLoading) {
        if (mIsLoading == isLoading) {
            return;
        }

        this.mIsLoading = isLoading;
        Adapter adapter = getAdapter();
        if (adapter instanceof PullAdapter) {
            if (mIsLoading) {
                adapter.notifyItemInserted(adapter.getItemCount());
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoad();
                }
            } else {
                adapter.notifyItemRemoved(adapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoad();
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public static abstract class PullAdapter<VH extends ViewHolder> extends RecyclerView.Adapter {

        private View mFooterView;
        private PullRecyclerView mPullRecyclerView;
        private static final int TYPE_FOOTER = -1;

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            if (!(recyclerView instanceof PullRecyclerView)) {
                throw new IllegalStateException("only used in PullRecyclerView");
            }
            mPullRecyclerView = (PullRecyclerView) recyclerView;
            final LayoutManager lm = recyclerView.getLayoutManager();
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
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            LayoutParams lp = (LayoutParams) holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && isFooter(holder.getAdapterPosition())) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }

        private boolean isFooter(int position) {
            return getItemViewType(position) == TYPE_FOOTER;
        }

        public abstract VH onCreateRealViewHolder(ViewGroup parent, int viewType);

        public abstract void onBindRealViewHolder(VH holder, int position);

        public abstract int getRealItemCount();

        public abstract View onCreateFooterView(ViewGroup parent);

        public int getReadlItemViewType(int position) {
            return 0;
        }

        @Override
        public final int getItemViewType(int position) {
            if (mPullRecyclerView.isLoading()
                    && position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                return getReadlItemViewType(position);
            }
        }

        @Override
        public final void onBindViewHolder(ViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type != TYPE_FOOTER) {
                onBindRealViewHolder((VH) holder, position);
            }
        }

        @Override
        public final int getItemCount() {
            int itemCount = getRealItemCount();
            if (itemCount > 0
                    && mPullRecyclerView.isLoading()) {
                return 1 + itemCount;
            }
            return itemCount;
        }

        //
//        public boolean isEnableLoad() {
//            return mEnableLoad;
//        }
//
//        public void setEnableLoad(boolean enableLoad) {
//            if (enableLoad == mEnableLoad) {
//                return;
//            }
//            mEnableLoad = enableLoad;
//            if (!mEnableLoad) {
//                if (getRealItemCount() > 0) {
//                    notifyItemRemoved(getItemCount() - 1);
//                }
//            } else if (getRealItemCount() > 0) {
//                notifyItemInserted(getItemCount() - 1);
//            }
//        }
        static class FooterHolder extends ViewHolder {

            public FooterHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
