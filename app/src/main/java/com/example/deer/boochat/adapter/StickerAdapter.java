package com.example.deer.boochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.deer.boochat.R;

import java.util.ArrayList;

/**
 * Created by deer on 2015/8/22.
 */
public class StickerAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<Integer> mList;

    private int[] imgR;

    public StickerAdapter(LayoutInflater inflater)
    {
        mInflater=inflater;
        mList=new ArrayList<Integer>();
    }

    public void add(int b)
    {
        mList.add(b);
    }
    public void add(int[] b)
    {
        imgR=b;
    }

    @Override
    public int getCount() {
        return mList.size();
    }
    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }
    @Override
    public long getItemId(int position){return mList.get(position).hashCode();}
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Reuse an old view if we can, otherwise create a new one.
        if (view == null) {
            view = mInflater.inflate(R.layout.sticker_image, null);
        }
        ImageView img=(ImageView) view.findViewById(R.id.imageview);
        img.setImageResource(mList.get(position));
        return view;
    }
}
