package com.totalboron.jay.labeled;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Jay on 14/05/16.
 */
public class MyRelativeLayout extends RelativeLayout
{
    private RelativeLayout relativeLayout = null;
    private Rect rect=new Rect();
    private MainActivity mainActivity=null;
    public MyRelativeLayout(Context context)
    {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setRelativeLayout(RelativeLayout relativeLayout)
    {
        this.relativeLayout = relativeLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (relativeLayout != null && relativeLayout.getVisibility() == VISIBLE)
            return true;
        else
            return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (relativeLayout != null && relativeLayout.getVisibility() == VISIBLE)
        {
            relativeLayout.getHitRect(rect);
            if (!rect.contains((int)event.getX(),(int)event.getY()))
            {
                mainActivity.deflate();
                return true;
            }
            else Log.d("Sizing",relativeLayout.getY()+"");
        }
            return super.onTouchEvent(event);
    }

    public void setMainActivity(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }
}
