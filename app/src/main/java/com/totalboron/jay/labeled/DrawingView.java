package com.totalboron.jay.labeled;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;


public class DrawingView extends ImageView implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener
{
    private GestureDetector gestureDetector;
    private Context context;
    private String logging=getClass().getSimpleName();
    private Path path;
    private float initX;
    private float initY;
    private float finX;
    private float finY;
    private Bitmap bitmap;
    private Canvas backupcanvas;
    private Paint paintForBitmap;
    private Paint paint;
    private boolean gestureNormalScroll=false;
    private boolean longPress=false;
    private DrawingScene drawingScene;
    private TextPaint textPaint;
    private final float lineSpacing=1.0f;
    private final int MAX_WIDTH_TEXT=610;
    private float factor=1;
    private final int STROKE_DEFAULT=5;
    private final int TEXT_SIZE=75;


    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initialize();
    }

    public DrawingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context=context;
        initialize();
    }

    public DrawingView(Context context,int imageWidth,int imageHeight,int screenWidth)
    {
        super(context);
        this.context=context;
        initialize();
        factor=imageWidth/(float) screenWidth;

    }

    public void setDrawingScene(DrawingScene drawingScene)
    {
        this.drawingScene = drawingScene;
    }

    private void initialize()
    {
        Log.d(logging,"Called initialize");
        int color=Color.RED;
        gestureDetector=new GestureDetector(context,this);
        gestureDetector.setOnDoubleTapListener(this);
        path=new Path();
        initX=initY=finY=finX=0f;
        paintForBitmap=new Paint(Paint.DITHER_FLAG);
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(STROKE_DEFAULT);
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        textPaint=new TextPaint();
        textPaint.setColor(color);
        textPaint.setTextSize(TEXT_SIZE);
        Log.d(logging,"End of initialize");
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        Log.d(logging,"Before the super call");
        super.onDraw(canvas);
        Log.d(logging, bitmap == null ? "Not there" : "There");
        canvas.drawBitmap(bitmap, 0, 0, paintForBitmap);
        if (gestureNormalScroll | longPress)
        {
            canvas.drawPath(path, paint);
        }
        Log.d(logging,"End of onDraw");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(logging,w+"+"+h);
        bitmap=Bitmap.createBitmap(w,h==0?2:h, Bitmap.Config.ARGB_8888);
        backupcanvas=new Canvas(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d(logging,"in Touch Event");
        gestureDetector.onTouchEvent(event);
        drawingScene.checkForOpenFragment();
        if (longPress)
        {
            if (event.getAction()==MotionEvent.ACTION_MOVE)
            {
                path.reset();
                path.addCircle(initX, initY, getRadius(event), Path.Direction.CW);
                invalidate();
            }
        }






        if (event.getAction()==MotionEvent.ACTION_UP)
        {
            if (gestureNormalScroll) drawingScene.loadFragment(null);
            gestureNormalScroll=longPress=false;
            backupcanvas.drawPath(path,paint);
            path.reset();
        }
        return true;
    }

    private int getRadius(MotionEvent event)
    {
        return (int)Math.sqrt(Math.pow(initX - event.getX(),2)+Math.pow(initY - event.getY(),2));

    }

    public void reset()
    {
        backupcanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void sendMessage(String string)
    {
        //Handle the message here
        //Todo:Correct for landscape position in a different function and arrange all these into functions also
        //Todo: Correct the value on the basis of left also
        float degree=getAngle(initX, initY, finX, finY);
        degree=(float)Math.toDegrees(degree);
        float degreeAbs=Math.abs(degree);
        boolean inverse=isInverse(degree);
        int minWidth=minWidthAdjustment(string,degreeAbs);


        StaticLayout staticLayout=new StaticLayout(string,textPaint,minWidth, Layout.Alignment.ALIGN_CENTER,lineSpacing,0f,false);
        int line=staticLayout.getLineCount();
        float height=(textPaint.ascent()-textPaint.descent())*line+line*lineSpacing;
        if (degreeAbs>=0&&degreeAbs<=45)
        {
            //Todo: Display vertical center
            height/=2;
            finY=finY+height;
            if (inverse)finX=finX-minWidth;
        }
        else
        {
            //Todo: Display horizontal center
            finX=finX-minWidth/2;
            if (inverse)finY=finY+height;
        }
        backupcanvas.translate(finX, finY);
        staticLayout.draw(backupcanvas);
        backupcanvas.translate(-finX, -finY);
        textPaint.setTextSize(75);
        Log.d(logging,inverse+"");
        invalidate();
    }

    private int minWidthAdjustment(String string,float degreeAbs)
    {
        int minWidth;
        float availableSpace;
        minWidth=getMaxWidth(string,MAX_WIDTH_TEXT);
        if (degreeAbs>=0&&degreeAbs<=45)
        {
            if (finX>initX)
            availableSpace = getWidth() - finX;
            else availableSpace=finX;
        }
        else return paraAdjustment(string,minWidth);
        if (availableSpace<minWidth)
        {
            if (availableSpace>400)
            {
                minWidth=(int)availableSpace;
            }
            else if (availableSpace>250)
            {
                textPaint.setTextSize(55);
                minWidth=getMaxWidth(string, (int) availableSpace);
            }
            else
            {
                textPaint.setTextSize(45);
                //Todo: Manage For very small space
                minWidth=getMaxWidth(string, (int) availableSpace);
            }
        }
        return minWidth;
    }
    private int paraAdjustment(String string,int minWidth)
    {
        //Here finX is the spaceLeft so no more allocation
        float spaceRight=getWidth()-finX;
        if (finX<minWidth/2||spaceRight<minWidth/2)
        {
            Log.d(logging,"Inner entered");
            textPaint.setTextSize(45);
            minWidth=getMaxWidth(string,(int)(finX<spaceRight?finX:spaceRight));
            return minWidth;
        }
        return minWidth;
    }


    private boolean isInverse(double degree)
    {
        if (degree>=0&&finX<=initX)
            return true;
        else if (degree>=-45&&finX<=initX)
            return true;
        else if (degree<=-45&&finX>=initX)
            return true;
        return false;
    }




    private int getMaxWidth(String s,int maxLength)
    {
        String[] split=s.split(" ");
        float length=0;
        float maxLineLength=0;
        float currentWord=0;
        float spaceVal=textPaint.measureText(" ");
        for (int i=0;i<split.length;i++)
        {
            currentWord=textPaint.measureText(split[i]);
            if (currentWord>maxLength) return maxLength;
            length=length+currentWord;
            if (length>maxLength)
            {
                if (length-currentWord>maxLineLength)
                    maxLineLength=length-currentWord;
                i--;
                length=0;
                continue;

            }
            if (i<split.length-1)length+=spaceVal;
        }
        if (maxLineLength==0) return (int)length;
        return (int)maxLineLength;
    }






    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        drawingScene.singleTapDone();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        reset();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        initX=finX=e.getX();
        initY=finY=e.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        gestureNormalScroll=true;
        path.reset();
        getArrow(e1.getX(), e1.getY(), e2.getX(), e2.getY());
        path.moveTo(e1.getX(), e1.getY());
        path.lineTo(e2.getX(), e2.getY());
        finX=e2.getX();
        finY=e2.getY();
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        longPress=true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return true;
    }


    private void getArrow(float x1,float y1,float x2,float y2)
    {
        int length=60;
        double pie=Math.PI/8;
        double degree=getAngle(x1,y1,x2,y2);
        if (x2-x1<0) {

            degree=-Math.PI+degree;
        }
        path.moveTo(x1, y1);
        path.lineTo(x1 + (float) (length * Math.cos(degree + pie)), y1 + (float) (length * Math.sin(degree + pie)));
        path.moveTo(x1, y1);
        path.lineTo(x1+(float)(length*Math.cos(degree-pie)),y1+(float)(length*Math.sin(degree-pie)));

    }
    private float getAngle(float x1,float y1,float x2,float y2)
    {
        return (float)Math.atan((double)(y2-y1)/(x2-x1));
    }

    public void setBlackColor()
    {
        textPaint.setColor(getResources().getColor(R.color.black));
        paint.setColor(getResources().getColor(R.color.black));
    }

    public void setRedColor()
    {
        textPaint.setColor(getResources().getColor(R.color.holo_red_dark));
        paint.setColor(getResources().getColor(R.color.holo_red_dark));
    }

    public void setGreenColor()
    {
        textPaint.setColor(getResources().getColor(R.color.holo_green_dark));
        paint.setColor(getResources().getColor(R.color.holo_green_dark));
    }

    public void setBlueColor()
    {
        textPaint.setColor(getResources().getColor(R.color.holo_blue_dark));
        paint.setColor(getResources().getColor(R.color.holo_blue_dark));
    }

    public void setWhiteColor()
    {
        textPaint.setColor(getResources().getColor(R.color.white));
        paint.setColor(getResources().getColor(R.color.white));
    }

    public void setSize(int progress)
    {
        float final_stroke=3f;
        float final_text=75f;
        if (progress>0)
        {
            final_stroke=progress/10f;
            final_text+=progress/2f;
        }
        textPaint.setTextSize(final_text);
        paint.setStrokeWidth(final_stroke);
    }
}