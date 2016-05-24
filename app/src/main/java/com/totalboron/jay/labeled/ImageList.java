package com.totalboron.jay.labeled;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by Jay on 14/04/16.
 */
public class ImageList extends RecyclerView.Adapter<ImageList.Holder>
{
    private Cursor cursor;
    private int width;
    private ImageListActivity imageListActivity;
    public ImageList(Cursor cursor, int width,ImageListActivity imageListActivity)
    {
        this.cursor = cursor;
        this.width = width;
        this.imageListActivity=imageListActivity;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_images,parent,false));
    }

    @Override
    public void onBindViewHolder(final Holder holder,int position)
    {
        ImageView imageView = holder.getImageView();
        cursor.moveToPosition(position);
        final int data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        Glide.with(imageView.getContext()).load(new File(cursor.getString(data))).override(width,width).placeholder(R.drawable.placeholder).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cursor.moveToPosition(holder.getAdapterPosition());
                imageListActivity.sendBackResult(cursor.getString(data));
            }
        });


    }

    @Override
    public int getItemCount()
    {
        return cursor.getCount();
    }

    class Holder extends RecyclerView.ViewHolder
    {
        private ImageView imageView;
        public Holder(View itemView)
        {
            super(itemView);
            ViewGroup.LayoutParams layoutParams=itemView.getLayoutParams();
            layoutParams.height=width;
            layoutParams.width=width;
            itemView.setLayoutParams(layoutParams);
            imageView=(ImageView)itemView.findViewById(R.id.image_view_images);
        }

        public ImageView getImageView()
        {
            return imageView;
        }
    }
}
