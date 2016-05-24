package com.totalboron.jay.labeled;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jay on 16/05/16.
 */
public class AsyncTaskUriShare extends AsyncTask<List<File>,Void,ArrayList<Uri>>
{
    private Context context;
    private WeakReference<MainActivity> reference;
    public AsyncTaskUriShare(Context context,MainActivity mainActivity)
    {
        this.context=context;
        reference=new WeakReference<MainActivity>(mainActivity);
    }

    @Override
    protected ArrayList<Uri> doInBackground(List<File>... params)
    {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "LabelED");
        if (!file.exists())
            return null;
        List<File> original_list=params[0];
        File[] lists=file.listFiles();
        ArrayList<Uri> modified_list=new ArrayList<>();
        for (int i = 0; i < original_list.size(); i++)
        {
            for (int j = 0; j < lists.length; j++)
            {
                if (original_list.get(i).getName().equals(lists[j].getName()))
                {
                    modified_list.add(Uri.fromFile(lists[j]));
                    break;
                }
            }
        }
        return modified_list;
    }
}
