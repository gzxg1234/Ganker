package com.sanron.ganker.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by sanron on 16-6-28.
 */
public abstract class BaseFragment extends Fragment {

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

    public void initView(View root, Bundle savedInstanceState) {
    }

    public abstract int getLayoutId();
}
