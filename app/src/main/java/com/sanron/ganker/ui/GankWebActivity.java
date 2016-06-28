package com.sanron.ganker.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sanron.ganker.R;
import com.sanron.ganker.model.entity.Gank;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sanron on 16-6-28.
 */
public class GankWebActivity extends AppCompatActivity {

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
            mProgressBar.setProgress(0);
            mProgressBar.animate().cancel();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gank_webview);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mGank = (Gank) intent.getSerializableExtra(ARG_GANK);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gank_web_menu, menu);
        return true;
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
