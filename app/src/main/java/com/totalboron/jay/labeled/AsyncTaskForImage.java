package com.totalboron.jay.labeled;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Jay on 14/04/16.
 */
public class AsyncTaskForImage extends AsyncTask<Void,Void,Cursor>
{
    private String[] bucket_name=new String[1];
    protected Context context;
    private ImageListActivity imageListActivity;
    private String logging=getClass().getSimpleName();
    public AsyncTaskForImage(String bucket_name, Context context, ImageListActivity imageListActivity)
    {
        this.bucket_name[0] = bucket_name;
        this.context = context;
        this.imageListActivity=imageListActivity;
    }
    @Override
    protected Cursor doInBackground(Void... params)
    {
        String[] projections={MediaStore.Images.Media.DATA};
        Cursor images=context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projections, MediaStore.Images.Media.BUCKET_DISPLAY_NAME+" = ?",
                bucket_name, MediaStore.Images.Media.DATE_ADDED+" DESC");
        if (images!=null)
        return images;
        else return null;
    }

    @Override
    protected void onPostExecute(Cursor cursor)
    {
        super.onPostExecute(cursor);
        imageListActivity.startImageFinal(cursor);


    }
}
