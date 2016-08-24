package com.example.deer.boochat;

        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothManager;
        import android.content.BroadcastReceiver;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.ServiceConnection;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.HandlerThread;
        import android.os.IBinder;
        import android.support.design.widget.CollapsingToolbarLayout;
        import android.support.design.widget.Snackbar;
        import android.support.design.widget.TabLayout;
        import android.support.v4.app.Fragment;
        import android.support.v4.view.GravityCompat;
        import android.support.v4.view.ViewCompat;
        import android.support.v4.view.ViewPager;
        import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.example.deer.boochat.adapter.DeviceTableAdapter;
        import com.example.deer.boochat.adapter.MsgItemAdapter;
        import com.example.deer.boochat.adapter.PagerAdapter;
        import com.example.deer.boochat.advertise.AddFriendsActivity;
        import com.example.deer.boochat.chat_room.MessageReader;
        import com.example.deer.boochat.for_mDB.ListDBItemDAO;
        import com.example.deer.boochat.for_mDB.ListItem;
        import com.example.deer.boochat.for_mDB.ListTMPItemDAO;
        import com.example.deer.boochat.service.AdvertiserService;
        import com.example.deer.boochat.service.BluetoothLeService;
        import com.example.deer.boochat.service.GattService;
        import com.example.deer.boochat.service.ScannerService;
        import com.example.deer.boochat.service.SequenceNumber;
        import com.example.deer.boochat.tab_fragment.ChatFragment;
        import com.example.deer.boochat.tab_fragment.FriendFragment;
        import com.example.deer.boochat.tab_fragment.PublicFragment;
        import com.example.deer.boochat.tab_fragment.SettingFragment;

        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private AdvertiserService mAdvertiserService;
    private ScannerService mScannerService;
    private UpdateReceiver myUpdateReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeService mBluetoothLeService;
    private MessageReader mReader;
    private DeviceTableAdapter mDeviceName;
    private MsgItemAdapter mItemAdapter;
    private ListDBItemDAO mDBManager;
    private ListTMPItemDAO mTMPManager;
    private SequenceNumber mSequenceNumber;
    private PagerAdapter mPagerAdapter;
    private FriendFragment fdFragment;
    private ChatFragment ctFragment;
    private PublicFragment pbFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mLvLeftMenu;
    private GattService chatService;
    private AlertDialog.Builder mBuilder;
    private AlertDialog alertDialog;
    private TabLayout tabLayout;
    private CollapsingToolbarLayout colToolbar;
    private ViewPager viewPager;
    //private SlidingMenu mMenu ;
    private Bundle checkBind;
    private Boolean check=false;
    public List<Fragment> list;
    public Toolbar toolbar;
    public Intent intent;

    private Handler mHandler; //雜事執行緒(mTread)的小幫手
    private HandlerThread mThread;
    /*基本活動生命週期行為設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null ) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            setupFragments();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myUpdateReceiver, makeUpdateIntentFilter());
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(myUpdateReceiver!=null)
            unregisterReceiver(myUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unbindService(mScanServiceConnection);
        mAdvertiserService = null;
        mScannerService = null;
        mDBManager.close();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*畫面設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    public void setupFragments()
    {

        mSequenceNumber= new SequenceNumber();
        /*開啟或創建資料庫*/
        mDBManager=new ListDBItemDAO(MainActivity.this);
        mTMPManager=new ListTMPItemDAO(MainActivity.this);
        if(mDBManager.getCount()==0)
        {
            ListItem item = new ListItem(0, new Date().getTime(), mBluetoothAdapter.getName(), Constants.BuildSERIAL,0,0);
            mDBManager.insert(item);
            mDBManager.setCreateTable(Constants.BuildSERIAL);
            mDBManager.sample();
            mTMPManager.setCreateTXTTable(Constants.AD_MSG);
            mTMPManager.setCreateMSGTable(Constants.AD_TABLE, Constants.AD_MSG);
            mTMPManager.setCreateTXTTable(Constants.SC_MSG);
            mTMPManager.setCreateMSGTable(Constants.SC_TABLE, Constants.SC_MSG);
        }
        //mTMPManager.dropTable(Constants.AD_TABLE);
        //mTMPManager.dropTable(Constants.AD_MSG);
        mTMPManager.setCreateTXTTable(Constants.AD_MSG);
        mTMPManager.setCreateMSGTable(Constants.AD_TABLE, Constants.AD_MSG);


        //Toast.makeText(this, Integer.toString(mDBManager.getCount()), Toast.LENGTH_SHORT).show();

        list = new ArrayList<Fragment>();
        list.add(new SettingFragment());
        list.add(new FriendFragment());
        list.add(new ChatFragment());
        list.add(new PublicFragment());


        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mPagerAdapter=new PagerAdapter(getSupportFragmentManager(),list);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setFocusable(true);
        viewPager.setAdapter(mPagerAdapter);


        //setListDBItemDAO
        mDeviceName=new DeviceTableAdapter(getApplicationContext(),getLayoutInflater());
        fdFragment=(FriendFragment)mPagerAdapter.getItem(1);
        fdFragment.setListDBItemDAO(mDBManager);
        fdFragment.setDeviceTableAdapter(mDeviceName);
        ctFragment=(ChatFragment)mPagerAdapter.getItem(2);
        ctFragment.setListDBItemDAO(mDBManager);
        pbFragment=(PublicFragment)mPagerAdapter.getItem(3);


        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        if (ViewCompat.isLaidOut(tabLayout)) {
            tabLayout.setupWithViewPager(viewPager);
        } else {
            tabLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    tabLayout.setupWithViewPager(viewPager);

                    tabLayout.removeOnLayoutChangeListener(this);
                }
            });
        }
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        //toolbar.setTitle("Boochat");
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        colToolbar=(CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        colToolbar.setTitle("Boochat");
        //final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(android.R.drawable.stat_notify_chat);
        //ab.setDisplayHomeAsUpEnabled(true);
        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        mItemAdapter = new MsgItemAdapter(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        mLvLeftMenu = (ListView) findViewById(R.id.id_lv_left_menu);
        setUpDrawer();
        //mMenu = (SlidingMenu) findViewById(R.id.expanded_menu);

        Intent adServiceIntent = new Intent(this, AdvertiserService.class);
        bindService(adServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Intent scServiceIntent = new Intent(this, ScannerService.class);
        startService(scServiceIntent);
        bindService(scServiceIntent, mScanServiceConnection, BIND_AUTO_CREATE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mConnServiceConnection, BIND_AUTO_CREATE);
        myUpdateReceiver = new UpdateReceiver();

        fdFragment.updateFriend();
        ctFragment.BaseMsgId(mItemAdapter);
        pbFragment.setAdvertiser(mAdvertiserService);
        pbFragment.setScanner(mScannerService);
        chatService=new GattService(mBluetoothManager,this);
    }
    private void setUpDrawer()
    {
        mBuilder=new AlertDialog.Builder(this);
        //LayoutInflater inflater = LayoutInflater.from(this);
        //mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.header_just_username, mLvLeftMenu, false));
        mLvLeftMenu.setAdapter(mItemAdapter);
        mItemAdapter.add((long)0,(long)0,"","",0,"",0,0,0,1);
        mLvLeftMenu.setOnItemClickListener(new onItemClickListener());
    }
    public class onItemClickListener implements AdapterView.OnItemClickListener
    {
        int pos;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            pos=position;
            if (pos==0);
            else
            {
                CharSequence yes = "Yes";
                CharSequence no = "No";
                alertDialog = mBuilder.setTitle("This message is not sending!")
                        .setMessage("Do you want to remove this message ?")
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mItemAdapter.remove(pos);
                                mItemAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        }).show();
            }
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.home:
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    mItemAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this,String.valueOf(mItemAdapter.getCount()), Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_edit:
                    msg += "Click add";
                    intent=new Intent(MainActivity.this, AddFriendsActivity.class);
                    startActivityForResult(intent,0);
                    break;
            }

            if(!msg.equals("")) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*取得活動轉換的回報*/
    @Override /*與fragment的溝通*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.RESULT_FROM_P0101:
                if (resultCode == RESULT_OK)
                    fdFragment.updateFriend();
                else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "no",
                            Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(android.R.id.content), "Adding friend is failed!", Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            case Constants.STOP_CATTING:
                if(resultCode==RESULT_OK)
                    ctFragment.updateChat();
                    break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*連接服務*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mAdvertiserService = ((AdvertiserService.LocalBinder) service).getService();
            mAdvertiserService.setBluetoothAdapter(mBluetoothAdapter);
            mAdvertiserService.setMsgItemAdapter(mItemAdapter);
            mAdvertiserService.setSequenceNumber(mSequenceNumber);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mAdvertiserService = null;
        }
    };
    private final ServiceConnection mScanServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mScannerService = ((ScannerService.LocalBinder) service).getService();
            mScannerService.setBluetoothAdapter(mBluetoothAdapter);
            mScannerService.setGattService(chatService);
            mScannerService.setMsgItemAdapter(mItemAdapter);
            mScannerService.setSequenceNumber(mSequenceNumber);
            mScannerService.restartScanning();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mScannerService = null;
        }
    };
    private final ServiceConnection mConnServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*內部資訊溝通*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*取得內部廣播 (來自服務的狀況回報)*/
    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle b=intent.getExtras();
            if (Constants.ADVERTISER_SERVICE_CHANGE.equals(action)) {
            }
            else if(Constants.SCANNER_SERVICE_CHANGE.equals(action)) {

                if (b.getInt(Constants.OPCODE) == 1 ) {

                    if(b.getInt(Constants.PRIORITY)==2)
                        setAlert(b.getString(Constants.SENDER),b);
                    else
                    {
                         fdFragment.updateFriend();
                        onDeviceSelected(b);
                    }
                }
                else
                    onDeviceSelected(b);

            }
            else if(Constants.IOS_CONNECT_REQUEST.equals(action))
            {
                Snackbar.make(findViewById(android.R.id.content), "Connect to ..." + b.getString(Constants.DEVICE_NAME), Snackbar.LENGTH_SHORT)
                        .show();
                onIosSelected(b);
            }
            else if(Constants.IOS_DETECT_REQUEST.equals(action))
            {
                Snackbar.make(findViewById(android.R.id.content),  b.getString(Constants.DEVICE_NAME)+"  is searching", Snackbar.LENGTH_SHORT)
                        .show();
                onIosSelected(b);
            }
            else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Snackbar.make(findViewById(android.R.id.content), "connected", Snackbar.LENGTH_SHORT)
                        .show();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Snackbar.make(findViewById(android.R.id.content), "disconnected", Snackbar.LENGTH_SHORT)
                        .show();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Snackbar.make(findViewById(android.R.id.content), "found", Snackbar.LENGTH_SHORT)
                        .show();
                mReader=new MessageReader(mBluetoothLeService);
                mReader.setUpIOS();
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                Snackbar.make(findViewById(android.R.id.content),"ok", Snackbar.LENGTH_SHORT)
                        .show();
                pbFragment.renew("test",intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
            else if(Constants.ADVERTISER_ITEM_ADD.equals(action)) {
                Snackbar.make(findViewById(android.R.id.content),"get", Snackbar.LENGTH_SHORT)
                        .show();
                pbFragment.renew("test", intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }

        }
    }
    /*廣播action濾波器*/
    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ADVERTISER_SERVICE_CHANGE);
        intentFilter.addAction(Constants.SCANNER_SERVICE_CHANGE);
        intentFilter.addAction(Constants.IOS_CONNECT_REQUEST);
        intentFilter.addAction(Constants.IOS_DETECT_REQUEST);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ADVERTISER_ITEM_ADD);
        return intentFilter;
    }

    /*加朋友意願視窗*/
    public void setAlert(String n,Bundle b)
    {
        checkBind=b;
        CharSequence yes="Yes";
        CharSequence no="No";
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        alertDialog=builder.setTitle("New Friend! "+n)
                .setMessage("Do you know him / her ?")
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onDeviceSelected(checkBind);
                                check=true;
                            }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                                check=false;
                            }
                })
                .show ();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*前往廣播服務*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*ios類型使用*/
    public void onIosSelected(Bundle bundle) {
        int flag = bundle.getInt(Constants.FRAGMENT_CHANGE);
        switch(flag) {
            case 2:
                bundle.putInt(Constants.SENDING,Constants.FROM_IOS);
                mAdvertiserService.hub(bundle);
                break;
            case 4:
                mBluetoothLeService.connect(bundle.getString(Constants.DEVICE_ADDRESS),0);
                break;
        }
    }

    //scannerFragment 使用之傳遞訊息(裝置位址、名稱)
    public void onDeviceSelected(Bundle bundle) {
        int flag = bundle.getInt(Constants.FRAGMENT_CHANGE);
        switch(flag) {
            case 2:
                bundle.putInt(Constants.SENDING,3);
                mAdvertiserService.hub(bundle);
                break;
            /*case 4:
                if(!mDBManager.FriendFilter(bundle.getString(Constants.ADVERTISER_DATA)))
                    break;
                mBluetoothLeService.connect(bundle.getString(Constants.DEVICE_ADDRESS));
                break;*/
        }
    }

    /**/
    public void test()
    {
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.SENDING,3);
        mAdvertiserService.hub(bundle);
        //mAdvertiserService.testAD();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
}
