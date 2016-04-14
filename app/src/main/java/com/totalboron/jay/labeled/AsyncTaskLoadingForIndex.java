package com.totalboron.jay.labeled;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AsyncTaskLoadingForIndex extends AsyncTask<Void,Void,List<String>>
{
    private Context context;
    private String logging=getClass().getSimpleName();
    private WeakReference<GalleryIndex> weakReference;
    List<String> data_each_bucket= new ArrayList<>();
    public AsyncTaskLoadingForIndex(Context context,WeakReference<GalleryIndex> weakReference)
    {
        this.context = context;
        this.weakReference = weakReference;
    }
    @Override
    protected List<String> doInBackground(Void... params)
    {
        String[] projections={MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor image_total=context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projections,null,null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        List<String> bucket_names_list= new ArrayList<>();
        if (image_total!=null)
        {
            int data_index=image_total.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            Log.d(logging,data_index+"=index");
            int bucket_index=image_total.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            Log.d(logging,bucket_index+"=index");
            while (image_total.moveToNext())
            {
                String[] bucket_name={image_total.getString(bucket_index)};
                Cursor no_of_photos_in_bucket=context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projections, MediaStore.Images.Media.BUCKET_DISPLAY_NAME+" = ?",
                        bucket_name, MediaStore.Images.Media.DATE_ADDED+" DESC");
                if (no_of_photos_in_bucket!=null)
                {
                    no_of_photos_in_bucket.moveToFirst();
                    bucket_names_list.add(bucket_name[0]);
                    data_each_bucket.add(no_of_photos_in_bucket.getString(data_index));
                    Log.d(logging,no_of_photos_in_bucket.getString(data_index));
                    image_total.moveToPosition(image_total.getPosition()+no_of_photos_in_bucket.getCount()-1);
                    no_of_photos_in_bucket.close();
                }
            }
            image_total.close();
        }
        return bucket_names_list;
    }

    @Override
    protected void onPostExecute(List<String> bucket_list_names)
    {
        if (bucket_list_names!=null)
        {
            weakReference.get().indexReady(bucket_list_names,data_each_bucket);
        }

    }
}
