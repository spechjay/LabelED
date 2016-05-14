package com.totalboron.jay.labeled;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Jay on 15/04/16.
 */
public class ImageListActivity extends AppCompatActivity
{
    RecyclerView recyclerView;
    int cnum;
    private String logging = getClass().getSimpleName();
    private String bucket_name;
    private final int REQUEST_READ_STORAGE = 55;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.behaviour);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_index);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setBackground(null);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        Intent intent = getIntent();
        bucket_name = intent.getStringExtra("BUCKET");
        getSupportActionBar().setTitle(bucket_name);
        cnum = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        setResult(RESULT_CANCELED);
        checkForPermissions();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void loadEverything()
    {
        AsyncTaskForImage asyncTaskForImage = new AsyncTaskForImage(bucket_name, getApplicationContext(), this);
        asyncTaskForImage.execute();
    }

    private void checkForPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                loadEverything();
            } else
            {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Toast.makeText(getApplicationContext(), "Reading Access required to select an Image", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            }
        else loadEverything();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (requestCode == REQUEST_READ_STORAGE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) loadEverything();
                else
                {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
    }


    public void startImageFinal(Cursor cursor)
    {
        ImageList imageList = new ImageList(cursor, getWindow().getDecorView().getWidth() / cnum, this);
        recyclerView.setAdapter(imageList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void sendBackResult(String data)
    {
        if (data != null)
        {
            Intent intent = new Intent();
            intent.putExtra("FILE_PATH", data);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
