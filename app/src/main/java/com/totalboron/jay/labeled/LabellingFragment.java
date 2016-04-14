package com.totalboron.jay.labeled;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Jay on 04/04/16.
 */
public class LabellingFragment extends Fragment
{
    private EditText editText;
    private MessageInterface messageInterface;
    private Button button;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        messageInterface=(MessageInterface)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.label_view, container, false);
        FocusChangeListener focusChangeListener=new FocusChangeListener(container.getContext());
        button=(Button)view.findViewById(R.id.button_Clicker);
        editText=(EditText)view.findViewById(R.id.edit_text);
                editText.setOnFocusChangeListener(focusChangeListener);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                messageInterface.messageReceiver(editText.getText().toString());
                messageInterface.functionComplete();
            }
        });
        editText.requestFocus();



        return view;
    }
    public interface MessageInterface
    {
        void messageReceiver(String string);
        void functionComplete();
    }
}
