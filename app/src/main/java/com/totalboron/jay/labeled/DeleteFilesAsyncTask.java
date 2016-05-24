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
public class DeleteFilesAsyncTask extends AsyncTask<List<DisplayObject>,Void,Void>
{
    //ToDo:Delete files and list from Adapter
    private WeakReference<FragmentList> weakReference;
    private String logging=getClass().getSimpleName();
    private DatabaseAdapter databaseAdapter;
    private Context context;
    public DeleteFilesAsyncTask(FragmentList fragmentList,DatabaseAdapter databaseAdapter,Context context)
    {
        weakReference= new WeakReference<>(fragmentList);
        this.context=context;
        this.databaseAdapter=databaseAdapter;
    }

    @Override
    protected final Void doInBackground(List<DisplayObject>... params)
    {
        List<DisplayObject> newList=params[0];
        for (int i = 0; i < newList.size(); i++)
        {
            databaseAdapter.removeFiles(newList.get(i).getLabelFile().getName());
            Log.d(logging,newList.get(i).getImageFile().delete()+"");
            Log.d(logging,newList.get(i).getLabelFile().delete()+"");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        weakReference.get().updateFiles();
    }
}
