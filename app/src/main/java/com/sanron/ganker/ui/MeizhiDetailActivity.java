package com.sanron.ganker.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jakewharton.salvage.RecyclingPagerAdapter;
import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.Common;
import com.sanron.ganker.util.ImageUtil;
import com.sanron.ganker.util.PermissionUtil;
import com.sanron.ganker.util.ShareUtil;
import com.sanron.ganker.util.ToastUtil;
import com.sanron.ganker.widget.PermissionDialog;

import java.io.File;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-2.
 */
public class MeizhiDetailActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    @BindView(R.id.root) ViewGroup mRoot;
    @BindView(R.id.appbar) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab_save) FloatingActionButton mFabSave;
    @BindView(R.id.pager_img) ViewPager mViewPager;

    private boolean mIsFullScreen = false;
    private Gank[] mGanks;
    private ImgPagerAdapter mPagerAdapter;
    private boolean mIsLoadedStartPic = false;
    private ImageView mCurrentImageView = null;
    private boolean mIsReturning;
    private int mStartPosition;
    private int mEndPosition;

    public static final String ELEMENT_IMG = "img";
    public static final String EXTRA_GANKS = "ganks";
    public static final String EXTRA_START_POS = "start_position";
    public static final String EXTRA_END_POS = "end_position";
    private static final int PERMISSION_REQ_CODE = 100;

    private SharedElementCallback mSharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReturning) {
                sharedElements.put(ELEMENT_IMG, mCurrentImageView);
                mIsReturning = false;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_meizhi_detail);
        ButterKnife.bind(this);
        setEnterSharedElementCallback(mSharedElementCallback);
        ActivityCompat.postponeEnterTransition(this);
        initData();
        initView();
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_END_POS, mEndPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        mAppBarLayout.setPadding(
                mAppBarLayout.getPaddingTop(),
                mAppBarLayout.getPaddingTop() + Common.getStatusBarHeight(this),
                mAppBarLayout.getPaddingRight(),
                mAppBarLayout.getPaddingBottom());

        mPagerAdapter = new ImgPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mStartPosition);

        mToolbar.setTitle((mStartPosition + 1) + "/" + mGanks.length);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAfterTransition(MeizhiDetailActivity.this);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        Object[] os = (Object[]) intent.getSerializableExtra(EXTRA_GANKS);
        mGanks = new Gank[os.length];
        System.arraycopy(os, 0, mGanks, 0, os.length);
        mStartPosition = intent.getIntExtra(EXTRA_START_POS, 0);
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

    @OnClick(R.id.fab_save)
    public void onSaveClick() {
        if (PermissionUtil.isMarshmallow()
                && !PermissionUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new PermissionDialog(this)
                    .setMessage(getString(R.string.permission_rationale_stroge_save_img))
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MeizhiDetailActivity.this,
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
        ImageUtil.saveImg(this, mGanks[mEndPosition].getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        ToastUtil.longShow(getString(R.string.save_meizhi_success, s));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ToastUtil.shortShow(getString(R.string.save_meizhi_failed));
                    }
                });
    }


    private void noFullScreen() {
        mAppBarLayout
                .animate().cancel();
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mAppBarLayout.setVisibility(View.VISIBLE);
        mAppBarLayout
                .animate()
                .translationY(0)
                .setDuration(300)
                .setListener(null)
                .start();
        mFabSave.show();
        mIsFullScreen = false;
    }

    private void fullScreen() {
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
                        if (Build.VERSION.SDK_INT >= 16) {
                            mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                    }
                })
                .start();
        mFabSave.hide();
        mIsFullScreen = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "分享").setIcon(R.mipmap.ic_share_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ImageUtil.saveImg(this, mGanks[mEndPosition].getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        ShareUtil.shareImg(MeizhiDetailActivity.this,
                                getString(R.string.share_meizhi_title),
                                getString(R.string.share_meizhi_subject),
                                getString(R.string.share_meizhi_text),
                                Uri.fromFile(new File(s)));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        ToastUtil.shortShow(getString(R.string.share_meizhi_failed));
                    }
                });
        return true;
    }


    class ImgPagerAdapter extends RecyclingPagerAdapter implements View.OnClickListener {

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentImageView = (ImageView) object;
            mEndPosition = position;
            mToolbar.setTitle((position + 1) + "/" + mGanks.length);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup container) {
            ImageView imageView = (ImageView) convertView;
            if (imageView == null) {
                imageView = new ImageView(MeizhiDetailActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            imageView.setOnClickListener(this);
            imageView.setImageBitmap(null);
            final ImageView finalImageView = imageView;
            Glide.with(MeizhiDetailActivity.this)
                    .load(mGanks[position].getUrl())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            ActivityCompat.startPostponedEnterTransition(MeizhiDetailActivity.this);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            finalImageView.setImageDrawable(resource);
                            if (!mIsLoadedStartPic && position == mStartPosition) {
                                ViewCompat.setTransitionName(finalImageView, ELEMENT_IMG);
                                mIsLoadedStartPic = true;
                                ActivityCompat.startPostponedEnterTransition(MeizhiDetailActivity.this);
                            }
                            return true;
                        }
                    })
                    .preload();
            return imageView;
        }

        @Override
        public int getCount() {
            return mGanks.length;
        }

        @Override
        public void onClick(View v) {
            if (mIsFullScreen) {
                noFullScreen();
            } else {
                fullScreen();
            }
        }
    }
}
