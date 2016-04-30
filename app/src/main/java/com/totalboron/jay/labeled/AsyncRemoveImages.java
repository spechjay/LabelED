package com.totalboron.jay.labeled;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

/**
 * Created by Jay on 19/04/16.
 */
public class AsyncRemoveImages extends AsyncTask<Void,Void,Void>
{
    private Context context;
    private String logging=getClass().getSimpleName();
    private AdapterForCardView adapterForCardView;
    public AsyncRemoveImages(Context context,AdapterForCardView adapterForCardView)
    {
        this.context = context;
        this.adapterForCardView=adapterForCardView;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        File file=context.getDir(context.getResources().getString(R.string.directory_images),Context.MODE_PRIVATE);
        File file_labels=context.getDir(context.getResources().getString(R.string.directory_labels),Context.MODE_PRIVATE);
        File[] imageFiles=file.listFiles();
        File[] labelFiles=file_labels.listFiles();
        for (int i=0;i<imageFiles.length;i++)
        {
            Log.d(logging,imageFiles[i].delete()+"=for image");
        }
        for (int i=0;i<labelFiles.length;i++)
        {
            Log.d(logging,labelFiles[i].delete()+"=for labels");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        adapterForCardView.notifyDataSetChanged();
    }
}
