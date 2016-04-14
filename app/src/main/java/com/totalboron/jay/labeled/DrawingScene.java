package com.totalboron.jay.labeled;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingScene extends FragmentActivity implements LabellingFragment.MessageInterface
{
    private boolean isFragmentPresent = false;
    private String logging = getClass().getSimpleName();
    private final String TAG_FOR_FRAGMENT = "LabelView";
    private LabellingFragment labellingFragment;
    private DrawingView drawingView;
    private String filePath;
    private  int imageHeight=0;
    private  int imageWidth=0;
    private SeekBar seekBar;
    private LinearLayoutCustom toolbars;
    private LinearLayout linearLayout;
    private boolean colorsBarOpen=true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_scene);
        Intent intent=getIntent();
        if (intent.getAction().equals("Main_Activity"))
        {
            String string = intent.getStringExtra("PATH");
            filePath = string;
            Log.d(logging, string);
        }
        else if (intent.getAction().equals(Intent.ACTION_SEND)&&intent.getType().startsWith("image/"))
        {
                handleSendImages(intent);
        }
        else finish();
        drawingView=(DrawingView)findViewById(R.id.drawing_view);
        seekBar=(SeekBar)findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                drawingView.setSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        toolbars=(LinearLayoutCustom)findViewById(R.id.toolsToShow);
        linearLayout=(LinearLayout)findViewById(R.id.colorsBar);
        colorsBarOpen = linearLayout.getVisibility() == View.VISIBLE;
    }

    private void handleSendImages(Intent intent)
    {
        Uri uri=intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Log.d(logging,uri.getPath());
        if (uri!=null)
        {
            pathFromUri(uri);
        }
        else finish();

    }
    private void pathFromUri(Uri uri)
    {
        Log.d(logging,"inUriPath");
        Cursor cursor=null;
        String[] projection={MediaStore.Images.Media.DATA};
        cursor=getApplicationContext().getContentResolver().query(uri,projection,null,null,null);
        int columnIndex=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        filePath=cursor.getString(columnIndex);
    }



    @Override
    protected void onResume()
    {
        setState();
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        Log.d(logging,"onWindowFocussedChange");
        loadImage();
    }

    private void setState()
    {
        Log.d(logging,"insetState");
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public void loadFragment(View view)
    {
        LabellingFragment labellingFragment = new LabellingFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relative_layout, labellingFragment, TAG_FOR_FRAGMENT).commit();
        this.labellingFragment=labellingFragment;
        isFragmentPresent = true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(logging, "in onKeyDown");
        if (isFragmentPresent == true)
        {

            if (getCurrentFocus() != null)
            {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getFragmentManager().findFragmentByTag(TAG_FOR_FRAGMENT).getView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
            removeFragment();


            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void removeFragment()
    {
        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_FOR_FRAGMENT);
        getFragmentManager().beginTransaction().remove(fragment).commit();
        labellingFragment=null;
        isFragmentPresent = false;
        setState();
    }

    public void checkForOpenFragment()
    {
        if (isFragmentPresent)
            removeFragment();
    }


    @Override
    public void messageReceiver(String string)
    {
        Log.d(logging,"inMessageReciver");
        drawingView.sendMessage(string);
    }

    @Override
    public void functionComplete()
    {
        removeFragment();
    }

    public void loadImage()
    {
        int width=getWindow().getDecorView().getWidth();
        float aspectRatio=aspectRatioFind();
        int height=(int)(width*aspectRatio);
        setParams(width,height);
        drawingView.setDrawingScene(this);
        Glide.with(getApplicationContext()).load(filePath).into(drawingView);
    }
    private void setParams(int width,int height)
    {
        Log.d(logging,"insetParams");
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) drawingView.getLayoutParams();
        layoutParams.height=height;
        drawingView.setLayoutParams(layoutParams);
    }

    private float aspectRatioFind()
    {
        Log.d(logging,"Start of AspectRatio");
        File file=new File(filePath);
        BitmapFactory.Options bmoption=new BitmapFactory.Options();
        bmoption.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bmoption);
        imageHeight=bmoption.outHeight;
        imageWidth=bmoption.outWidth;
        float aspectRatio=imageHeight/(float)imageWidth;
        Log.d(logging,"End of aspectRatio");
        return aspectRatio;
    }



    public void saveImage(View view) throws FileNotFoundException
    {
        File file=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        file=new File(file+"/LabelED");
        if (!file.exists())
            file.mkdir();
        drawingView.setDrawingCacheEnabled(true);
        drawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        drawingView.buildDrawingCache();
        Bitmap bitmap=drawingView.getDrawingCache();
        Uri uri=Uri.parse(filePath);
        String fileName=uri.getLastPathSegment();
        Log.d(logging,file.getAbsolutePath()+fileName);
        String filePath=file.getAbsolutePath()+"/"+"LabelED"+fileName;
        try
        {
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85,fileOutputStream);
            Log.d(logging,"Reached almost end");
            fileOutputStream.close();
        } catch (IOException e)
        {
            Log.d(logging,e.toString());
        }
        MediaStore.Images.Media.insertImage(getContentResolver(),filePath,fileName,"LabelED created");
        finish();
    }

    public void clearLabel(View view)
    {
        checkForOpenFragment();
        drawingView.reset();
    }


    public void singleTapDone()
    {
        int visibility=toolbars.getVisibility();
        if (visibility==View.INVISIBLE)
            toolsToggle(true);
        else toolsToggle(false);
    }

    public void toolsToggle(boolean val)
    {
        if (val)
        {
            if (colorsBarOpen)
            {
                seekBar.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
            }
            toolbars.setVisibility(View.VISIBLE);
        }
        else
        {
            if (colorsBarOpen)
            {
                seekBar.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
            }
            toolbars.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    protected void onStop()
    {
        super.onStop();
        checkForOpenFragment();
    }

    public void blackButton(View view)
    {
        drawingView.setBlackColor();
    }

    public void redButton(View view)
    {
        drawingView.setRedColor();
    }

    public void greenButton(View view)
    {
        drawingView.setGreenColor();
    }

    public void blueButton(View view)
    {
        drawingView.setBlueColor();
    }

    public void whiteButton(View view)
    {
        drawingView.setWhiteColor();
    }

    public void toggleColor(View view)
    {
        if (colorsBarOpen)
        {
            colorsBarOpen=false;
            linearLayout.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
        }
        else {
            colorsBarOpen=true;
            linearLayout.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
        }

    }
}
