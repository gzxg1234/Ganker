package com.sanron.ganker.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.ImageUtil;
import com.sanron.ganker.util.PermissionUtil;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PermissionDialog;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-2.
 */
public class MeizhiPicActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    @BindView(R.id.root) ViewGroup mRoot;
    @BindView(R.id.iv_img) ImageView mImageView;
    @BindView(R.id.appbar) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab_save) FloatingActionButton mFabSave;

    private boolean isHide;
    private Gank mGank;

    public static final String TRANSITION_IMG = "img";
    public static final String EXTRA_GANK = "gank";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSystemBarTintManager.setStatusBarTintEnabled(false);
        setContentView(R.layout.activity_meizhi_show);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mToolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(mToolbar);
        ViewCompat.setTransitionName(mImageView, TRANSITION_IMG);
        SystemBarTintManager.SystemBarConfig systemBarConfig = mSystemBarTintManager.getConfig();
        mRoot.setPadding(
                mRoot.getPaddingLeft(),
                mRoot.getPaddingTop(),
                mRoot.getPaddingRight() + systemBarConfig.getPixelInsetRight(),
                mRoot.getPaddingBottom() + systemBarConfig.getPixelInsetBottom());
        mAppBarLayout.setPadding(
                mAppBarLayout.getPaddingTop(),
                mAppBarLayout.getPaddingTop() + systemBarConfig.getPixelInsetTop(false),
                mAppBarLayout.getPaddingRight(),
                mAppBarLayout.getPaddingBottom());
    }

    private void initData() {
        Intent intent = getIntent();
        mGank = (Gank) intent.getSerializableExtra(EXTRA_GANK);
        Glide.with(this)
                .load(mGank.getUrl())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mImageView.setImageDrawable(resource);
                        return true;
                    }
                })
                .preload();
    }

    @OnClick(R.id.iv_img)
    public void onPicClick() {
        if (!isHide) {
            if (Build.VERSION.SDK_INT >= 16) {
                mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
            animateHide();
            mFabSave.hide();
            isHide = true;
        } else {
            mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            animateShow();
            mFabSave.show();
            isHide = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    save();
                }
            }
        }
    }

    private static final int PERMISSION_REQ_CODE = 100;

    @OnClick(R.id.fab_save)
    public void onSaveClick() {
        if (PermissionUtil.isMarshmallow()
                && !PermissionUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new PermissionDialog(this)
                    .setMessage("应用保存图片,需要授予以下权限")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MeizhiPicActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_REQ_CODE);
                        }
                    })
                    .show();
        } else {
            save();
        }
    }

    private void save() {
        Bitmap bmp = ((GlideBitmapDrawable) mImageView.getDrawable()).getBitmap();
        try {
            URL url = new URL(mGank.getUrl());
            String urlFile = url.getFile();
            int index = urlFile.lastIndexOf('/');
            index = (index == -1 ? 0 : index);
            String fileName = urlFile.substring(index);
            ImageUtil.saveBitmap(bmp, fileName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if (s == null) {
                                ToastUtil.longShow("保存失败");
                            } else {
                                ToastUtil.longShow("保存成功,保存路径" + s);
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void animateShow() {
        mAppBarLayout
                .animate().cancel();
        mAppBarLayout.setVisibility(View.VISIBLE);
        mAppBarLayout
                .animate()
                .translationY(0)
                .setDuration(300)
                .setListener(null)
                .start();
    }

    private void animateHide() {
        mAppBarLayout
                .animate().cancel();
        mAppBarLayout
                .animate()
                .translationY(-mAppBarLayout.getHeight())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAppBarLayout.setVisibility(View.INVISIBLE);
                    }
                })
                .start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "分享").setIcon(R.mipmap.ic_share_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return false;
    }
}
