package com.sanron.ganker.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.BaseData;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.ui.base.BaseActivity;
import com.sanron.ganker.util.ToastUtil;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-7-5.
 */
public class SubmitGankActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.et_desc) EditText mEtDesc;
    @BindView(R.id.et_url) EditText mEtUrl;
    @BindView(R.id.et_who) EditText mEtWho;
    @BindView(R.id.sp_type) NiceSpinner mSpinner;

    private static final String[] TYPES = new String[]{
            Gank.CATEGORY_ANDROID,
            Gank.CATEGORY_IOS,
            Gank.CATEGORY_APP,
            Gank.CATEGORY_EXPAND,
            Gank.CATEGORY_FRONT_END,
            Gank.CATEGORY_FULI,
            Gank.CATEGORY_VEDIO,
            Gank.CATEGORY_RECMD
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_gank);
        ButterKnife.bind(this);
        initView();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {
        String desc = mEtDesc.getText().toString();
        String url = mEtUrl.getText().toString();
        String who = mEtWho.getText().toString();
        if (checkInput()) {
            addSub(GankerRetrofit.get()
                    .getGankService()
                    .addGank(desc, url, who, TYPES[mSpinner.getSelectedIndex()], true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<BaseData>() {
                        @Override
                        public void call(BaseData baseData) {
                            if (baseData.error) {
                                ToastUtil.shortShow(baseData.msg);
                            } else {
                                new AlertDialog.Builder(SubmitGankActivity.this)
                                        .setMessage(getString(R.string.submit_success))
                                        .setCancelable(true)
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                finish();
                                            }
                                        }).show();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            ToastUtil.shortShow(getString(R.string.submit_failed));
                        }
                    }));
        }
    }

    private boolean checkInput() {
        if (TextUtils.isEmpty(mEtDesc.getText().toString())) {
            mEtDesc.setError(getString(R.string.input_error_desc));
            return false;
        }
        if (TextUtils.isEmpty(mEtUrl.getText().toString())) {
            mEtUrl.setError(getString(R.string.input_error_url));
            return false;
        }
        if (TextUtils.isEmpty(mEtWho.getText().toString())) {
            mEtWho.setError(getString(R.string.input_error_who));
            return false;
        }
        if (mSpinner.getSelectedIndex() == NiceSpinner.INVAILD_INDEX) {
            ToastUtil.shortShow(getString(R.string.select_submit_type));
            return false;
        }
        return true;
    }

    private void initView() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSpinner.setHint(getString(R.string.select_submit_type));
        mSpinner.attachDataSource(Arrays.asList(TYPES));
    }
}
