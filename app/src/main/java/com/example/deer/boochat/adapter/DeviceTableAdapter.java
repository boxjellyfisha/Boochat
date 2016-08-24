package com.example.deer.boochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deer on 2015/8/3.用來存放裝置與裝置序號配對
 */
public class DeviceTableAdapter extends BaseAdapter {

    private int self;
    private Context mContext;
    private LayoutInflater mInflater;
    private Map<String,String> mTable;
    private ArrayList<String> mList;
    private List<Map<String,Object>> mMap;


    public DeviceTableAdapter()
    {
        mTable=new HashMap<String,String>();
        mList=new ArrayList<String>();
        mMap=new ArrayList<Map<String,Object>>();
    }
    public DeviceTableAdapter(Context context, LayoutInflater inflater)
    {
        mContext=context;
        mInflater=inflater;
        mTable=new HashMap<String,String>();
        mList=new ArrayList<String>();
        mMap=new ArrayList<Map<String,Object>>();
    }
    public void addinMap(String serial_number,String device_name,long id)
    {
        if(!mTable.containsKey(serial_number))
        {
            Map<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("0", id);
            tmpMap.put("1", serial_number);
            tmpMap.put("2", device_name);
            tmpMap.put("3", 0);//state sleep

            mMap.add(tmpMap);
            mTable.put(serial_number, device_name);
            mList.add(serial_number);
            //tmpMap.clear();
        }
    }
    public void renewState(String serial_number,int state)
    {
        for(int i=0;i<mMap.size();i++) {
            if(mMap.get(i).get("1")==serial_number)
            {
                mMap.get(i).remove("3");
                mMap.get(i).put("3",state);//state wake
            }
        }

    }
    public int getState(String serial_number)
    {
        for(int i=0;i<mMap.size();i++) {
            if(mMap.get(i).get("1")==serial_number)
            {
               return  (int)mMap.get(i).get("3");
            }
        }
        return -1;
    }
    public void cleanTemp(){
        mMap.clear();
        mList.clear();
        mTable.clear();
    }
    public void add(String serial_number,String device_name)
    {
        if(mTable.containsKey(serial_number));
        else
        {
            mTable.put(serial_number, device_name);
            mList.add(serial_number);
        }
    }
    public long getID(int position){
        String tmp="";
        tmp=mList.get(position);
        for(int i=0;i<mMap.size();i++) {
            if(mMap.get(i).get("1")==tmp)
            {
                return (long)mMap.get(i).get("0");
            }
        }
        return -1;
    }
    public String findBySerial(String serial_number)
    {
        return mTable.get(serial_number);
    }
    public Boolean checkBySerial(String serial_number){return mTable.containsKey(serial_number);}

    public String findByPosition(int position){
        String tmp="";
        tmp=mList.get(position);
        tmp=findBySerial(tmp);
        return tmp;
    }

    public String getSerial(int position){ return mList.get(position);}

    @Override
    public int getItemViewType(int position) {
        if(mList.get(position).equals(Constants.BuildSERIAL))
            return 0;
        else if(getState(mList.get(position))==1)
            return 1;
        else
            return -1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
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
    public long getItemId(int position) {
        return mList.get(position).hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Reuse an old view if we can, otherwise create a new one.
        int type=getItemViewType(position);
        if(view==null)
        {
            if(type==0)
            {
                view = mInflater.inflate(R.layout.device_name_list, null);
                ImageView img=(ImageView)view.findViewById(R.id.chatImage);
                Glide.with(mContext)
                        .load(R.drawable.ch3)
                        .asGif()
                        .into(img);
                //WebView chatImg=(WebView)view.findViewById(R.id.chatimg);
                //chatImg.loadUrl("file:///android_asset/ch.html");

            }
            else
            {
                view = mInflater.inflate(R.layout.device_name_2, null);
                ImageView img=(ImageView)view.findViewById(R.id.imageView2);

                if(type==1){
                    self=(int)(Math.random() *3+1);
                    switch(self)
                    {
                        case 1:
                            img.setImageResource(R.drawable.chat2);
                            break;
                        case 2:
                            img.setImageResource(R.drawable.chat3);
                            break;
                        case 3:
                            img.setImageResource(R.drawable.chat4);
                            break;
                    }
                }
                else
                    img.setImageResource(R.drawable.chat1);
            }
        }
        TextView deviceNameView = (TextView) view.findViewById(R.id.device_name_txt);
        deviceNameView.setText(findBySerial(mList.get(position)));
        TextView deviceNameView2 = (TextView) view.findViewById(R.id.device_address);
        deviceNameView2.setText(getSerial(position));
        return view;
    }

}
