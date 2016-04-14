package com.totalboron.jay.labeled;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Jay on 15/04/16.
 */
public class ImageListActivity extends AppCompatActivity
{
    RecyclerView recyclerView;
    int cnum;
    private String logging=getClass().getSimpleName();
    private String bucket_name;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.behaviour);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().getDecorView().setBackground(null);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        Intent intent=getIntent();
        bucket_name=intent.getStringExtra("BUCKET");
        getSupportActionBar().setTitle(bucket_name);

        cnum=getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?3:5;
        setResult(RESULT_CANCELED);
        AsyncTaskForImage asyncTaskForImage=new AsyncTaskForImage(bucket_name,getApplicationContext(),this);
        asyncTaskForImage.execute();
    }

    public void startImageFinal(Cursor cursor)
    {
        ImageList imageList=new ImageList(cursor,getWindow().getDecorView().getWidth()/cnum,this);
        recyclerView.setAdapter(imageList);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getApplicationContext(),cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void sendBackResult(String data)
    {
        if (data!=null)
        {
            Intent intent=new Intent();
            intent.putExtra("FILE_PATH",data);
            setResult(RESULT_OK,intent);
        }
        finish();
    }
}
