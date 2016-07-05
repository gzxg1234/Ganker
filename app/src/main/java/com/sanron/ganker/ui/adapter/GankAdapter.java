package com.sanron.ganker.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.GankWebActivity;
import com.sanron.ganker.widget.PullAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GankAdapter extends PullAdapter<GankAdapter.Holder> {

    private Context mContext;
    private List<Gank> mGanks = new ArrayList<>();

    public GankAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<? extends Gank> data) {
        mGanks.clear();
        if (data != null) {
            mGanks.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void addAll(List<? extends Gank> data) {
        if (data != null) {
            mGanks.addAll(data);
            notifyDataSetChanged();
        }
    }

    @Override
    public Holder onCreateRealViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_gank_item, parent, false);
        Holder holder = new Holder(view);
        holder.ivIcon.setVisibility(mShowCategoryIcon ? View.VISIBLE : View.GONE);
        return holder;
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

    private boolean mShowCategoryIcon = false;

    public void setShowCategoryIcon(boolean showCategoryIcon) {
        if (showCategoryIcon == mShowCategoryIcon) {
            return;
        }
        mShowCategoryIcon = showCategoryIcon;
        notifyDataSetChanged();
    }

    @Override
    public int getRealItemCount() {
        return mGanks.size();
    }

    @Override
    public View onCreateFooterView(ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(R.layout.loading_footer_layout, parent, false);
    }

    public class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_desc)
        TextView tvDesc;
        @BindView(R.id.tv_published_time)
        TextView tvPublishedTime;
        @BindView(R.id.tv_who)
        TextView tvWho;
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        Gank gank;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private String getTimeText(Date time) {
            long pastTime = System.currentTimeMillis() - time.getTime();
            int sec = (int) (pastTime / 1000);
            if (sec > 59) {
                int min = sec / 60;
                if (min > 59) {
                    int hour = min / 60;
                    if (hour > 23) {
                        int day = hour / 24;
                        if (day > 6) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            return sdf.format(time);
                        } else {
                            return mContext.getString(R.string.days_ago, day);
                        }
                    } else {
                        return mContext.getString(R.string.hours_ago, hour);
                    }
                } else {
                    return mContext.getString(R.string.minute_ago, min);
                }
            } else {
                return mContext.getString(R.string.a_moment_ago);
            }
        }

        private int getCategoryIcon(String type) {
            switch (type) {
                case Gank.CATEGORY_IOS: {
                    return R.mipmap.ic_ios_24dp;
                }
                case Gank.CATEGORY_ANDROID: {
                    return R.mipmap.ic_android_24dp;
                }
                case Gank.CATEGORY_FRONT_END: {
                    return R.mipmap.ic_front_end_24dp;
                }
                case Gank.CATEGORY_APP: {
                    return R.mipmap.ic_app_24dp;
                }
                default: {
                    return R.mipmap.ic_expand_res_24dp;
                }
            }
        }

        public void setGank(Gank gank) {
            this.gank = gank;
            tvPublishedTime.setText(getTimeText(gank.getPublishedAt()));
            tvDesc.setText(gank.getDesc());
            tvWho.setText(gank.getWho());
            ivIcon.setImageResource(getCategoryIcon(gank.getType()));
        }
    }
}