package com.totalboron.jay.labeled;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LinearLayoutCustom extends LinearLayout
{
    public LinearLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public LinearLayoutCustom(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LinearLayoutCustom(Context context)
    {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return true;
    }
}
