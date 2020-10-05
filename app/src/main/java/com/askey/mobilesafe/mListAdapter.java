package com.askey.mobilesafe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class mListAdapter extends BaseAdapter {

    private ArrayList<mTag> arrayList;
    private Context context;

    public mListAdapter(Context context, ArrayList<mTag> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public int getCount() {
        return arrayList.size();
    }

    public Object getItem(int position) {
       View convertView = LayoutInflater.from(context).inflate(R.layout.style_vertical_item, null);
        ((TextView) convertView.findViewById(R.id.vertical_textView)).setText(arrayList.get(position).id + ", " + arrayList.get(position).tag);
        return convertView;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.style_vertical_item, null);
        ((TextView) convertView.findViewById(R.id.vertical_textView)).setText(arrayList.get(position).id + ", " + arrayList.get(position).tag);
        return convertView;
    }
}
