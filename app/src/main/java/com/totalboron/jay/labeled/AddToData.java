package com.totalboron.jay.labeled;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay on 10/05/16.
 */
public class AddToData extends AsyncTask<File,Void,Void>
{
    private String file_name;
    private DatabaseAdapter databaseAdapter;
    private Context context;
    public AddToData(String file_name, DatabaseAdapter databaseAdapter, Context context)
    {
        this.file_name = file_name;
        this.databaseAdapter=databaseAdapter;
        this.context=context;
    }
    @Override
    protected Void doInBackground(File... params)
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
        str.remove("");
        for (int i = 0; i < str.size(); i++)
        {
            databaseAdapter.insertData(str.get(i),file_name);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        super.onPostExecute(aVoid);
    }
}
