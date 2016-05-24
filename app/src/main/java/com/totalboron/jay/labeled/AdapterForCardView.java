package com.totalboron.jay.labeled;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterForCardView extends RecyclerView.Adapter<AdapterForCardView.CardAdapterHolder>
{
    private Context context;
    private List<DisplayObject> displayObjects;
    private FragmentList fragmentList;
    private FragmentList.OnFragmentListUpdate onFragmentListUpdate;
    private String logging = getClass().getSimpleName();
    private boolean longClicked = false;
    private List<Integer> selection_list;
    private List<CardAdapterHolder> holders;
    private TextView numberReference;
    private RecyclerView recyclerView;

    public AdapterForCardView(Context context, FragmentList fragmentList, RecyclerView recyclerView, FragmentList.OnFragmentListUpdate onFragmentListUpdate)
    {
        this.context = context;
        displayObjects = null;
        this.fragmentList = fragmentList;
        selection_list = new ArrayList<>();
        holders = new ArrayList<>();
        this.recyclerView = recyclerView;
        this.onFragmentListUpdate = onFragmentListUpdate;
    }

    @Override
    public CardAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_recycler, parent, false);
        CardAdapterHolder cardAdapterHolder = new CardAdapterHolder(view);
        holders.add(cardAdapterHolder);
        return cardAdapterHolder;
    }

    @Override
    public void onBindViewHolder(final CardAdapterHolder holder, int position)
    {
        Glide.with(context).load(displayObjects.get(position).getImageFile()).into(holder.getImageView());
        if (longClicked)
        {
            if (selection_list.contains(position))
                holder.highlight();
            else holder.deHighlight();
        }
        holder.getImageView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!longClicked)
                {
                    onFragmentListUpdate.setUpOverview(displayObjects.get(holder.getAdapterPosition()));
                } else
                {
                    longClickSelection(holder);
                }
            }
        });
        holder.getImageView().setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                longClicked = true;
                longClickSelection(holder);
                return true;
            }
        });

        if (position != holder.Position())
        {
            if (holder.Position() != -1) holder.getLoadingText().cancel(true);
            removeAllText(holder.getTableLayout());
            LoadingText loadingText = new LoadingText(context, holder.getTableLayout());
            holder.setLoadingText(loadingText, position);
            loadingText.execute(displayObjects.get(position).getLabelFile());
        }
    }

    private void longClickSelection(CardAdapterHolder cardAdapterHolder)
    {
        if (selection_list.contains(cardAdapterHolder.getAdapterPosition()))
        {
            selection_list.remove(Integer.valueOf(cardAdapterHolder.getAdapterPosition()));
            cardAdapterHolder.deHighlight();
            numberReference.setText(selection_list.size() + "");
            checkIfLast();
        } else
        {
            if (selection_list.size() == 0)
                showSelectionBar();
            selection_list.add(cardAdapterHolder.getAdapterPosition());
            numberReference.setText(selection_list.size() + "");
            cardAdapterHolder.highlight();
        }
    }

    private void showSelectionBar()
    {
        onFragmentListUpdate.showSelectionBar();
    }

    private void hideSelectionBar()
    {
        onFragmentListUpdate.hideSelectionBar();
    }

    private void checkIfLast()
    {
        if (selection_list.size() == 0)
        {
            longClicked = false;
            hideSelectionBar();
        }
    }

    public void setNull()
    {
        displayObjects = null;
        notifyDataSetChanged();
    }

    private void removeAllText(TableLayout tableLayout)
    {
        tableLayout.removeAllViews();
    }

    public void setFiles(List<DisplayObject> objects)
    {
        objects = getNewList(objects);
        if (displayObjects == null)
        {
            this.displayObjects = objects;
            notifyDataSetChanged();
        } else
        {
            animateTo(objects);
        }
    }

    private List<DisplayObject> getNewList(List<DisplayObject> original)
    {
        List<DisplayObject> duplicate = new ArrayList<>();
        for (int i = 0; i < original.size(); i++)
        {
            duplicate.add(original.get(i));
        }
        return duplicate;
    }

    private void removeItem(int pos)
    {
        displayObjects.remove(pos);
        notifyItemRemoved(pos);
    }

    private void addItem(DisplayObject displayObject)
    {
        displayObjects.add(displayObject);
        notifyItemInserted(displayObjects.size() - 1);
    }

    private void moveItem(int fromPosition, int toPosition)
    {
        DisplayObject item = displayObjects.remove(fromPosition);
        displayObjects.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void animateTo(List<DisplayObject> newList)
    {
        applyAndAnimateRemovals(newList);
        applyAndAnimateAddition(newList);
        applyAndAnimateMove(newList);

    }

    private void applyAndAnimateMove(List<DisplayObject> newList)
    {
        for (int toPosition = 0; toPosition < newList.size(); toPosition++)
        {
            int fromPosition = indexOf(displayObjects, newList.get(toPosition).getLabelFile().getName());
            if (fromPosition >= 0 && toPosition != fromPosition)
                moveItem(fromPosition, toPosition);
        }
    }

    private void applyAndAnimateAddition(List<DisplayObject> newList)
    {
        for (int i = 0; i < newList.size(); i++)
        {
            if (!contains(displayObjects, newList.get(i).getLabelFile().getName()))
            {
                addItem(newList.get(i));
            }
        }
    }

    private void applyAndAnimateRemovals(List<DisplayObject> newList)
    {
        for (int i = displayObjects.size() - 1; i >= 0; i--)
        {
            if (!contains(newList, displayObjects.get(i).getLabelFile().getName()))
            {
                removeItem(i);
            }
        }
    }

    private boolean contains(List<DisplayObject> fileList, String label)
    {
        for (int i = 0; i < fileList.size(); i++)
        {
            if (fileList.get(i).getLabelFile().getName().equals(label))
                return true;
        }
        return false;
    }

    private int indexOf(List<DisplayObject> newList, String label)
    {
        for (int i = 0; i < newList.size(); i++)
        {
            if (newList.get(i).getLabelFile().getName().equals(label))
                return i;
        }
        return -99;
    }


    @Override
    public int getItemCount()
    {
        return displayObjects == null ? 0 : displayObjects.size();
    }


    public void setNumberReference(TextView numberReference)
    {
        this.numberReference = numberReference;
    }

    public void getListItems(int requestCode)
    {
        List<DisplayObject> selections = new ArrayList<>();
        int selectin = -1;
        for (int i = 0; i < selection_list.size(); i++)
        {
            selectin = selection_list.get(i);
            selections.add(displayObjects.get(selectin));
        }
        if (requestCode == 0)
            fragmentList.receiverOfSelection(selections);
        else fragmentList.sharing(selections);
    }

    class CardAdapterHolder extends RecyclerView.ViewHolder
    {
        private TableLayout tableLayout;
        private ImageView imageView;
        private LoadingText loadingText;
        private int position;
        private FrameLayout frameLayout;
        private ImageView imageSelection;

        public CardAdapterHolder(View itemView)
        {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view_card_view);
            tableLayout = (TableLayout) itemView.findViewById(R.id.tableLayout);
            frameLayout = (FrameLayout) itemView.findViewById(R.id.selection_scrim);
            imageSelection = (ImageView) itemView.findViewById(R.id.selection_done);
            position = -1;
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

        public void setLoadingText(LoadingText loadingText, int position)
        {
            this.loadingText = loadingText;
            this.position = position;
        }

        public void highlight()
        {
            frameLayout.setVisibility(View.VISIBLE);
            imageSelection.setVisibility(View.VISIBLE);
        }

        public void deHighlight()
        {
            frameLayout.setVisibility(View.INVISIBLE);
            imageSelection.setVisibility(View.INVISIBLE);
        }

        public int Position()
        {
            return position;
        }
    }

    public void removeAllSelection()
    {
        selection_list.clear();
        longClicked = false;
        for (int i = 0; i < holders.size(); i++)
        {
            holders.get(i).deHighlight();
        }
    }

    public boolean isLongClicked()
    {
        return longClicked;
    }

    public List<Integer> getSelection_list()
    {
        return selection_list;
    }
}
