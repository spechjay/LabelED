package com.totalboron.jay.labeled;

import android.Manifest;
import android.animation.Animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FragmentList.OnFragmentListUpdate
{
    private final int REQUEST_PICTURE = 1;
    private final int SPEECH_CODE = 55;
    private String logging = getClass().getSimpleName();
    private Toolbar toolbar;
    private AutoCompleteTextView search_edit_text;
    private DatabaseAdapter databaseAdapter;
    private ImageView overview_image;
    private TableLayout overview_table;
    private RelativeLayout relativeLayout;
    private RelativeLayout selectionToolbar;
    private FrameLayout blurring;
    private MyRelativeLayout rootView;
    private ScrollView scrollView;
    private FragmentList fragmentList;
    private boolean animationGoing = false;
    private int REQUEST_CODE_SHARE = 94;
    private int REQUEST_READ_STORAGE = 53;
    private FrameLayout container;
    private int GET_IMAGE = 88;
    private String sent_path;

    //ToDo:One bug: When searched and then deleted the list loads all the selections and not just the one needed.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        relativeLayout = (RelativeLayout) findViewById(R.id.overview_whole);
        container = (FrameLayout) findViewById(R.id.container_for_fragment);
        fragmentList = FragmentList.newInstance();
        scrollView = (ScrollView) findViewById(R.id.scroll_view_table);
        selectionToolbar = (RelativeLayout) findViewById(R.id.selection_toolbar);
        rootView = (MyRelativeLayout) findViewById(R.id.root_view);
        if (rootView != null)
        {
            rootView.setMainActivity(this);
            rootView.setRelativeLayout(relativeLayout);
        }
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
                        fragmentList.messageReceiver(search_edit_text.getText().toString());
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });
        }
        databaseAdapter = new DatabaseAdapter(getApplicationContext());
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
        search_edit_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

                fragmentList.messageReceiver(search_edit_text.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        overview_image = (ImageView) findViewById(R.id.overview_image);
        overview_table = (TableLayout) findViewById(R.id.overview_table);
        fragmentList.setNumberReference((TextView) toolbar.findViewById(R.id.text_selection));
        fragmentList.setDatabaseAdapter(databaseAdapter);
        getFragmentManager().beginTransaction().add(R.id.container_for_fragment, fragmentList).commit();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        sent_path = savedInstanceState.getString("PATH");
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (toolbar.findViewById(R.id.searchTool).getVisibility() == View.INVISIBLE)
        {
            fragmentList.updateFiles();
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
                fragmentList.messageReceiver(result.get(0));
            }
        } else if (requestCode == REQUEST_CODE_SHARE)
        {
            fragmentList.removeAllSelection();
            hideSelectionBar();
        } else if (requestCode == GET_IMAGE && resultCode == RESULT_OK)
        {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(new File(sent_path)));
            this.sendBroadcast(mediaScanIntent);
            Intent intent = new Intent(this, DrawingScene.class);
            intent.setAction("Main_Activity");
            intent.putExtra("PATH", sent_path);
            startActivity(intent);
        }
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
        if (fragmentList.isLongClicked())
        {
            fragmentList.removeAllSelection();
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

    public void setUpOverview(DisplayObject overview_object)
    {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(overview_object.getImageFile().getAbsolutePath(), options);
//        int width = options.outWidth;
//        int height = options.outHeight;
//        overview_image.setImageBitmap(null);
//        overview_table.removeAllViews();
//        float aspectRatio = ((float) height) / width;
//        int orientation = getResources().getConfiguration().orientation;
//        if (aspectRatio >= 1)
//        {
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
//            {
//                setOverViewLayoutForLandscape(0.85f, aspectRatio, overview_object.getImageFile());
//            } else
//                setOverViewLayoutForPortrait(0.65f, aspectRatio, overview_object.getImageFile());
//        } else
//        {
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
//            {
//                setOverViewLayoutForLandscape(0.75f, aspectRatio, overview_object.getImageFile());
//            } else
//                setOverViewLayoutForPortrait(0.85f, aspectRatio, overview_object.getImageFile());
//        }
//        DetailedLabelShow detailedLabelShow = new DetailedLabelShow(getApplicationContext(), overview_table, this);
//        detailedLabelShow.execute(overview_object.getLabelFile());
        Intent intent=new Intent(this,DetailActivity.class);
        intent.putExtra("DISPLAY_OBJECT",overview_object);
        startActivity(intent);
    }

    private void setOverViewLayoutForPortrait(float percentage, float aspectRatio, File images)
    {
        int total_width = getWindow().getDecorView().getWidth();
        int total_height = rootView.getHeight();
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
        int total_height = rootView.getHeight();
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
        fragmentList.getListItems(0);
    }

    public void receiverOfSelection(List<DisplayObject> newList)
    {
        fragmentList.removeAllSelection();
        hideSelectionBar();
        DeleteFilesAsyncTask deleteFilesAsyncTask = new DeleteFilesAsyncTask(fragmentList, databaseAdapter, getApplicationContext());
        deleteFilesAsyncTask.execute(newList);
    }

    public void startAll()
    {
        inflate();
    }

    public void shareContent(View view)
    {
        fragmentList.getListItems(1);
    }


    public void sharing(List<DisplayObject> newList)
    {
        //ToDo: Create Uri for all the files
        if (newList.size() == 1)
        {
            Uri fileUri = FileProvider.getUriForFile(this, "com.totalboron.jay.labeled.MainActivity", newList.get(0).getImageFile());
            Intent shareIntent = ShareCompat.IntentBuilder.from(this).setStream(fileUri).setType("image/*").getIntent();
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (shareIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(shareIntent, REQUEST_CODE_SHARE);
        } else
        {
            ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this).setType("image/*");
            for (int i = 0; i < newList.size(); i++)
            {
                intentBuilder.addStream(FileProvider.getUriForFile(this, "com.totalboron.jay.labeled.MainActivity", newList.get(i).getImageFile()));
            }
            Log.d(logging, "In Multiple");
            Intent shareIntent = intentBuilder.getIntent();

            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (shareIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(shareIntent, REQUEST_CODE_SHARE);
        }
    }


    public void backButtonToolbar(View view)
    {
        fragmentList.removeAllSelection();
        hideSelectionBar();
    }

    public void closeSearchBar(View view)
    {
        closeSearchingToolbar();
    }

    public void clickAPhoto(View view)
    {
        checkForPermissions();
    }

    private void loadEverything()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri());
        startActivityForResult(intent, GET_IMAGE);
    }

    private Uri getFileUri()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (!file.exists())
            file.mkdirs();
        file = new File(file, "IMG_" + timeStamp + ".jpg");
        sent_path = file.getAbsolutePath();
        return Uri.fromFile(file);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString("PATH", sent_path);
        super.onSaveInstanceState(outState);
    }

    private void checkForPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                loadEverything();
            } else
            {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Toast.makeText(getApplicationContext(), "Read and Write Access required to click Image", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            }
        else loadEverything();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (requestCode == REQUEST_READ_STORAGE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    loadEverything();
                else
                {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
    }
}
