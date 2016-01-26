package com.island.island.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.island.island.R;

/**
 * TODO: document view.
 */
public class PostView extends FrameLayout
{
    public PostView(Context context)
    {
        super(context);
        initView();
    }

    public PostView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView();
    }

    public PostView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // https://stackoverflow.com/questions/4328838/create-a-custom-view-by-inflating-a-layout
    private void initView()
    {
        View view = inflate(getContext(), R.layout.post, null);
        addView(view);
    }
}
