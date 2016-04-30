package com.totalboron.jay.labeled;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class AdapterForCardView extends RecyclerView.Adapter<AdapterForCardView.CardAdapterHolder>
{
    private Context context;
    private File[] image_files;
    private File[] strings_of_files;
    private String logging=getClass().getSimpleName();
    public AdapterForCardView(Context context, File[] image_files, File[] strings_of_files)
    {
        this.context = context;
        this.image_files = image_files;
        this.strings_of_files = strings_of_files;
    }

    @Override
    public CardAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(context).inflate(R.layout.card_view_recycler,parent,false);
        return new CardAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(CardAdapterHolder holder, int position)
    {
        Glide.with(context).load(image_files[position]).into(holder.getImageView());
        if (position!=holder.Position())
        {
            if (holder.Position()!=-1)holder.getLoadingText().cancel(true);
            removeAllText(holder.getTableLayout());
            LoadingText loadingText = new LoadingText(context,holder.getTableLayout());
            holder.setLoadingText(loadingText, position);
            loadingText.execute(strings_of_files[position]);
        }
    }

    private void removeAllText(TableLayout tableLayout)
    {
        tableLayout.removeAllViews();
    }

    public void setStrings_of_files(File[] strings_of_files)
    {
        this.strings_of_files = strings_of_files;
    }

    public void setImage_files(File[] image_files)
    {
        this.image_files = image_files;
    }

    @Override
    public int getItemCount()
    {
        return image_files==null?0:image_files.length;
    }

    class CardAdapterHolder extends RecyclerView.ViewHolder
    {
        private TableLayout tableLayout;
        private ImageView imageView;
        private LoadingText loadingText;
        private int position;
        public CardAdapterHolder(View itemView)
        {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.image_view_card_view);
            tableLayout=(TableLayout)itemView.findViewById(R.id.tableLayout);
            position=-1;
        }

        public ImageView getImageView()
        {
            return imageView;
        }

        public TableLayout getTableLayout()
        {
            return tableLayout;
        }

        public LoadingText getLoadingText()
        {
            return loadingText;
        }

        public void setLoadingText(LoadingText loadingText,int position)
        {
            this.loadingText = loadingText;
            this.position=position;
        }

        public int Position()
        {
            return position;
        }
    }
}
