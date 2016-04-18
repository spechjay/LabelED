package com.totalboron.jay.labeled;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by Jay on 16/04/16.
 */
public class ActivityFake extends AppCompatActivity
{
    private ImageView imageView;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        imageView=new ImageView(getApplicationContext());
        setContentView(imageView);
    }
}
