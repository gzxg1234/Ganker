package com.sanron.ganker.widget;

import android.Manifest;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.ganker.R;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sanron on 16-7-2.
 */
public class PermissionDialog extends AlertDialog.Builder {

    @BindView(R.id.tv_content) TextView mTvMessage;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_next)
    TextView mTvNext;
    List<String> mPremissions;

    public PermissionDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dlg_permission_rationale, null);
        setView(view);
        setCancelable(false);
        ButterKnife.bind(this, view);
    }

    @Override
    public AlertDialog create() {
        final AlertDialog alertDialog = super.create();
        mTvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        return alertDialog;
    }

    public PermissionDialog setMessage(String msg) {
        mTvMessage.setText(msg);
        return this;
    }

    public PermissionDialog setPermissions(List<String> permissions) {
        mPremissions = permissions;
        PermissionAdapter permissionAdapter = new PermissionAdapter(getContext(), mPremissions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(permissionAdapter);
        return this;
    }

    public PermissionDialog setPermissions(String... permissions) {
        mPremissions = Arrays.asList(permissions);
        PermissionAdapter permissionAdapter = new PermissionAdapter(getContext(), mPremissions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(permissionAdapter);
        return this;
    }

    static class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.Holder> {

        private List<String> mPermissions;
        private Context mContext;


        public PermissionAdapter(Context context, List<String> permissions) {
            mContext = context;
            mPermissions = permissions;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_permission_item, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.tvPermission.setText(getPermissionText(mPermissions.get(position)));
            holder.ivImg.setImageResource(getPermissionIcon(mPermissions.get(position)));
        }

        @Override
        public int getItemCount() {
            return mPermissions == null ? 0 : mPermissions.size();
        }

        String getPermissionText(String permission) {
            switch (permission) {
                case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                    return "存储空间";
                }
                default:
                    return "";
            }
        }

        int getPermissionIcon(String permission) {
            switch (permission) {
                case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                    return R.mipmap.ic_folder_open_black_24dp;
                }
                default:
                    return 0;
            }
        }

        static class Holder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_img) ImageView ivImg;
            @BindView(R.id.tv_permission) TextView tvPermission;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
