package com.totalboron.jay.labeled;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AsyncTaskSaveImage extends AsyncTask<Bitmap,Void,Void>
{
    private String fileName;
    private Context context;
    private String logging=getClass().getSimpleName();
    public AsyncTaskSaveImage(String fileName, Context context)
    {
        this.fileName = fileName;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Bitmap... params)
    {
        File file= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"LabelED");
        if (!file.exists())
        {
            file.mkdirs();
        }
        Log.d(logging,fileName);
        String publicPath=file.getAbsolutePath()+ "/"+fileName;
        Log.d(logging,publicPath);

        File internal_save=context.getDir("DONE_IMAGES",Context.MODE_PRIVATE);
        File image=new File(internal_save,fileName);
        Log.d(logging,image.getAbsolutePath());
        try
        {
            FileOutputStream fileOutputStream=new FileOutputStream(image);
            params[0].compress(Bitmap.CompressFormat.JPEG,85,fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


        try
        {
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(publicPath);
            params[0].compress(Bitmap.CompressFormat.JPEG,85,fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri=Uri.fromFile(new File(publicPath));
        intent.setData(uri);
        context.sendBroadcast(intent);






        return null;
    }
}
