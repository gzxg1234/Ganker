package com.sanron.ganker.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sanron.ganker.Ganker;
import com.sanron.ganker.R;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.db.CollectionGank;
import com.sanron.ganker.db.GankerDB;
import com.sanron.ganker.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by sanron on 16-6-28.
 */
public class GankWebActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.webview)
    WebView mWebView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_desc)
    TextView mTvDesc;

    @BindView(R.id.iv_favorite)
    ImageView mIvFavorite;

    private Gank mGank;
    private Toast mToast;
    private long collectId = -1;
    private int collectState = UN_CHECK;
    private static final int UN_CHECK = 0;
    private static final int COLLECTED = 1;
    private static final int UN_COLLECTED = 2;

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
            System.out.println(newProgress);
            mProgressBar.setProgress(newProgress);
        }
    }

    public class LocalWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
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
        public void onPageStarted(final WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.animate().cancel();
            mProgressBar.setTranslationY(0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gank_webview);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mGank = (Gank) intent.getSerializableExtra(ARG_GANK);

        GankerDB gankerDB = ((Ganker) getApplication()).getDB();
        mToolbar.setTitle(mGank.who);
        setSupportActionBar(mToolbar);
        mTvDesc.setText(mGank.desc);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebView.loadUrl(mGank.url);
        mWebView.setWebChromeClient(new LocalWebChromeClient());
        mWebView.setWebViewClient(new LocalWebViewClient());

        gankerDB.getCollectionByGankId(mGank.gankId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CollectionGank>() {
                    @Override
                    public void call(CollectionGank collectionGank) {
                        if (collectionGank != null) {
                            setCollectState(COLLECTED);
                            collectId = collectionGank.id;
                        } else {
                            setCollectState(UN_COLLECTED);
                        }
                    }
                });
    }

    public void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @OnClick(R.id.iv_favorite)
    public void onClickFavorite(View v) {
        GankerDB gankerDB = ((Ganker) getApplication()).getDB();
        switch (collectState) {
            case COLLECTED: {
                gankerDB.deleteCollectionById(collectId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (aBoolean) {
                                    setCollectState(UN_COLLECTED);
                                    showToast("取消收藏成功");
                                }
                            }
                        });
            }
            break;
            case UN_COLLECTED: {
                gankerDB.addCollection(mGank)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (aLong > -1) {
                                    collectId = aLong;
                                    setCollectState(COLLECTED);
                                    showToast("收藏成功");
                                }
                            }
                        });
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
