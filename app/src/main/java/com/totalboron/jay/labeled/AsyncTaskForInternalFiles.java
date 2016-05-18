package com.totalboron.jay.labeled;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Created by Jay on 19/04/16.
 */
public class AsyncTaskForInternalFiles extends AsyncTask<Void, Void, File[]>
{
    private Context context;
    private WeakReference<MainActivity> weakReference;
    private File[] labels;
    private String logging = getClass().getSimpleName();

    public AsyncTaskForInternalFiles(Context context, MainActivity mainActivity)
    {
        this.context = context;
        weakReference = new WeakReference<MainActivity>(mainActivity);
        Log.d(logging,"Harami");
    }

    @Override
    protected File[] doInBackground(Void... params)
    {
        File internal_save = new File(context.getFilesDir(), context.getResources().getString(R.string.directory_images));
        Log.d(logging,internal_save.getAbsolutePath());
        File[] files = internal_save.listFiles();
        if (files == null)
        {
            Log.d(logging,"Null returniung");
            return null;
        }
        labels = (new File(context.getFilesDir(), context.getResources().getString(R.string.directory_labels))).listFiles();
        labels = (new File(context.getFilesDir(), context.getResources().getString(R.string.directory_labels))).listFiles();
        sortItOut(files, labels);
        return files;
    }

    private void sortItOut(File[] files, File[] labels)
    {
        PairForSorting[] pairForSorting = new PairForSorting[files.length];
        PairForSorting[] labelsForSorting = new PairForSorting[files.length];
        for (int i = 0; i < files.length; i++)
        {
            pairForSorting[i] = new PairForSorting(files[i]);
            labelsForSorting[i] = new PairForSorting(labels[i]);
        }
        Arrays.sort(pairForSorting);
        Arrays.sort(labelsForSorting);
        for (int i = 0; i < files.length; i++)
        {
            files[i] = pairForSorting[i].fi;
            labels[i] = labelsForSorting[i].fi;
        }
    }

    @Override
    protected void onPostExecute(File[] files)
    {
        if ((!isCancelled()) && files != null)
        {
            weakReference.get().fillUpRecyclerView(files, labels);
        }
    }
}
