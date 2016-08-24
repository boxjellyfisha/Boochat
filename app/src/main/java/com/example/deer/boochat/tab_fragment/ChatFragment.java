package com.example.deer.boochat.tab_fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.adapter.MsgItemAdapter;
import com.example.deer.boochat.adapter.NoticeAdapter;
import com.example.deer.boochat.chat_room.BluetoothChatActivity;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;
import com.example.deer.boochat.for_mDB.ListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by deer on 2015/8/20.
 */
public class ChatFragment extends ListFragment {
    private ListDBItemDAO mDBManager;
    private NoticeAdapter mNotice;
    private List<Map<String,Object>> mList;

    public static ChatFragment newInstance()
    {
        ChatFragment f=new ChatFragment();
        return f;
    }

    public void setListDBItemDAO(ListDBItemDAO mdb) {
        mDBManager = mdb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mNotice = new NoticeAdapter(LayoutInflater.from(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setListAdapter(mNotice);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        //getListView().setDividerHeight(10);
        setEmptyText(getString(R.string.empty_list));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //updateChat();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        Toast.makeText(getActivity(), mNotice.getName(position)+"\n"+mNotice.getSerial(position), Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(getActivity(), BluetoothChatActivity.class);
        Bundle bundle_p = new Bundle();
        bundle_p.putString(Constants.DEVICE_NAME, mNotice.getName(position));
        bundle_p.putString(Constants.DEVICE_SERIAL, mNotice.getSerial(position));
        intent.putExtras(bundle_p);
        mNotice.clear();
        getActivity().startActivityForResult(intent, Constants.STOP_CATTING);
    }

    public void updateChat()
    {
        if(mNotice!=null)
            mNotice.clear();
        mList=mDBManager.getLatest();
        for(int i=0; i<mList.size();i++)
        {
            ListItem list=(ListItem)mList.get(i).get("result");
            mNotice.add(list.getDatetime(),
                    list.getContent(),
                    (String) mList.get(i).get("name"),
                    (String) mList.get(i).get("serial"));
        }
        mList.clear();
        mNotice.sort();
        mNotice.notifyDataSetChanged();
    }
    public void BaseMsgId(MsgItemAdapter itemAdapter)
    {
        mList=mDBManager.getLatest();
        for(int i=0; i<mList.size();i++)
        {
        ListItem list=(ListItem)mList.get(i).get("result");
        itemAdapter.RecordMsgId((String)mList.get(i).get("serial"),list.getId());
        }
    }
}
