package com.example.deer.boochat.tab_fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.MainActivity;
import com.example.deer.boochat.R;
import com.example.deer.boochat.adapter.DeviceTableAdapter;
import com.example.deer.boochat.adapter.MsgItemAdapter;
import com.example.deer.boochat.advertise.AddFriendsActivity;
import com.example.deer.boochat.chat_room.BluetoothChatActivity;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;
import com.example.deer.boochat.for_mDB.ListItem;

import java.util.List;

/**
 * Created by deer on 2015/8/20.
 */
public class FriendFragment extends Fragment {

    private SearchView searchView;
    private ListView mListView;
    private ListDBItemDAO mDBManager;
    private  AlertDialog.Builder builder;
    private  AlertDialog alertDialog;
    DeviceTableAdapter mDeviceName;

    public static FriendFragment newInstance()
    {
        FriendFragment fragment = new FriendFragment();
        return fragment;
    }

    public void setListDBItemDAO(ListDBItemDAO mdb) {
        this.mDBManager = mdb;
    }
    public void setDeviceTableAdapter(DeviceTableAdapter mdta) {
        this.mDeviceName = mdta;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mDBManager.sample();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_friend, container, false);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        //searchView=(SearchView)view.findViewById(R.id.searchView);
        builder = new AlertDialog.Builder (view.getContext());
        mListView=(ListView)view.findViewById(R.id.name_listView);
        mListView.setAdapter(mDeviceName);
        //mListView.setFocusable(true);
        //mListView.setFocusableInTouchMode(true);
        mListView.setOnItemClickListener(new onItemClickListener());
        mListView.setOnItemLongClickListener(new onItemLongClickListener());

    }
    @Override
    public void onResume() {
        super.onResume();
        mListView.setEnabled(true);
    }
    @Override
    public void onStart()
    {
        super.onStart();
        //updateFriend();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mDBManager.close();
    }

    public class onItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            mListView.setEnabled(false);
            //mDBManager.setCreateTable(mDeviceName.getSerial(position));
            Intent intent=new Intent(getActivity(), BluetoothChatActivity.class);
            Bundle bundle_p = new Bundle();
            bundle_p.putLong(Constants.DEVICE_ID,mDeviceName.getID(position));
            bundle_p.putString(Constants.DEVICE_NAME, mDeviceName.findByPosition(position));
            bundle_p.putString(Constants.DEVICE_SERIAL, mDeviceName.getSerial(position));
            intent.putExtras(bundle_p);
            startActivity(intent);
        }
    }
    public class onItemLongClickListener implements AdapterView.OnItemLongClickListener
    {
        private long itemId;
        private String name;
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id)
        {
            // TODO Auto-generated method stub
            itemId=mDeviceName.getID(pos);
            name=mDeviceName.getSerial(pos);
            if(itemId<0);
            else
            {
                Toast.makeText(getActivity(), Long.toString(itemId), Toast.LENGTH_SHORT).show();
                CharSequence yes="Yes";
                CharSequence no="No";
                alertDialog=builder.setTitle("Delete Friend "+ mDeviceName.findByPosition(pos))
                        .setMessage("Do you want to remove him / her ?")
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDBManager.delete(itemId);
                                mDBManager.dropTable(name);
                                if(mDeviceName!=null)
                                mDeviceName.cleanTemp();
                                updateFriend();
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


    public void updateFriend()
    {
        List<ListItem> mList=mDBManager.getAll();
        if(mList!=null){
        for(int i=0;i<mList.size();i++) {
            mDeviceName.addinMap(mList.get(i).getSerial(), mList.get(i).getDeviceName(),mList.get(i).getId());
        }}
        mDeviceName.notifyDataSetChanged();
    }

}
