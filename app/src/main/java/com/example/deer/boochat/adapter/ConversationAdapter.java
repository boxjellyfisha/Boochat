package com.example.deer.boochat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deer on 2015/8/22.
 */
public class ConversationAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<Map<String,Object>> mList;

    private List<Integer> mPic;

    private String yname="Red Hong";

    private Date mDate;

    private int count=0;

    int previous;

    public ConversationAdapter(LayoutInflater inflater)
    {
        mInflater=inflater;
        mList=new ArrayList<Map<String,Object>>();
        mPic=new ArrayList<>();
    }

    public void add(long d,long t,String s,int b,int i)
    {
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("id",d);
        map.put("time",t);
        map.put("message",s);
        map.put("flag",b);  //哪一種排版
        map.put("sendstate",i); //寄送狀態
        mList.add(map);
        //map.clear();
    }
    public long getID(int pos){
        return (long)mList.get(pos).get("id");
    }
    public void clear() {
        mList.clear();
    }
    public void setDeviceName(String n){yname=n;}

    private void setName(View v)
    {
      TextView name=(TextView)v.findViewById(R.id.your_name);
        name.setText(yname);
    }
    @Override
    public int getItemViewType(int position) {
        if((int)mList.get(position).get("flag")==0)
            return 0;
        else
            return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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

        //ViewHolder holder = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        long t = (long)mList.get(position).get("time");
        String dts=sdf.format(new Date(t));
        String dts2=sdf2.format(new Date(t));
        int flag=getItemViewType(position);//(int)mList.get(position).get("flag");
        if(position>0)
        previous =(int)mList.get(position-1).get("flag");

        if(view == null)
        {
            if (flag==0){
                view = mInflater.inflate(R.layout.i_say, null);
            }
            else if(flag==1){
                view = mInflater.inflate(R.layout.you_say, null);
                setName(view);
            }
            else
            {view = mInflater.inflate(R.layout.image_of_mysticker, null);}
        }

        if (flag==3)
        {
            return view;
        }
        else{
        TextView sentence=(TextView) view.findViewById(R.id.c_s);
        TextView time=(TextView) view.findViewById(R.id.c_time);
        sentence.setText((String)mList.get(position).get("message"));
        if((int)mList.get(position).get("sendstate")==1)
            time.setText(dts + "\n" + dts2);
            else
            time.setText("傳送中");}

        return view;
    }

}
