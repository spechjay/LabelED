package com.totalboron.jay.labeled;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay on 14/05/16.
 */
public class DetailedLabelShow extends AsyncTask<File, Void, List<String>>
{
    private Context context;
    private TableLayout tableLayout;
    private String logging=getClass().getSimpleName();
    private WeakReference<MainActivity> weakReference;
    public DetailedLabelShow(Context context, TableLayout tableLayout, MainActivity mainActivity)
    {
        this.context=context;
        this.tableLayout=tableLayout;
        weakReference=new WeakReference<MainActivity>(mainActivity);
    }

    @Override
    protected List<String> doInBackground(File... params)
    {
        List<String> str = new ArrayList<>();
        try
        {
            String inputString;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(params[0])));
            while ((inputString = bufferedReader.readLine()) != null)
            {
                str.add(inputString);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    protected void onPostExecute(List<String> strings)
    {
        strings.remove("");
        float textSize;
        textSize = 13;
        if (!isCancelled())
        {
            TableLayout.LayoutParams tableRowParams=new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
            TableRow.LayoutParams textViewParams=new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f);
            int color = context.getResources().getColor(R.color.black);
            TableRow tableRow=null;
            for (int i = 0; i < strings.size(); i++)
            {
                if (i%2==0)
                {
                    tableRow=new TableRow(context);
                    tableRow.setLayoutParams(tableRowParams);
                    tableLayout.addView(tableRow);
                }
                TextView textView=new TextView(context);
                textView.setTextSize(textSize);
                textView.setText(strings.get(i));
                textView.setLayoutParams(textViewParams);
                textView.setTextColor(color);
                textView.setGravity(Gravity.CENTER);
                if (tableRow!=null)
                tableRow.addView(textView);
                else Log.d(logging,"Null:Panic");
            }
            weakReference.get().startAll();
        }
    }
}
