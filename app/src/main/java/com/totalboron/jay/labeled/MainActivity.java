package com.totalboron.jay.labeled;

import android.animation.Animator;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_PICTURE = 1;
    private final int SPEECH_CODE = 55;
    private RecyclerView recyclerView;
    private AdapterForCardView adapterForCardView;
    private String logging = getClass().getSimpleName();
    private Toolbar toolbar;
    private EditText search_edit_text;
    private DatabaseAdapter databaseAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            search_edit_text=(EditText)toolbar.findViewById(R.id.edit_text_search);
            search_edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId== EditorInfo.IME_ACTION_SEARCH)
                        messageReceiver();
                    return false;
                }
            });
        }
        databaseAdapter=new DatabaseAdapter(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), GalleryIndex.class);
                startActivityForResult(intent, REQUEST_PICTURE);
            }
        });
        GridLayoutManager gridLayoutManager;
        int cnum = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapterForCardView = new AdapterForCardView(getApplicationContext(), null, null);
        recyclerView.setAdapter(adapterForCardView);
        AsyncTaskForInternalFiles asyncTaskForInternalFiles = new AsyncTaskForInternalFiles(getApplicationContext(), adapterForCardView, false);
        asyncTaskForInternalFiles.execute();
        Resetting resetting = Resetting.getInstance(getApplicationContext());
        resetting.setAdapterForCardView(adapterForCardView);
    }

    private void messageReceiver()
    {
        String text=search_edit_text.getText().toString();
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        //Todo:Search In a different Thread For Files
        databaseAdapter.getSearch(text);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_PICTURE)
        {
            if (resultCode == RESULT_OK)
            {
                Intent intent = new Intent(this, DrawingScene.class);
                intent.setAction("Main_Activity");
                intent.putExtra("PATH", data.getStringExtra("FILE_PATH"));
                startActivity(intent);
            }
        } else if (requestCode == SPEECH_CODE)
        {
            if (resultCode == RESULT_OK && data != null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                search_edit_text.setText(result.get(0));
                //Todo:Start the search here also
            }
        }


    }

    public void removeAll(View view)
    {
        AsyncRemoveImages asyncRemoveImages = new AsyncRemoveImages(getApplicationContext(), adapterForCardView);
        asyncRemoveImages.execute();
    }

    public void clickedSearch(View view)
    {
        RelativeLayout searchTool = (RelativeLayout) toolbar.findViewById(R.id.searchTool);
        searchTool.setVisibility(View.VISIBLE);
        int cx = searchTool.getMeasuredWidth();
        int cy = searchTool.getMeasuredHeight();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            Animator animator = ViewAnimationUtils.createCircularReveal(searchTool, cx, cy / 2, 0, cx);
            animator.addListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {

                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    toolbar.findViewById(R.id.initial).setVisibility(View.INVISIBLE);
                    toolbar.findViewById(R.id.edit_text_search).requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(toolbar.findViewById(R.id.edit_text_search), InputMethodManager.SHOW_IMPLICIT);
                }

                @Override
                public void onAnimationCancel(Animator animation)
                {

                }

                @Override
                public void onAnimationRepeat(Animator animation)
                {

                }
            });
            animator.start();
        } else searchTool.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(logging, "Here");
        if (toolbar.findViewById(R.id.searchTool).getVisibility() == View.VISIBLE)
        {
            search_edit_text.setText("");
            toolbar.findViewById(R.id.initial).setVisibility(View.VISIBLE);
            toolbar.findViewById(R.id.searchTool).setVisibility(View.INVISIBLE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void speechToText(View view)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Label");
        try
        {
            startActivityForResult(intent, SPEECH_CODE);
        } catch (ActivityNotFoundException e)
        {
            Toast.makeText(getApplicationContext(), "Speech Recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }
}
