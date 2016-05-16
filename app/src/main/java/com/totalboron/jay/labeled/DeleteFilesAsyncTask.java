package com.totalboron.jay.labeled;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Jay on 16/05/16.
 */
public class DeleteFilesAsyncTask extends AsyncTask<List<File>,Void,Void>
{
    //ToDo:Delete files and list from Adapter
    private List<File> images;
    private WeakReference<MainActivity> weakReference;
    private String logging=getClass().getSimpleName();
    private DatabaseAdapter databaseAdapter;
    private Context context;
    public DeleteFilesAsyncTask(List<File> images,MainActivity mainActivity,DatabaseAdapter databaseAdapter,Context context)
    {
        this.images = images;
        weakReference=new WeakReference<MainActivity>(mainActivity);
        this.context=context;
        this.databaseAdapter=databaseAdapter;
    }

    @Override
    protected final Void doInBackground(List<File>... params)
    {
        List<File> labels=params[0];
        for (int i = 0; i < labels.size(); i++)
        {
            databaseAdapter.removeFiles(labels.get(i).getName());
            Log.d(logging,labels.get(i).delete()+"");
            Log.d(logging,images.get(i).delete()+"");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        weakReference.get().updateFiles();
    }
}
