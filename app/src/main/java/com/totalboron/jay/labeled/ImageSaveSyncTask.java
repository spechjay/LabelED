package com.totalboron.jay.labeled;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;

/**
 * Created by Jay on 11/04/16.
 */
public class ImageSaveSyncTask extends AsyncTask<Bitmap,Void,Void>
{
    final  private  String filePath;
    final private Context context;
    public ImageSaveSyncTask(String filePath, Context context)
    {
        this.filePath=filePath;
        this.context=context;
    }

    @Override
    protected Void doInBackground(Bitmap... params)
    {
        Bitmap bitmap= BitmapFactory.decodeFile(filePath);
        Bitmap overlay=params[0];
        Canvas canvas=new Canvas(bitmap);
        canvas.drawBitmap(overlay,0,0,new Paint(Paint.DITHER_FLAG));

        return null;
    }


}
