package com.totalboron.jay.labeled;

import android.animation.Animator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_PICTURE = 1;
    private final int SPEECH_CODE = 55;
    private RecyclerView recyclerView;
    private AdapterForCardView adapterForCardView;
    private String logging = getClass().getSimpleName();
    private Toolbar toolbar;
    private AutoCompleteTextView search_edit_text;
    private DatabaseAdapter databaseAdapter;
    private File[] images_total = null;
    private File[] labels_total = null;
    private ImageView overview_image;
    private TableLayout overview_table;
    private RelativeLayout relativeLayout;
    private MyRelativeLayout myRelativeLayout;
    private RelativeLayout selectionToolbar;

    private int[] rect;
    private int measuredHeight;
    private int measuredWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        relativeLayout = (RelativeLayout) findViewById(R.id.overview_whole);
        myRelativeLayout = (MyRelativeLayout) findViewById(R.id.main_view_relative_layout);
        myRelativeLayout.setMainActivity(this);
        myRelativeLayout.setRelativeLayout(relativeLayout);
        selectionToolbar=(RelativeLayout)findViewById(R.id.selection_toolbar);
        if (toolbar != null)
        {
            search_edit_text = (AutoCompleteTextView) toolbar.findViewById(R.id.edit_text_search);
            search_edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH)
                    {
                        databaseAdapter.insertWordHistory(search_edit_text.getText().toString());
                        messageReceiver();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }
                    return false;
                }
            });
        }
        databaseAdapter = new DatabaseAdapter(getApplicationContext());
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
        adapterForCardView = new AdapterForCardView(getApplicationContext(), this);
        recyclerView.setAdapter(adapterForCardView);
        search_edit_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

                messageReceiver();
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        overview_image = (ImageView) findViewById(R.id.overview_image);
        overview_table = (TableLayout) findViewById(R.id.overview_table);
        adapterForCardView.setNumberReference((TextView)selectionToolbar.findViewById(R.id.text_selection));
    }

    private void messageReceiver()
    {
        String text = search_edit_text.getText().toString();
        //Todo:Do this in a different thread here
        Cursor cursor = databaseAdapter.getSearch(text);
        if (cursor != null && cursor.getCount() > 0 && labels_total != null)
        {
            String label_name;
            List<File> result_image = new ArrayList<>();
            List<File> result_label = new ArrayList<>();
            while (cursor.moveToNext())
            {
                label_name = cursor.getString(0);
                int index = indexOf(labels_total, label_name);
                if (index >= 0 && !checkForDuplicate(label_name, result_label))
                {
                    result_image.add(images_total[index]);
                    result_label.add(labels_total[index]);
                }
            }
            adapterForCardView.setFiles(result_label, result_image);
        } else
        {
            adapterForCardView.setNull();
        }
        recyclerView.scrollToPosition(0);
    }

    private boolean checkForDuplicate(String label_name, List<File> result_label)
    {
        if (result_label != null)
        {
            for (int i = 0; i < result_label.size(); i++)
            {
                if (result_label.get(i).getName().equals(label_name))
                    return true;
            }
        }
        return false;
    }

    private int indexOf(File[] newList, String label)
    {
        for (int i = 0; i < newList.length; i++)
        {
            if (newList[i].getName().equals(label))
                return i;
        }
        return -99;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (toolbar.findViewById(R.id.searchTool).getVisibility() == View.INVISIBLE)
        {
            AsyncTaskForInternalFiles asyncTaskForInternalFiles = new AsyncTaskForInternalFiles(getApplicationContext(), this);
            asyncTaskForInternalFiles.execute();
        }
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
                messageReceiver();
            }
        }
    }


    public void fillUpRecyclerView(File[] images, File[] label)
    {
        images_total = images;
        labels_total = label;
        adapterForCardView.setFiles(new LinkedList<File>(Arrays.asList(label)), new LinkedList<File>(Arrays.asList(images)));
    }


    public void removeAll(View view)
    {
        AsyncRemoveImages asyncRemoveImages = new AsyncRemoveImages(getApplicationContext(), adapterForCardView);
        asyncRemoveImages.execute();
    }

    public void clickedSearch(View view)
    {
        if (relativeLayout.getVisibility() == View.VISIBLE)
            deflate();
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
        if (adapterForCardView.isLongClicked())
        {
            adapterForCardView.removeAllSelection();
            hideSelectionBar();
            return true;
        } else if (relativeLayout.getVisibility() == View.VISIBLE)
        {
            deflate();
            return true;
        } else if (toolbar.findViewById(R.id.searchTool).getVisibility() == View.VISIBLE)
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

    public void Register(View view)
    {
        adapterForCardView.addToDatabase();
    }

    public void setUpOverview(File images, File label_files, int[] rect, int measuredHeight, int measuredWidth)
    {
        this.measuredHeight = measuredHeight;
        this.measuredWidth = measuredWidth;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(images.getAbsolutePath(), options);
        int width = options.outWidth;
        int height = options.outHeight;
        overview_image.setImageBitmap(null);
        overview_table.removeAllViews();
        float aspectRatio = ((float) height) / width;
        Log.d(logging, aspectRatio + "");
        if (aspectRatio >= 1)
        {
            int total_width = getWindow().getDecorView().getWidth();
            int decided_width = (int) (total_width * 0.65);
            ViewGroup.LayoutParams layoutParams = overview_image.getLayoutParams();
            layoutParams.width = decided_width;
            layoutParams.height = (int) (decided_width * aspectRatio);
            overview_image.requestLayout();
            overview_table.getLayoutParams().width = decided_width;
            overview_table.requestLayout();
        } else
        {
            int total_width = getWindow().getDecorView().getWidth();
            int decided_width = (int) (total_width * 0.85);
            ViewGroup.LayoutParams layoutParams = overview_image.getLayoutParams();
            layoutParams.width = decided_width;
            layoutParams.height = (int) (decided_width * aspectRatio);
            overview_image.requestLayout();
            overview_table.getLayoutParams().width = decided_width;
            overview_table.requestLayout();

        }
        Glide.with(getApplicationContext()).load(images).into(overview_image);
        this.rect = rect;
        DetailedLabelShow detailedLabelShow = new DetailedLabelShow(getApplicationContext(), overview_table, this);
        detailedLabelShow.execute(label_files);
    }

    public void inflate()
    {
        relativeLayout.setVisibility(View.VISIBLE);
    }

    public void deflate()
    {
        relativeLayout.setVisibility(View.INVISIBLE);
    }

    public void clickedOverviewImage(View view)
    {
        Log.d(logging, relativeLayout.getY() + "");
    }

    public void showSelectionBar()
    {
        selectionToolbar.setVisibility(View.VISIBLE);
    }

    public void hideSelectionBar()
    {
        selectionToolbar.setVisibility(View.INVISIBLE);
    }
}
