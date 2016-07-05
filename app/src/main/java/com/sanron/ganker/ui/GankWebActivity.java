package com.sanron.ganker.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.db.GankerDB;
import com.sanron.ganker.db.entity.SaveGank;
import com.sanron.ganker.event.CollectionUpdateEvent;
import com.sanron.ganker.event.HistoryUpdateEvent;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.RxBus;
import com.sanron.ganker.util.ShareUtil;
import com.sanron.ganker.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-28.
 */
public class GankWebActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.webview) WebView mWebView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.tv_desc) TextView mTvDesc;
    @BindView(R.id.iv_favorite) ImageView mIvFavorite;

    private Gank mGank;
    private long collectId = -1;
    private int collectState = UN_CHECK;
    private static final int UN_CHECK = 0;//未检查
    private static final int COLLECTED = 1;//已收藏
    private static final int UN_COLLECTED = 2;//未收藏

    private static final String ARG_GANK = "gank";

    public static void startFrom(Context context, Gank gank) {
        Intent intent = new Intent(context, GankWebActivity.class);
        intent.putExtra(ARG_GANK, gank);
        context.startActivity(intent);
    }


    public class LocalWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mProgressBar.setProgress(newProgress);
        }
    }

    public class LocalWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            animateHideProgress();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            animateHideProgress();
            ToastUtil.shortShow(getString(R.string.web_error));
        }

        @Override
        public void onPageStarted(final WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            animateShowProgress();
        }
    }

    private void animateShowProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.animate().cancel();
        mProgressBar.setTranslationY(0);
    }

    private void animateHideProgress() {
        mProgressBar.animate().cancel();
        mProgressBar.animate()
                .translationY(-mProgressBar.getHeight())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .start();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gank_webview);
        ButterKnife.bind(this);
        initData();
        initView();
        //添加浏览历史
        GankerDB.get(this)
                .getHistoryTableHelper()
                .add(mGank)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (aLong > -1) {
                            RxBus.getDefault().post(new HistoryUpdateEvent());
                        }
                    }
                });
        //检查是否收藏
        GankerDB.get(this)
                .getCollectionsTableHelper()
                .getByGankId(mGank.getGankId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SaveGank>() {
                    @Override
                    public void call(SaveGank saveGank) {
                        if (saveGank != null) {
                            setCollectState(COLLECTED);
                            collectId = saveGank.id;
                        } else {
                            setCollectState(UN_COLLECTED);
                        }
                    }
                });
    }

    private void initView() {
        mToolbar.setTitle(mGank.getWho());
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvDesc.setText(mGank.getDesc());
        mTvDesc.requestFocus();
        WebSettings ws = mWebView.getSettings();
        ws.setSupportZoom(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        mWebView.loadUrl(mGank.getUrl());
        mWebView.setWebChromeClient(new LocalWebChromeClient());
        mWebView.setWebViewClient(new LocalWebViewClient());
    }

    private void initData() {
        Intent intent = getIntent();
        mGank = (Gank) intent.getSerializableExtra(ARG_GANK);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_copy_url: {
                copyUrl();
            }
            break;

            case R.id.menu_open_by_browser: {
                openByBrowser();
            }
            break;

            case R.id.menu_share: {
                shareGank();
            }
            break;

            case R.id.menu_refresh: {
                mWebView.reload();
            }
            break;
        }
        return false;
    }

    private void copyUrl() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", mGank.getUrl());
        cm.setPrimaryClip(clipData);
        ToastUtil.shortShow(getString(R.string.copy_success));
    }

    private void openByBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mGank.getUrl()));
        startActivity(intent);
    }

    private void shareGank() {
        ShareUtil.shareText(this, getString(R.string.share_gank_title),
                getString(R.string.share_gank_subject),
                getString(R.string.share_gank_text, mGank.getDesc(), mGank.getUrl()));
    }

    private void cancelCollect() {
        GankerDB.get(this)
                .getCollectionsTableHelper()
                .deleteById(collectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            setCollectState(UN_COLLECTED);
                            ToastUtil.shortShow(getString(R.string.cancel_collection_success));
                            //通知数据变化
                            RxBus.getDefault().post(new CollectionUpdateEvent());
                        }
                    }
                });
    }

    private void collect() {
        GankerDB.get(this)
                .getCollectionsTableHelper()
                .add(mGank)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (aLong > -1) {
                            collectId = aLong;
                            setCollectState(COLLECTED);
                            ToastUtil.shortShow(getString(R.string.collection_success));
                            //通知数据变化
                            RxBus.getDefault().post(new CollectionUpdateEvent());
                        }
                    }
                });
    }

    @OnClick(R.id.iv_favorite)
    public void onClickFavorite(View v) {
        switch (collectState) {
            case COLLECTED: {
                cancelCollect();
            }
            break;
            case UN_COLLECTED: {
                collect();
            }
        }
    }

    private void setCollectState(int state) {
        if (state == COLLECTED) {
            mIvFavorite.setImageResource(R.mipmap.ic_favorite_349df9_24dp);
        } else {
            mIvFavorite.setImageResource(R.mipmap.ic_favorite_border_349df9_24dp);
        }
        collectState = state;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gank_web_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.stopLoading();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
