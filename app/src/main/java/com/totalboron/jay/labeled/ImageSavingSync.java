package com.totalboron.jay.labeled;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Jay on 12/04/16.
 */
public class ImageSavingSync extends AsyncTask<Bitmap,Void,Void>
{
    private Context context;
    public ImageSavingSync(Context context)
    {
        this.context=context;
    }

    @Override
    protected Void doInBackground(Bitmap... params)
    {

        return null;
    }
}
