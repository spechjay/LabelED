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
import java.util.Arrays;

/**
 * Created by Jay on 19/04/16.
 */
public class AsyncTaskForInternalFiles extends AsyncTask<Void, Void, File[]>
{
    private Context context;
    private AdapterForCardView adapterForCardView;
    private File[] labels;
    private boolean ifSaved;
    private String logging = getClass().getSimpleName();

    public AsyncTaskForInternalFiles(Context context, AdapterForCardView adapterForCardView, boolean ifSaved)
    {
        this.context = context;
        this.adapterForCardView = adapterForCardView;
        this.ifSaved = ifSaved;
    }

    @Override
    protected File[] doInBackground(Void... params)
    {
        File internal_save = context.getDir(context.getResources().getString(R.string.directory_images), Context.MODE_PRIVATE);
        File[] files = internal_save.listFiles();
        labels = context.getDir(context.getResources().getString(R.string.directory_labels), Context.MODE_PRIVATE).listFiles();
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
            Log.d(logging, files[i].getAbsolutePath() + "=fileNames");
            Log.d(logging, labels[i].getAbsolutePath() + "=labelNames");
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
        if (files != null)
        {
            adapterForCardView.setImage_files(files);
            adapterForCardView.setStrings_of_files(labels);
            if (ifSaved)
            {
                adapterForCardView.notifyItemInserted(0);
                File file= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"LabelED");
                Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
                context.sendBroadcast(intent);
            }
            else
                adapterForCardView.notifyDataSetChanged();
        }
    }
}
