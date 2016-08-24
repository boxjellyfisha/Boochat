package com.example.deer.boochat.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deer on 2015/12/11.
 */
public class MsgItemAdapter extends BaseAdapter{


        private LayoutInflater mInflater;
        private Context mContext;
        private long time;
        public List<Map<String,Object>> mItems= new ArrayList<Map<String,Object>>();
        public List<Bundle> mReceivedItems= new ArrayList<>();
        private Map<String,Long> BaseMsgId= new HashMap<>();


        public long findMsgId(String name, int id)
            {
                long now=new Date().getTime();
                if(now>time)
                    return -1;
                return (BaseMsgId.get(name)+id);
            }

        public void RecordMsgId(String name, long id)
        {
            time=new Date().getTime();
            BaseMsgId.put(name,id);
        }
        public int getIndexMsgId(String name, long id)
        {
            long tmpID=BaseMsgId.get(name);
            int offset=(int)(id-tmpID);
            if(offset>127) {
                time=new Date().getTime();
                BaseMsgId.remove(name);
                BaseMsgId.put(name,id);
                return 0;
            }
            return offset;

        }
        public boolean addfriendcopare(int localnum,int receiver)
        {
            int m3=(int)mItems.get(1).get(Constants.LOCALNUM);
            int m4=(int)mItems.get(1).get(Constants.MSGID);
            if(localnum==m4 && receiver==m3)
            return true;
            return false;
        }
        public boolean compareMsgId(String name, int id)
        {
            long in=findMsgId(name, id);
            long tmp=(long)mItems.get(1).get(Constants.MSGID);

            if(in!=-1 && tmp==in)
                return true;
            return false;
        }
        public MsgItemAdapter(Context context)
        {
            mInflater = LayoutInflater.from(context);
            mContext = context;
        }

        public void add(Bundle bundle) {
            long dt=bundle.getLong(Constants.TIME);
            long m=bundle.getLong(Constants.MSGID);
            String s=bundle.getString(Constants.SENDER);
            String r=bundle.getString(Constants.RECEIVER);
            int op=bundle.getInt(Constants.OPCODE);
            String c=bundle.getString(Constants.CONTENT);
            int p=bundle.getInt(Constants.PRIORITY);
            int l=bundle.getInt(Constants.LOCALNUM);
            int o=bundle.getInt(Constants.OSTYPE);
            int t=bundle.getInt(Constants.LYAOUTTYPE);

            Map<String,Object> map=new HashMap<>();
            map.put(Constants.TIME,dt);  //寫下訊息時間
            map.put(Constants.MSGID,m); //寄件人寫給該收件人的第幾封信
            map.put(Constants.SENDER,s);  //寄件人
            map.put(Constants.RECEIVER,r); //收件人
            map.put(Constants.OPCODE,op); //此訊息類型
            map.put(Constants.CONTENT,c); //此訊息的主要內容
            map.put(Constants.PRIORITY,p); //此訊息的優先等級
            map.put(Constants.LOCALNUM,l); //寄件人的區域代號
            map.put(Constants.OSTYPE,o);   //此訊息屬於哪種裝置
            map.put(Constants.LYAOUTTYPE, t); //此訊息的排版類型


            mItems.add(map);
            //map.clear();

        }
    public void addR(Bundle bundle) {
        //接收到03封包的部分
        mReceivedItems.add(bundle);

    }
        public void add(long dt,long m,String s,String r,int op,String c,int p,int l,int o,int t)
        {
            Map<String,Object> map=new HashMap<>();
            map.put(Constants.TIME,dt);  //寫下訊息時間
            map.put(Constants.MSGID,m); //寄件人寫給該收件人的第幾封信
            map.put(Constants.SENDER,s);  //寄件人
            map.put(Constants.RECEIVER,r); //收件人
            map.put(Constants.OPCODE,op); //此訊息類型
            map.put(Constants.CONTENT,c); //此訊息的主要內容
            map.put(Constants.PRIORITY,p); //此訊息的優先等級
            map.put(Constants.LOCALNUM,l); //寄件人的區域代號
            map.put(Constants.OSTYPE,o);   //此訊息屬於哪種裝置
            map.put(Constants.LYAOUTTYPE, t); //此訊息的排版類型

            mItems.add(map);
            //map.clear();

        }

        public void remove(int position)
        {
            mItems.remove(position);
        }


        public Map<String,Object> getItemFirst()
    {
        return mItems.get(1);
    }
        public Object getTimeFirst()
    {
        return mItems.get(1).get("time");
    }

    @Override
        public int getCount()
        {
            return mItems.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public int getViewTypeCount()
        {
            return 3;
        }

        @Override
        public int getItemViewType(int position)
        {
            return (int)mItems.get(position).get(Constants.LYAOUTTYPE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            switch (getItemViewType(position))
            {
                case 0:
                    if (convertView==null)
                    {
                        convertView = mInflater.inflate(R.layout.tmp_msg2, parent,
                                false);
                    }

                    TextView itemView = (TextView) convertView.findViewById(R.id.textSender);
                    TextView itemView2 = (TextView) convertView.findViewById(R.id.textReceiver);
                    TextView itemView3 = (TextView) convertView.findViewById(R.id.textContent);

                    if(mItems.get(1).equals(mItems.get(position)))
                    {
                        /**ImageView img=(ImageView)convertView.findViewById(R.id.glidetest);
                        Glide.with(mContext)
                                .load(R.drawable.an)
                                .asGif()
                                .into(img);**/
                    }
                    itemView.setText((String)mItems.get(position).get("sender"));
                    itemView2.setText((String)mItems.get(position).get("receiver"));
                    itemView3.setText((String)mItems.get(position).get("content"));
                    break;
                case 1:

                        convertView = mInflater.inflate(R.layout.layout_menu, parent,
                                false);

                    break;
                case 2:
                    if (convertView==null)
                    {
                        convertView = mInflater.inflate(R.layout.tmp_msg, parent,
                                false);
                    }

                    TextView itemView4 = (TextView) convertView.findViewById(R.id.textSender);
                    itemView4.setText((String)mItems.get(position).get(Constants.SENDER));
                    break;

            }

            return convertView;
        }

        public void setIconColor(Drawable icon)
        {
            int textColorSecondary = android.R.attr.textColorSecondary;
            TypedValue value = new TypedValue();
            if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true))
            {
                return;
            }
            int baseColor = mContext.getResources().getColor(value.resourceId, null);
            icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
        }
    }

