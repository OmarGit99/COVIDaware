package com.wasim.covidaware;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ArrayAdapter extends BaseAdapter {
    Context context;
    private ArrayList<String> list1;
    private ArrayList<String> list2;

    public ArrayAdapter(Context context, ArrayList<String>list1, ArrayList<String>list2) {
        this.context= context;
        this.list1= list1;
        this.list2= list2;

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View convertView =  view;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item,viewGroup,false);

        }
        TextView t1 = (TextView) convertView.findViewById(R.id.tv1);
        TextView t2 = (TextView) convertView.findViewById(R.id.tv2);

        // Verify value of position not greater than size of ArrayList.
        if(position < list1.size())
            t1.setText(list1.get(position));

        if(position< list2.size())
            t2.setText(list2.get(position));

        return convertView;
    }

    @Override
    public int getCount()
    {
        if(list1.size() < list2.size())
            return list2.size();
        else
            return list1.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



}
