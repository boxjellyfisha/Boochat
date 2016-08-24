package com.example.deer.boochat.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by deer on 2015/8/31.
 */
public class NoticeAdapter extends BaseAdapter {



    private LayoutInflater mInflater;

    private List<Map<String,Object>> mMap;

    private int flag=0;

    public int size=0;

    public NoticeAdapter(LayoutInflater inflater) {
        super();

        mInflater = inflater;
        mMap=new ArrayList<Map<String,Object>>();

    }
    public void add(long t,String s,String n,String a)
    {
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("time", t);
        map.put("message",s);
        map.put("name", n);
        map.put("address",a);
        mMap.add(map);
        //map.clear();
    }
    public void sort()
    {
        Collections.sort(mMap, new Comparator() {

            long t1,t2;
            int t;
            @Override
            public int compare(Object o1, Object o2) {
                t1=(long)((Map<String,Object>)o1).get("time");
                t2=(long)((Map<String,Object>)o2).get("time");
                t=(int)(t1-t2);
                return t*(-1); //由大到小排列 最大為最新資訊
            }

        });

    }
    public long getTime(int pos){return (long)mMap.get(pos).get("time");}
    public String getName(int pos){return (String)mMap.get(pos).get("name");}
    public String getSerial(int pos){return (String)mMap.get(pos).get("address");}
    @Override
    public int getCount() {
        return mMap.size();
    }

    @Override
    public Object getItem(int position) {
        return mMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMap.get(position).hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Reuse an old view if we can, otherwise create a new one.
        if (view == null) {
            view = mInflater.inflate(R.layout.notice, null);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        long t = (long)mMap.get(position).get("time");
        int color = Color.argb(255, 255, 175, 64);
        int r,g,b;
        String dts=sdf.format(new Date(t));
        TextView nameView = (TextView) view.findViewById(R.id.n_device_name);
        TextView contentView = (TextView) view.findViewById(R.id.n_content);
        TextView timeView = (TextView) view.findViewById(R.id.n_date);
        ImageView i1=(ImageView) view.findViewById(R.id.n_s1);
        ImageView i2=(ImageView) view.findViewById(R.id.n_s2);

        nameView.setText((String)mMap.get(position).get("name"));
        contentView.setText((String)mMap.get(position).get("message"));
        timeView.setText(dts);

        r=(int)(Math.random() *176+0);
        g=(int)(Math.random() *166+0);
        b=(int)(Math.random() *25+200);
        color=Color.argb(255,r,g,b);

        i1.setBackgroundColor(color);
        i2.setBackgroundColor(color);

        return view;
    }

    /**
     * Clear out the adapter.
     */
    public void clear() {
       mMap.clear();
    }

    public static String getTimeSinceString(Context context, long timeNanoseconds) {
        String lastSeenText = context.getResources().getString(R.string.last_seen) + " ";

        long timeSince = SystemClock.elapsedRealtimeNanos() - timeNanoseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.NANOSECONDS);

        if (secondsSince < 5) {
            lastSeenText += context.getResources().getString(R.string.just_now);
        } else if (secondsSince < 60) {
            lastSeenText += secondsSince + " " + context.getResources()
                    .getString(R.string.seconds_ago);
        } else {
            long minutesSince = TimeUnit.MINUTES.convert(secondsSince, TimeUnit.SECONDS);
            if (minutesSince < 60) {
                if (minutesSince == 1) {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minute_ago);
                } else {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minutes_ago);
                }
            } else {
                long hoursSince = TimeUnit.HOURS.convert(minutesSince, TimeUnit.MINUTES);
                if (hoursSince == 1) {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hour_ago);
                } else {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hours_ago);
                }
            }
        }

        return lastSeenText;
    }

}
