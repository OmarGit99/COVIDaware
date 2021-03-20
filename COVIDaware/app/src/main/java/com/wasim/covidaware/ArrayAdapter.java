package com.wasim.covidaware;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

class ArrayAdapter extends BaseAdapter {
    Context context;
    private ArrayList<String> list1;
    private ArrayList<String> list2;
    private ArrayList<String> list3;
    private ArrayList<String> list4;

    public ArrayAdapter(Context context, ArrayList<String>dname,ArrayList<String>ac,ArrayList<String>dc,ArrayList<String>pd) {
        this.context= context;
        this.list1= dname;
        this.list2= ac;
        this.list3= dc;
        this.list4= pd;

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View convertView =  view;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item,viewGroup,false);

        }
        TextView t1 = (TextView) convertView.findViewById(R.id.dname);
        TextView t2 = (TextView) convertView.findViewById(R.id.Ac);
        TextView t3 = (TextView) convertView.findViewById(R.id.dc);
        TextView t4 = (TextView) convertView.findViewById(R.id.pd);


        // Verify value of position not greater than size of ArrayList.
        if(position < list1.size())
            t1.setText("District : "+list1.get(position));

        if(position< list2.size())
            t2.setText("Active Cases : "+list2.get(position));

        if(position < list3.size())
            t3.setText("Death Count : "+list3.get(position));

        if(position< list4.size())
            t4.setText("Prediction : "+list4.get(position));

        return convertView;
    }

    @Override
    public int getCount()
    {
        return Math.min(Math.min(list1.size(),list2.size()),Math.min(list3.size(),list4.size()));
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
