package com.totalboron.jay.labeled;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class AsyncTaskSaveImage extends AsyncTask<Bitmap, Void, Void>
{
    private String fileName;
    private Context context;
    private String logging = getClass().getSimpleName();
    private List<String> labels;

    public AsyncTaskSaveImage(String fileName, Context context, List<String> labels)
    {
        this.fileName = fileName;
        this.context = context;
        this.labels = labels;
        removeAllBlanks();
    }

    private void removeAllBlanks()
    {
        for (int i=labels.size()-1;i>=0;i--)
        {
            if (labels.get(i).equals(""))
                labels.remove(i);
        }
    }

    @Override
    protected Void doInBackground(Bitmap... params)
    {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "LabelED");
        if (!file.exists())
        {
            file.mkdirs();
        }
        long time = System.currentTimeMillis();
        String publicPath = file.getAbsolutePath() + "/" + "LabelED" + time + fileName;
        File internal_save=new File(context.getFilesDir(),context.getResources().getString(R.string.directory_images));
        if (!internal_save.exists())
            internal_save.mkdirs();
        Log.d(logging,internal_save.getAbsolutePath());
        File image = new File(internal_save, "LabelED" + time + fileName);
        getName(time);
        File internal_save_labels=new File(context.getFilesDir(),context.getResources().getString(R.string.directory_labels));
        if (!internal_save_labels.exists())
            internal_save_labels.mkdirs();
        internal_save_labels = new File(internal_save_labels, fileName);
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(image);
            params[0].compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
            fileOutputStream.close();
            writeLabels(internal_save_labels);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(publicPath);
            params[0].compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(publicPath)));
        context.sendBroadcast(intent);
        return null;
    }

    private void writeLabels(File file) throws IOException
    {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        for (int i = 0; i < labels.size(); i++)
        {
            bufferedWriter.write(labels.get(i));
            databaseAdapter.insertData(labels.get(i), file.getName());
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private void getName(long time)
    {
        String string = "";
        int i;
        for (i = 0; i < fileName.length(); i++)
            if (fileName.charAt(i) != '.')
                string += fileName.charAt(i);
            else break;
        fileName = "LabelED" + time + string;
    }
}
