package com.sanron.ganker.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.widget.PullRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sanron on 16-7-2.
 */
public class MeizhiAdapter extends PullRecyclerView.PullAdapter<MeizhiAdapter.Holder> {

    private Context mContext;
    private List<Gank> mItems = new ArrayList<>();

    public MeizhiAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<Gank> data) {
        mItems.clear();
        if (data != null) {
            mItems.addAll(data);
        }
        notifyDataSetChanged();
    }

    public Gank getItem(int position) {
        return mItems.get(position);
    }

    public void addData(List<Gank> data) {
        if (data != null) {
            mItems.addAll(data);
            notifyDataSetChanged();
        }
    }

    public List<Gank> getData() {
        return mItems;
    }

    @Override
    public Holder onCreateRealViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_meizhi_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindRealViewHolder(final Holder holder, final int position) {
        holder.setGank(mItems.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getRealItemCount() {
        return mItems.size();
    }

    @Override
    public View onCreateFooterView(ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(R.layout.loading_footer_layout, parent, false);
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_img)
        public ImageView ivImg;
        public Gank gank;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setGank(Gank gank) {
            this.gank = gank;
            Glide.clear(ivImg);
            Glide.with(mContext)
                    .load(gank.getUrl())
                    .placeholder(new ColorDrawable(Color.WHITE))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivImg);
        }
    }
}
