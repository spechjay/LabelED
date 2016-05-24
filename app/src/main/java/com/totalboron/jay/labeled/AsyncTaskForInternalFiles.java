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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jay on 19/04/16.
 */
public class AsyncTaskForInternalFiles extends AsyncTask<Void, Void, Void>
{
    private Context context;
    private WeakReference<FragmentList> weakReference;
    private File[] labels;
    private String logging = getClass().getSimpleName();
    private List<DisplayObject> displayObjects;

    public AsyncTaskForInternalFiles(Context context, FragmentList fragmentList)
    {
        this.context = context;
        weakReference = new WeakReference<FragmentList>(fragmentList);
        displayObjects=new ArrayList<>();
        Log.d(logging, "Harami");
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        File internal_save = new File(context.getFilesDir(), context.getResources().getString(R.string.directory_images));
        Log.d(logging, internal_save.getAbsolutePath());
        File[] files = internal_save.listFiles();
        if (files == null)
        {
            Log.d(logging, "Null returniung");
            return null;
        }
        labels = (new File(context.getFilesDir(), context.getResources().getString(R.string.directory_labels))).listFiles();
        sortItOut(files, labels);
        return null;
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
            displayObjects.add(new DisplayObject(pairForSorting[i].fi,labelsForSorting[i].fi));
        }
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        if (displayObjects != null && weakReference.get() != null)
            weakReference.get().fillUpRecyclerView(displayObjects);
    }
}
