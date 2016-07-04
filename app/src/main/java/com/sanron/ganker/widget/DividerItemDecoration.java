package com.sanron.ganker.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sanron on 16-6-28.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {


    private int mDividerHeight;

    public DividerItemDecoration(int dividerHeight) {
        mDividerHeight = dividerHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == 0) {
            outRect.set(0, mDividerHeight, 0, mDividerHeight);
        } else {
            outRect.set(0, 0, 0, mDividerHeight);
        }
    }
}
