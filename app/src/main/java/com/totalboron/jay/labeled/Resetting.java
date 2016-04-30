package com.totalboron.jay.labeled;

import android.content.Context;

/**
 * Created by Jay on 21/04/16.
 */
public  class Resetting
{
    private static Resetting resetting=null;
    private AdapterForCardView adapterForCardView;
    private Context context;
    private Resetting(Context context)
    {
        this.context=context;
    }
    public static Resetting getInstance(Context context)
    {
        if (resetting==null)
            resetting=new Resetting(context);
        return resetting;
    }

    public void setAdapterForCardView(AdapterForCardView adapterForCardView)
    {
        this.adapterForCardView = adapterForCardView;
    }
    public void startRefresh()
    {
        AsyncTaskForInternalFiles asyncTaskForInternalFiles=new AsyncTaskForInternalFiles(context,adapterForCardView,true);
        asyncTaskForInternalFiles.execute();
    }
}
