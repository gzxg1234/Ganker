package com.sanron.ganker.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.sanron.ganker.R;
import com.sanron.ganker.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sanron on 16-7-5.
 */
public class AboutActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_about_author) View mAboutAuthor;
    @BindView(R.id.view_check_update) View mCheckUpdate;
    @BindView(R.id.view_github) View mOpenGithub;
    @BindView(R.id.tv_version) TextView mTvVersionName;

    public static final String GITHUB = "https://github.com/gzxg1234/Ganker";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            mTvVersionName.setText("V" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @OnClick(R.id.view_github)
    public void onOpenGithub() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(GITHUB));
        startActivity(intent);
    }

    @OnClick(R.id.view_check_update)
    public void onCheckUpdate() {

    }

    @OnClick(R.id.view_about_author)
    public void onAboutAuthor() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(R.layout.dlg_author_info)
                .show();
    }
}
