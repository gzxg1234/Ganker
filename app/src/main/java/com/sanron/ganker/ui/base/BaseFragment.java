package com.sanron.ganker.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sanron on 16-6-28.
 */
public abstract class BaseFragment extends Fragment {

    private CompositeSubscription mCompositeSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutId();
        View root = null;
        if (layoutId != 0) {
            root = inflater.inflate(getLayoutId(), container, false);
            ButterKnife.bind(this, root);
        }
        initView(root, savedInstanceState);
        return root;
    }

    protected void addSub(Subscription subscription) {
        if (mCompositeSubscription == null
                || mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCompositeSubscription != null
                && mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

    public void initView(View root, Bundle savedInstanceState) {
    }

    public abstract int getLayoutId();
}
