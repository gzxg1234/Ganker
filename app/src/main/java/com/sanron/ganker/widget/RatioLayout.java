package com.sanron.ganker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sanron.ganker.R;


/**
 * 长宽比例布局，设定长宽比
 * Created by sanron on 16-3-18.
 */
public class RatioLayout extends FrameLayout {

    /**
     * 长还是宽
     */
    private int mType;
    /**
     * 比例
     */
    private float mRatio;

    public static final int TYPE_WIDTH = 1;
    public static final int TYPE_HEIGHT = 2;

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public float getRatio() {
        return mRatio;
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout);
        mType = ta.getInt(R.styleable.RatioLayout_type, 0);
        mRatio = ta.getFloat(R.styleable.RatioLayout_ratio, 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        if (mType == TYPE_WIDTH) {
            int width = (int) (getMeasuredHeight() * mRatio);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        } else if (mType == TYPE_HEIGHT) {
            int height = (int) (getMeasuredWidth() * mRatio);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
