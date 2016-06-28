package com.sanron.ganker.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sanron on 16-6-28.
 */
public class PullRecyclerView extends RecyclerView {

    private boolean mIsLoading = false;

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (null != mOnLoadMoreListener
                        && getAdapter() instanceof PullAdapter
                        && (((PullAdapter) getAdapter()).isEnableLoad())
                        && !mIsLoading) {
                    int lastVisiblePosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
                    if (lastVisiblePosition + 1 == getAdapter().getItemCount()) {
                        mOnLoadMoreListener.onLoad();
                        mIsLoading = true;
                    }
                }
            }
        });
    }

    private OnLoadMoreListener mOnLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoad();
    }


    public void setLoading(boolean loading) {
        mIsLoading = loading;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public static abstract class PullAdapter<VH extends ViewHolder> extends RecyclerView.Adapter {

        private View mFooterView;
        private boolean mEnableLoad = true;
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

        public abstract VH onCreateRealViewHolder(ViewGroup parent, int viewType);

        public abstract void onBindRealViewHolder(VH holder, int position);

        public abstract int getRealItemCount();

        public abstract View onCreateFooterView(ViewGroup parent);

        public int getReadlItemViewType(int position) {
            return 0;
        }

        @Override
        public final int getItemViewType(int position) {
            if (mEnableLoad
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
                    && mEnableLoad) {
                return 1 + itemCount;
            }
            return itemCount;
        }

        public boolean isEnableLoad() {
            return mEnableLoad;
        }

        public void setEnableLoad(boolean enableLoad) {
            if (enableLoad == mEnableLoad) {
                return;
            }
            mEnableLoad = enableLoad;
            if (!mEnableLoad) {
                if (getRealItemCount() > 0) {
                    notifyItemRemoved(getItemCount() - 1);
                }
            } else if (getRealItemCount() > 0) {
                notifyItemInserted(getItemCount() - 1);
            }
        }
    }

    static class FooterHolder extends ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}
