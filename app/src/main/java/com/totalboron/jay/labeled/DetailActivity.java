package com.totalboron.jay.labeled;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TableLayout;

public class DetailActivity extends AppCompatActivity
{
    private DisplayObject displayObject;
    private TableLayout tableLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tableLayout=(TableLayout)findViewById(R.id.table_layout_detail);
        displayObject=(DisplayObject)getIntent().getSerializableExtra("DISPLAY_OBJECT");
        DetailedLabelShow detailedLabelShow=new DetailedLabelShow(getApplicationContext(),tableLayout);
        detailedLabelShow.execute(displayObject.getLabelFile());
    }

}
