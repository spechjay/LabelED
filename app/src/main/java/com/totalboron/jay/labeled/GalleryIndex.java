package com.totalboron.jay.labeled;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Jay on 07/04/16.
 */
public class GalleryIndex extends AppCompatActivity
{
    //Todo: Traverse all the directories for now atleast in Camera itself
    //Todo: Then traverse in all the directories there is
    //Todo:Decrease the total Cache Memory
    RecyclerView recyclerView;
    int cnum;
    final private int REQUEST_CODE_FOR_IMAGE=75;
    private String logging=getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(logging,"in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.behaviour);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Choose Image");
        getWindow().getDecorView().setBackground(null);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        AsyncTaskLoadingForIndex asyncTaskLoadingForIndex=new AsyncTaskLoadingForIndex(getApplicationContext(),new WeakReference<GalleryIndex>(this));
        asyncTaskLoadingForIndex.execute();
        setResult(RESULT_CANCELED);
    }
    private File getGallery()
    {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(),"Camera");
    }

    public void settingResult(String path, int code)
    {
        if (code==1) setResult(RESULT_OK,new Intent().putExtra("PATH",path));
        else setResult(RESULT_CANCELED);
        finish();
    }


    public void indexReady(List<String> bucket_list_names, List<String> data_each_bucket)
    {
        int cnum=getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT?2:4;
        ImageAdapterIndex imageAdapterIndex=new ImageAdapterIndex(bucket_list_names,data_each_bucket,getWindow().getDecorView().getWidth(),cnum,this);
        recyclerView.setAdapter(imageAdapterIndex);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getApplicationContext(),cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void openImages(String bucket_name)
    {
        Intent intent=new Intent(this,ImageListActivity.class);
        Log.d(logging,bucket_name+"=bucket");
        intent.putExtra("BUCKET",bucket_name);
        startActivityForResult(intent,REQUEST_CODE_FOR_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==REQUEST_CODE_FOR_IMAGE&&resultCode==RESULT_OK)
        {
            setResult(RESULT_OK,data);
            finish();
        }

    }
}
