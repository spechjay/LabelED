package com.totalboron.jay.labeled;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Jay on 04/04/16.
 */
public class FocusChangeListener implements View.OnFocusChangeListener
{
    private Context context;

    public FocusChangeListener(Context context)
    {
        this.context = context;
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v.getId()== R.id.edit_text&& !hasFocus)
        {
            InputMethodManager imn=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imn.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    }
}
