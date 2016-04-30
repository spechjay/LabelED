package com.totalboron.jay.labeled;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
    private final int STROKE_DEFAULT=5;
    private final int TEXT_SIZE=75;
    private List<Bitmap> bitmap_list;
    private List<String> tags_for_each_label;
    private int list_item;

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

    public DrawingView(Context context)
    {
        super(context);
        this.context=context;
        initialize();
    }

    public void setDrawingScene(DrawingScene drawingScene)
    {
        this.drawingScene = drawingScene;
    }

    private void initialize()
    {
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
        Typeface typeface=Typeface.createFromAsset(context.getAssets(),"fonts/roboto_mediumItalic.ttf");
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(TEXT_SIZE);
        bitmap_list= new ArrayList<>();
        tags_for_each_label=new ArrayList<>();
        list_item=-1;
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, paintForBitmap);
        if (gestureNormalScroll | longPress)
        {
            canvas.drawPath(path, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap=Bitmap.createBitmap(w,h==0?2:h, Bitmap.Config.ARGB_8888);
        backupcanvas=new Canvas(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
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
            if (gestureNormalScroll||longPress)
            {
                Log.d(logging,"Only arrow saved");
                bitmap_list.add(Bitmap.createBitmap(bitmap));
                tags_for_each_label.add("");
                ++list_item;
            }
            gestureNormalScroll=longPress=false;
            backupcanvas.drawPath(path,paint);
            Log.d(logging,list_item+"");
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
        list_item=-1;
        clearBitmapList();
        invalidate();
    }

    public void sendMessage(String string)
    {
        float textSize=textPaint.getTextSize();
        //Handle the message here
        //Todo:Correct for landscape position in a different function and arrange all these into functions also
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
            height/=2;
            finY=finY+height;
            if (inverse)finX=finX-minWidth;
        }
        else
        {
            finX=finX-minWidth/2;
            if (inverse)finY=finY+height;
        }
        backupcanvas.translate(finX, finY);
        staticLayout.draw(backupcanvas);
        backupcanvas.translate(-finX, -finY);
        Log.d(logging,list_item+"=at save time");
        bitmap_list.add(list_item+1,Bitmap.createBitmap(bitmap));
        tags_for_each_label.add(list_item,string);
        clearBitmapList();
        textPaint.setTextSize(textSize);
        invalidate();
    }
    private void clearBitmapList()
    {
        for (int i=bitmap_list.size()-1;i>list_item;i--)
        {
            Log.d(logging,bitmap_list.size()+"=size of bitmap");
            Log.d(logging,i+"=i");
            Log.d(logging,tags_for_each_label.size()+"=tag size");
            bitmap_list.remove(i);
            tags_for_each_label.remove(i);
        }
    }

    private int minWidthAdjustment(String string, float degreeAbs)
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
        float currentWord;
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

    public void undoLast()
    {
        if (list_item>0)
        {
            backupcanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            backupcanvas.drawBitmap(bitmap_list.get(list_item),0,0,paintForBitmap);
            --list_item;
            clearBitmapList();
            invalidate();
        }
        else
        {
            reset();
            list_item=-1;
            clearBitmapList();
        }
    }

    public List<String> getTags_for_each_label()
    {
        return tags_for_each_label;
    }
}