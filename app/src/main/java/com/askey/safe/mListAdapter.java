package com.askey.safe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class mListAdapter extends BaseAdapter {

    private ArrayList<String> arrayList;
    private Context context;

    public mListAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public int getCount() {
        return arrayList.size();
    }

    public Object getItem(int position) {
       View convertView = LayoutInflater.from(context).inflate(R.layout.style_vertical_item, null);
        ((TextView) convertView.findViewById(R.id.text)).setText(arrayList.get(position));
        return convertView;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.style_vertical_item, null);
        ((TextView) convertView.findViewById(R.id.text)).setText(arrayList.get(position));
        return convertView;
    }
}
