package com.totalboron.jay.labeled;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jay on 20/05/16.
 */
public class FragmentList extends Fragment
{
    private RecyclerView recyclerView;
    private AdapterForCardView adapterForCardView;
    private Context context;
    private DatabaseAdapter databaseAdapter;
    private List<DisplayObject> object_total = null;
    private OnFragmentListUpdate onFragmentListUpdate;
    private TextView textSelectionNumber;
    private String logging = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        context = getActivity().getApplicationContext();
        Log.d(logging, "onCreateFragment Called");
        super.onCreate(savedInstanceState);
    }


    public static FragmentList newInstance()
    {
        Bundle args = new Bundle();
        FragmentList fragment = new FragmentList();
        fragment.setArguments(args);
        return fragment;
    }

    public void setDatabaseAdapter(DatabaseAdapter databaseAdapter)
    {
        this.databaseAdapter = databaseAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        onFragmentListUpdate = (OnFragmentListUpdate) getActivity();
        View view = inflater.inflate(R.layout.fragment_list_main, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        adapterForCardView = new AdapterForCardView(context, this, recyclerView, onFragmentListUpdate);
        int cnum = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, cnum);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapterForCardView);
        adapterForCardView.setNumberReference(textSelectionNumber);
        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    protected void fillUpRecyclerView(List<DisplayObject> displayObjects)
    {
        this.object_total = displayObjects;
        adapterForCardView.setFiles(displayObjects);
    }

    public void setNumberReference(TextView textViewSelection)
    {
        textSelectionNumber = textViewSelection;
    }

    public void updateFiles()
    {
        AsyncTaskForInternalFiles asyncTaskForInternalFiles = new AsyncTaskForInternalFiles(context, this);
        asyncTaskForInternalFiles.execute();
    }

    public void removeAllSelection()
    {
        adapterForCardView.removeAllSelection();
    }

    public void getListItems(int i)
    {
        adapterForCardView.getListItems(i);
    }

    public void receiverOfSelection(List<DisplayObject> selectionList)
    {
        onFragmentListUpdate.receiverOfSelection(selectionList);
    }

    public void sharing(List<DisplayObject> selectionList)
    {
        onFragmentListUpdate.sharing(selectionList);
    }

    public boolean isLongClicked()
    {
        return adapterForCardView.isLongClicked();
    }


    interface OnFragmentListUpdate
    {
        public void setUpOverview(DisplayObject displayObject);

        public void showSelectionBar();

        public void hideSelectionBar();

        public void receiverOfSelection(List<DisplayObject> newList);

        public void sharing(List<DisplayObject> newList);
    }

    protected void messageReceiver(String text)
    {
        //Todo:Do this in a different thread here
        Cursor cursor = databaseAdapter.getSearch(text);
        if (text.length() <= 0)
        {
            if (object_total != null)
                adapterForCardView.setFiles(object_total);
            else adapterForCardView.setNull();
        } else if (cursor != null && cursor.getCount() > 0 && object_total != null)
        {
            String label_name;
            List<DisplayObject> results = new ArrayList<>();
            while (cursor.moveToNext())
            {
                label_name = cursor.getString(0);
                int index = indexOf(object_total, label_name);
                if (index >= 0 && !checkForDuplicate(label_name, results))
                {
                    results.add(object_total.get(index));
                }
            }
            adapterForCardView.setFiles(results);
        } else
        {
            adapterForCardView.setNull();
        }
        recyclerView.scrollToPosition(0);
    }

    private boolean checkForDuplicate(String label_name, List<DisplayObject> newList)
    {
        if (newList != null)
        {
            for (int i = 0; i < newList.size(); i++)
            {
                if (newList.get(i).getLabelFile().getName().equals(label_name))
                    return true;
            }
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


}
