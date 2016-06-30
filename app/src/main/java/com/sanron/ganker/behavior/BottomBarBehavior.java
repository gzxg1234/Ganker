package com.sanron.ganker.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sanron on 16-6-28.
 */
public class BottomBarBehavior extends CoordinatorLayout.Behavior<View> {

    public BottomBarBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0) {
            setVisiable(child, false);
        } else if (dyConsumed < 0) {
            setVisiable(child, true);
        }
    }

    private void setVisiable(View v, boolean visiable) {
        v.animate().cancel();
        v.animate()
                .translationY(visiable ? 0 : v.getHeight())
                .setDuration(300)
                .start();
    }
}
