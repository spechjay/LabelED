package com.totalboron.jay.labeled;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by Jay on 16/05/16.
 */
public class MyDialog extends Dialog
{
    private ImageView imageView;
    private TableLayout tableLayout;
    private RelativeLayout baseLayout;
    private Context context;
    private File images;
    private String logging=getClass().getSimpleName();
    private File label_files;
    public MyDialog(Context context, File images, File label_files)
    {
        super(context);
        this.context=context;
        this.images=images;
        this.label_files=label_files;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout);
        imageView=(ImageView)findViewById(R.id.overview_image);
        tableLayout=(TableLayout)findViewById(R.id.overview_table);
        baseLayout=(RelativeLayout)findViewById(R.id.base_layout);
    }
    private void adjustment()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(images.getAbsolutePath(), options);
        int width = options.outWidth;
        int height = options.outHeight;
        tableLayout.removeAllViews();
        float aspectRatio = ((float) height) / width;
        Log.d(logging,aspectRatio+"");
        int orientation = context.getResources().getConfiguration().orientation;
        if (aspectRatio >= 1)
        {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setOverViewLayoutForLandscape(0.85f, aspectRatio);
            } else
                setOverViewLayoutForPortrait(0.65f, aspectRatio);
        } else
        {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setOverViewLayoutForLandscape(0.75f, aspectRatio);
            } else
                setOverViewLayoutForPortrait(0.85f, aspectRatio);
        }
    }

    private void setOverViewLayoutForPortrait(float percentage, float aspectRatio)
    {
        int total_height=getWindow().getDecorView().getHeight();
        int total_width = getWindow().getDecorView().getWidth();
        int decided_width = (int) (total_width * percentage);
        baseLayout.getLayoutParams().width=decided_width;
        baseLayout.requestLayout();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = decided_width;
        int decided_height=(int) (decided_width * aspectRatio);
        layoutParams.height = decided_height;
        imageView.requestLayout();
        ViewGroup.LayoutParams tableParams = tableLayout.getLayoutParams();
        tableParams.width = decided_width;
        //ToDo:change the usingHeight somewhere Else
        float leftHeight = total_height - decided_width * aspectRatio;
        float using_height = tableLayout.getHeight();
        if (using_height > leftHeight)
            tableParams.height = (int) (0.9 * leftHeight);
        Log.d(logging,decided_width+"Decided width");
        Log.d(logging,decided_height+"Decided Height");
        Log.d(logging,using_height+"usingHeight");
        tableLayout.requestLayout();
        Glide.with(context).load(images).override(decided_width,decided_height).into(imageView);
    }

    private void setOverViewLayoutForLandscape(float percentage, float aspectRatio)
    {
        int total_height = getWindow().getDecorView().getHeight();
        float decided_height = total_height * percentage;
        int decided_width = (int) (decided_height / aspectRatio);
        baseLayout.getLayoutParams().width=decided_width;
        baseLayout.requestLayout();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) decided_height;
        layoutParams.width = decided_width;
        imageView.requestLayout();
        ViewGroup.LayoutParams tableParams = tableLayout.getLayoutParams();
        tableParams.width = decided_width;
        //ToDo:Change the using height somewhere else
        float usingHeight = tableLayout.getHeight();
        float leftHeight = total_height * (1 - percentage);
        if (usingHeight > leftHeight)
            tableParams.height = (int) (total_height * (1 - percentage - 0.04));
        Log.d(logging,decided_width+"Decided width");
        Log.d(logging,decided_height+"Decided Height");
        Log.d(logging,usingHeight+"usingHeight");
        tableLayout.requestLayout();
        Glide.with(context).load(images).override(decided_width, (int) decided_height).into(imageView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        adjustment();
        DetailedLabelShow detailedLabelShow = new DetailedLabelShow(context, tableLayout, this);
        detailedLabelShow.execute(label_files);

    }
}
