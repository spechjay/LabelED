package com.totalboron.jay.labeled;

import android.animation.Animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.reflect.Array;
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
    private RelativeLayout myRelativeLayout;
    private RelativeLayout selectionToolbar;
    private FrameLayout blurring;
    private MyRelativeLayout rootView;
    private ScrollView scrollView;
    private int[] rect;
    private int measuredHeight;
    private int measuredWidth;
    private boolean animationGoing = false;
    private int REQUEST_CODE_SHARE = 94;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(logging, "What the fuck");
        setContentView(R.layout.app_bar_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        relativeLayout = (RelativeLayout) findViewById(R.id.overview_whole);
        myRelativeLayout = (RelativeLayout) findViewById(R.id.main_view_relative_layout);
        scrollView = (ScrollView) findViewById(R.id.scroll_view_table);
        selectionToolbar = (RelativeLayout) findViewById(R.id.selection_toolbar);
        rootView = (MyRelativeLayout) findViewById(R.id.root_view);
        rootView.setMainActivity(this);
        rootView.setRelativeLayout(relativeLayout);
        blurring = (FrameLayout) findViewById(R.id.blurring);
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
                        return true;
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
        adapterForCardView.setNumberReference((TextView) selectionToolbar.findViewById(R.id.text_selection));
    }

    private void messageReceiver()
    {
        String text = search_edit_text.getText().toString();
        //Todo:Do this in a different thread here
        Cursor cursor = databaseAdapter.getSearch(text);
        if (text.length() <= 0)
        {
            if (labels_total != null)
                adapterForCardView.setFiles(new LinkedList<>(Arrays.asList(labels_total)), new LinkedList<>(Arrays.asList(images_total)));
            else adapterForCardView.setNull();
        } else if (cursor != null && cursor.getCount() > 0 && labels_total != null)
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
            updateFiles();
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
        } else if (requestCode == REQUEST_CODE_SHARE)
        {
            adapterForCardView.removeAllSelection();
            hideSelectionBar();
        }
    }


    public void fillUpRecyclerView(File[] images, File[] label)
    {
        images_total = images;
        labels_total = label;
        adapterForCardView.setFiles(new LinkedList<>(Arrays.asList(label)), new LinkedList<>(Arrays.asList(images)));
    }

    public void clickedSearch(View view)
    {
        RelativeLayout searchTool = (RelativeLayout) toolbar.findViewById(R.id.searchTool);
        searchTool.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int cx = searchTool.getMeasuredWidth();
            int cy = searchTool.getMeasuredHeight();
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
        }
    }

    @Override
    public void onBackPressed()
    {
        if (adapterForCardView.isLongClicked())
        {
            adapterForCardView.removeAllSelection();
            hideSelectionBar();
        } else if (relativeLayout.getVisibility() == View.VISIBLE)
        {
            deflate();
        } else if (toolbar.findViewById(R.id.searchTool).getVisibility() == View.VISIBLE)
        {
            closeSearchingToolbar();
        } else
            super.onBackPressed();
    }

    private void closeSearchingToolbar()
    {
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        search_edit_text.setText("");
        toolbar.findViewById(R.id.initial).setVisibility(View.VISIBLE);
        final View searchTool = toolbar.findViewById(R.id.searchTool);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int cx = searchTool.getMeasuredWidth();
            int cy = searchTool.getMeasuredHeight();
            Animator animator = ViewAnimationUtils.createCircularReveal(searchTool, cx, cy / 2, cx, 0);
            animator.addListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {

                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    searchTool.setVisibility(View.INVISIBLE);
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
        int orientation = getResources().getConfiguration().orientation;
        if (aspectRatio >= 1)
        {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setOverViewLayoutForLandscape(0.85f, aspectRatio, images);
            } else
                setOverViewLayoutForPortrait(0.65f, aspectRatio, images);
        } else
        {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setOverViewLayoutForLandscape(0.75f, aspectRatio, images);
            } else
                setOverViewLayoutForPortrait(0.85f, aspectRatio, images);
        }
        DetailedLabelShow detailedLabelShow = new DetailedLabelShow(getApplicationContext(), overview_table, this);
        detailedLabelShow.execute(label_files);
        this.rect = rect;
    }

    private void setOverViewLayoutForPortrait(float percentage, float aspectRatio, File images)
    {
        int total_width = getWindow().getDecorView().getWidth();
        int total_height = myRelativeLayout.getHeight();
        int decided_width = (int) (total_width * percentage);
        ViewGroup.LayoutParams layoutParams = overview_image.getLayoutParams();
        layoutParams.width = decided_width;
        int decided_height = (int) (decided_width * aspectRatio);
        layoutParams.height = decided_height;
        overview_image.requestLayout();

        ViewGroup.LayoutParams tableParams = overview_table.getLayoutParams();
        tableParams.width = decided_width;
        float leftHeight = total_height - decided_width * aspectRatio;
        float using_height = overview_table.getHeight();
        Log.d(logging, "leftHeight=" + leftHeight);
        Log.d(logging, "using Height=" + using_height);
        if (using_height > leftHeight)
            scrollView.getLayoutParams().height = (int) leftHeight;
        scrollView.requestLayout();
        overview_table.requestLayout();
        Glide.with(getApplicationContext()).load(images).override(decided_width, decided_height).into(overview_image);
    }

    private void setOverViewLayoutForLandscape(float percentage, float aspectRatio, File images)
    {
        int total_height = myRelativeLayout.getHeight();
        float decided_height = total_height * percentage;
        int decided_width = (int) (decided_height / aspectRatio);
        ViewGroup.LayoutParams layoutParams = overview_image.getLayoutParams();
        layoutParams.height = (int) decided_height;
        layoutParams.width = decided_width;
        overview_image.requestLayout();
        ViewGroup.LayoutParams tableParams = overview_table.getLayoutParams();
        tableParams.width = decided_width;
        float usingHeight = overview_table.getHeight();
        float leftHeight = total_height * (1 - percentage);
        if (usingHeight > leftHeight)
            tableParams.height = (int) (usingHeight = (float) (total_height * (1 - percentage - 0.04)));
        overview_table.requestLayout();
        Glide.with(getApplicationContext()).load(images).override(decided_width, (int) decided_height).into(overview_image);
    }

    public void inflate()
    {
        //ToDo:Add an animation here
        if (!animationGoing)
        {
            relativeLayout.setVisibility(View.VISIBLE);
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator animator = ObjectAnimator.ofFloat(blurring, View.ALPHA, 0f, 0.75f);
            ObjectAnimator overViewanim = ObjectAnimator.ofFloat(relativeLayout, View.ALPHA, 0f, 1f);
//        ObjectAnimator translateX=ObjectAnimator.ofFloat(relativeLayout,View.X,rect[0],x);
//        ObjectAnimator translateY=ObjectAnimator.ofFloat(relativeLayout,View.Y,rect[1],y);
//        animatorSet.play(animator).with(translateX);
//        animatorSet.play(animator).with(translateY);
//        animatorSet.start();
            animatorSet.play(animator).with(overViewanim);
            animatorSet.addListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    animationGoing = true;
                    getWindow().getDecorView().getRootView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    animationGoing = false;
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
            animatorSet.start();
        }
    }

    public void deflate()
    {
        if (!animationGoing)
        {
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator animator = ObjectAnimator.ofFloat(blurring, View.ALPHA, 0.75f, 0f);
            ObjectAnimator overViewanim = ObjectAnimator.ofFloat(relativeLayout, View.ALPHA, 1f, 0f);
            animatorSet.play(animator).with(overViewanim);
            animatorSet.addListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    animationGoing = true;
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    relativeLayout.setVisibility(View.INVISIBLE);
                    getWindow().getDecorView().getRootView().setSystemUiVisibility(0);
                    animationGoing = false;
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
            animatorSet.start();
        }
    }

    public void clickedOverviewImage(View view)
    {

    }

    public void showSelectionBar()
    {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(selectionToolbar, View.ALPHA, 0f, 1f);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                selectionToolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {

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
        objectAnimator.start();
    }

    public void hideSelectionBar()
    {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(selectionToolbar, View.ALPHA, 1f, 0f);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                selectionToolbar.setVisibility(View.INVISIBLE);
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
        objectAnimator.start();
    }

    public void removeItems(View view)
    {
        adapterForCardView.getListItems(this, 0);
    }

    public void receiverOfSelection(List<File> images_delete, List<File> labels_delete)
    {
        adapterForCardView.removeAllSelection();
        hideSelectionBar();
        DeleteFilesAsyncTask deleteFilesAsyncTask = new DeleteFilesAsyncTask(images_delete, this, databaseAdapter, getApplicationContext());
        deleteFilesAsyncTask.execute(labels_delete);
    }

    public void updateFiles()
    {
        AsyncTaskForInternalFiles asyncTaskForInternalFiles = new AsyncTaskForInternalFiles(getApplicationContext(), this);
        asyncTaskForInternalFiles.execute();
    }

    public void startAll()
    {
        inflate();
    }

    public void shareContent(View view)
    {
        adapterForCardView.getListItems(this, 1);
    }


    public void sharing(List<File> images_delete)
    {
        //ToDo: Create Uri for all the files
        if (images_delete.size() == 1)
        {
            Uri fileUri=FileProvider.getUriForFile(this,"com.totalboron.jay.labeled.MainActivity",images_delete.get(0));
            Intent shareIntent=ShareCompat.IntentBuilder.from(this).setStream(fileUri).setType("image/*").getIntent();
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (shareIntent.resolveActivity(getPackageManager())!=null)
                startActivityForResult(shareIntent,REQUEST_CODE_SHARE);
        } else
        {
            ShareCompat.IntentBuilder intentBuilder=ShareCompat.IntentBuilder.from(this).setType("image/*");
            for (int i = 0; i < images_delete.size(); i++)
            {
                intentBuilder.addStream(FileProvider.getUriForFile(this, "com.totalboron.jay.labeled.MainActivity", images_delete.get(i)));
            }
            Log.d(logging,"In Multiple");
            Intent shareIntent=intentBuilder.getIntent();

            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (shareIntent.resolveActivity(getPackageManager())!=null)
                startActivityForResult(shareIntent,REQUEST_CODE_SHARE);
        }
    }

    public void shareUris(ArrayList<Uri> uris)
    {
        Intent intent;
        if (uris.size() > 1)
        {
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris);
//            intent=Intent.createChooser(intent,"Choose");
            startActivityForResult(intent, REQUEST_CODE_SHARE);

        } else if (uris.size() == 1)
        {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            intent = Intent.createChooser(intent, "Choose");
            startActivityForResult(intent, REQUEST_CODE_SHARE);
        }
    }

    public void backButtonToolbar(View view)
    {
        adapterForCardView.removeAllSelection();
        hideSelectionBar();
    }

    public void closeSearchBar(View view)
    {
        closeSearchingToolbar();
    }
}
