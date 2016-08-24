/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.deer.boochat.chat_room;


import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.adapter.ConversationAdapter;
import com.example.deer.boochat.adapter.StickerAdapter;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;
import com.example.deer.boochat.for_mDB.ListItem;

import java.util.Date;
import java.util.List;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_ADVERTISE_DATA = "ADVERTISE_DATA";
    public static final String EXTRAS_PACKAGE_DATA = "PACKAGE_DATA";

    // Layout Views
    private PopupWindow popupWindow;
    private ListView mConversationView;
    private EditText mOutEditText;
    private ImageView mIcon;
    private Button mSendButton;
    private Button mSticker;
    private AlertDialog alertDialog;
    private  AlertDialog.Builder builder;
    long   mTime;
    long   mDeviceId;
    int   mDeviceNUM;
    String mDeviceName;
    String mDeviceAddress;
    String mData;
    String mPackage;
    Boolean Send_btn=false;  //表示現在的發送狀態
    private BluetoothChatActivity mCallback;
    private ConversationAdapter mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter;
    private ListDBItemDAO mDBManager;
    private StickerAdapter mStickerAdapter;
    private GridView mGridView;   //MyGridView
        //定義圖標數組
        private int[] imageRes = { R.drawable.addfriend };/*, R.drawable.blel3,
        R.drawable.blel5, R.drawable.blel7, R.drawable.blel9, R.drawable.blel10, R.drawable.blel11,
        R.drawable.blel13, R.drawable.blel15, R.drawable.blel17,
        R.drawable.blel19, R.drawable.blel20*/

        /*public void setListDBItemDAO(ListDBItemDAO mdb) {
            this.mDBManager = mdb;
        }*/

    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        mBluetoothAdapter = btAdapter;
    }

    /*一般生命週期設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCallback=(BluetoothChatActivity)getActivity();
        mDBManager=new ListDBItemDAO(getContext());
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null)
        {
            mDeviceId = bundle.getLong(Constants.DEVICE_ID);
            mDeviceName = bundle.getString(Constants.DEVICE_NAME);
            mDeviceAddress = bundle.getString(Constants.DEVICE_SERIAL);
        }
        mStickerAdapter = new StickerAdapter(LayoutInflater.from(getActivity()));
        mConversationArrayAdapter = new ConversationAdapter(LayoutInflater.from(getActivity()));
        if(mDBManager.getCountByName(mDeviceAddress)==0){
            ListItem i=new ListItem(mDeviceId,new Date().getTime(),mDeviceName, mDeviceAddress,0,1);
            mDBManager.update(i);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
            setupChat();
        if(mConversationArrayAdapter!=null)
        mConversationArrayAdapter.clear();
        if(mDBManager.getCountByName(mDeviceAddress)!=0)
            updateConversation();
        Toast.makeText(getActivity(),Integer.toString(mDBManager.getCountByName(mDeviceAddress)), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Override
    public void onResume() {
        super.onResume();
        //mSendButton.setEnabled(Send_btn);
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.

    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_public, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mConversationView.setOnItemLongClickListener(new onItemLongClickListener());
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mSticker = (Button) view.findViewById(R.id.sticker);
        builder = new AlertDialog.Builder (view.getContext());
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*設置畫面*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*基礎畫面設置*/
    private void setupChat() {

        mConversationArrayAdapter.setDeviceName(mDeviceName);
        // Initialize the array adapter for the conversation thread
        mConversationView.setAdapter(mConversationArrayAdapter);
        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);
        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    long time=new Date().getTime();
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    //加入聊天紀錄資料庫中
                    ListItem i=new ListItem(Constants.BuildSERIAL,new Date().getTime(),message,0);
                    long msgid=mDBManager.record(i,mDeviceAddress).getId();
                    //打包訊息 之後要存進暫存佇列
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.SENDING,Constants.FROM_CHATROOM);
                    bundle.putString(BluetoothChatFragment.EXTRAS_ADVERTISE_DATA, message);
                    bundle.putLong(Constants.TIME, time);
                    bundle.putLong(Constants.MSGID, msgid);
                    bundle.putString(Constants.SENDER, Constants.BuildSERIAL);
                    bundle.putString(Constants.RECEIVER, mDeviceAddress);
                    bundle.putInt(Constants.OPCODE,6);
                    bundle.putString(Constants.CONTENT,message);
                    bundle.putInt(Constants.PRIORITY,0);
                    bundle.putInt(Constants.LOCALNUM,mDeviceNUM);
                    bundle.putInt(Constants.OSTYPE,0);
                    bundle.putInt(Constants.LYAOUTTYPE,0);
                    mCallback.onSendMessage(bundle);
                    //對話框上顯示
                    mConversationArrayAdapter.add(0,time,message, 0,0);
                    mConversationArrayAdapter.notifyDataSetChanged();
                    //設為不可發送 並清除訊息文字編輯區
                    Send_btn=false;
                    //mSendButton.setEnabled(Send_btn);
                    mOutStringBuffer.setLength(0);
                    mOutEditText.setText(mOutStringBuffer);

                }
            }
        });
        mSticker.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                View v=getView();
                if(v!=null)
                {
                    initPopupWindowView();
                    showUp(v);
                    Toast.makeText(getActivity(), "click",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    /*訊息選項長按監聽*/
    public class onItemLongClickListener implements AdapterView.OnItemLongClickListener
    {
        private long itemId;
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id)
        {
            // TODO Auto-generated method stub
            mConversationArrayAdapter.clear();
            updateConversation();
            itemId=mConversationArrayAdapter.getID(pos);
            if(itemId<0);
            else
            {
                Toast.makeText(getActivity(), Long.toString(itemId), Toast.LENGTH_SHORT).show();
                CharSequence yes="Yes";
                CharSequence no="No";
                alertDialog=builder.setTitle("Delete")
                        .setMessage("Do you want to remove this message ?")
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDBManager.delete(itemId,mDeviceAddress);
                                if(mConversationArrayAdapter!=null)
                                    mConversationArrayAdapter.clear();
                                updateConversation();
                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        })
                        .show ();
            }
            return true;
        }
    }
    /*文字編輯監聽*/
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (Send_btn && actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                long time=new Date().getTime();
                String message = view.getText().toString();
                //加入資料庫中
                ListItem i=new ListItem(Constants.BuildSERIAL,new Date().getTime(),message,0);
                long msgid=mDBManager.record(i,mDeviceAddress).getId();
                //打包訊息 之後要存進暫存佇列
                Bundle bundle=new Bundle();
                bundle.putInt(Constants.SENDING,Constants.FROM_CHATROOM);
                bundle.putString(EXTRAS_ADVERTISE_DATA, message);
                bundle.putLong(Constants.TIME, time);
                bundle.putLong(Constants.MSGID,msgid);
                bundle.putString(Constants.SENDER, Constants.BuildSERIAL);
                bundle.putString(Constants.RECEIVER, mDeviceAddress);
                bundle.putInt(Constants.OPCODE,6);
                bundle.putString(Constants.CONTENT,message);
                bundle.putInt(Constants.PRIORITY,0);
                bundle.putInt(Constants.LOCALNUM,mDeviceNUM);
                bundle.putInt(Constants.OSTYPE,0);
                bundle.putInt(Constants.LYAOUTTYPE, 0);
                mCallback.onSendMessage(bundle);
                //對話框上顯示
                mConversationArrayAdapter.add(0,new Date().getTime(),message,0,0);
                mConversationArrayAdapter.notifyDataSetChanged();
                //設定為不可發送 清除訊息文字編輯區
                Send_btn=false;
                //mSendButton.setEnabled(Send_btn);
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);

            }
            return true;
        }
    };

    /*popwindow初始化設置----貼圖選擇畫面*/
    public void initPopupWindowView()
    {

        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_sticker, null, false);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mStickerAdapter.add(imageRes[0]);
        mGridView = (GridView) popupView.findViewById(R.id.gridView);
        mIcon=(ImageView) popupView.findViewById(R.id.test_icon);
        mIcon.setImageResource(R.drawable.addfriend);
        mGridView.setAdapter(mStickerAdapter);
        //为mGridView添加點擊事件監聽器
        mGridView.setOnItemClickListener(new GridViewItemOnClick());

    }
    /*貼圖按件監聽*/
    public class GridViewItemOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
            mConversationArrayAdapter.add(0,new Date().getTime(),"((@+stiker:",3,0);
            mConversationArrayAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "pic",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /*popwindow出現動畫*/
    public void showUp(View parent) {
        popupWindow.setAnimationStyle(R.style.AnimationPopup);
        popupWindow.showAtLocation(parent, Gravity.LEFT |Gravity.BOTTOM,0,400);
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/


    /*更新訊息*/

    public void updateChatView(Bundle bundle)
    {
        if (bundle != null)
        {

            if(bundle.getInt(Constants.OP_CODE)==2)
            mData = bundle.getString(Constants.ADVERTISER_DATA);
            mTime = bundle.getLong(Constants.ACCEPT_TIME);
            Toast.makeText(getActivity(), "顯示: " + mData, Toast.LENGTH_LONG).show();
            //加入資料庫中
            //ListItem i=new ListItem(mDeviceAddress,mTime,mData);
            //mDBManager.record(i, mDeviceAddress);

            mConversationArrayAdapter.add(0,mTime, mData, 1,1);
            mConversationArrayAdapter.notifyDataSetChanged();
        }
    }
    public void updateConversation()
    {
        int flag;
        List<ListItem> mList=mDBManager.getAll(mDeviceAddress);
        if(mList!=null){
            for(int i=0;i<mList.size();i++) {
                if(mList.get(i).getSerial().equals(Constants.BuildSERIAL))
                   flag=0;
                else
                    flag=1;
                mConversationArrayAdapter.add(mList.get(i).getId(),mList.get(i).getDatetime(),mList.get(i).getContent(), flag,mList.get(i).getIsAlready());

            }}
        mConversationArrayAdapter.notifyDataSetChanged();
    }


}
