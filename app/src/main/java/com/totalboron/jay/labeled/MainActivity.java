package com.totalboron.jay.labeled;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_PICTURE=1;
    private RecyclerView recyclerView;
    private String logging=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView=(RecyclerView)findViewById(R.id.main_activity_recycler_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(getApplicationContext(),GalleryIndex.class);
                startActivityForResult(intent, REQUEST_PICTURE);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==REQUEST_PICTURE)
            if (resultCode==RESULT_OK)
            {
                Intent intent=new Intent(this,DrawingScene.class);
                intent.setAction("Main_Activity");
                intent.putExtra("PATH",data.getStringExtra("FILE_PATH"));
                startActivity(intent);
            }
    }
}
