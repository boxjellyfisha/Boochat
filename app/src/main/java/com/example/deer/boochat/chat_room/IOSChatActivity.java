package com.example.deer.boochat.chat_room;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;
import com.example.deer.boochat.service.AdvertiserService;
import com.example.deer.boochat.service.BluetoothLeService;
import com.example.deer.boochat.service.GattService;
import com.example.deer.boochat.service.ScannerService;

/**
 * Created by deer on 2016/3/19.
 */
public class IOSChatActivity  extends AppCompatActivity {
    private final static String TAG = BluetoothChatActivity.class.getSimpleName();
    private AdvertiserService mAdvertiserService;
    private ScannerService mScannerService;
    private BluetoothLeService mBluetoothLeService;
    private UpdateReceiver myUpdateReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private MessageReader mReader;
    private GattService chatService;
    private ListDBItemDAO mDBManager;
    //private ListTMPItemDAO mTMPManager;
    private BluetoothChatFragment mFragment;
    private String deviceSerial;
    private String tmpSerial;
    private Boolean check=false;
    public Toolbar toolbar;
    private int chose=0;//選擇要讀哪個資料

    /*一般生命週期設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        setContentView(R.layout.chat_room);
        mDBManager=new ListDBItemDAO(IOSChatActivity.this);
        //mTMPManager=new ListTMPItemDAO(BluetoothChatActivity.this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        BluetoothChatFragment chatFragment = new BluetoothChatFragment();
        chatFragment.setBluetoothAdapter(mBluetoothAdapter);
        //chatFragment.setListDBItemDAO(mDBManager);
        transaction.replace(R.id.chat_fragment_container, chatFragment);
        transaction.commit();

        mFragment = (BluetoothChatFragment)
                getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);

        String name=getIntent().getExtras().getString(Constants.DEVICE_NAME);
        deviceSerial=getIntent().getExtras().getString(Constants.DEVICE_SERIAL);

        toolbar = (Toolbar) findViewById(R.id.chat_room_toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        Intent adServiceIntent = new Intent(this, AdvertiserService.class);
        bindService(adServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Intent scServiceIntent = new Intent(this, ScannerService.class);
        bindService(scServiceIntent, mScanServiceConnection, BIND_AUTO_CREATE);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mConnServiceConnection, BIND_AUTO_CREATE);
        myUpdateReceiver = new UpdateReceiver();

        chatService=new GattService(mBluetoothManager,this);
        if(!chatService.existence(deviceSerial))
            chatService.setChatData(deviceSerial);

    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myUpdateReceiver, makeUpdateIntentFilter());
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myUpdateReceiver);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unbindService(mScanServiceConnection);
        unbindService(mConnServiceConnection);
        //mAdvertiserService = null;
        //mScannerService = null;
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
        super.onBackPressed();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*連接服務*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mAdvertiserService = ((AdvertiserService.LocalBinder) service).getService();
            mAdvertiserService.setBluetoothAdapter(mBluetoothAdapter);
            mAdvertiserService.updateToWhom(deviceSerial);
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

    /*內部廣播*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*與服務溝通*/
    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle b=intent.getExtras();

            /*if (Constants.ADVERTISER_SERVICE_CHANGE.equals(action)) {}*/
            if(Constants.SCANNER_SERVICE_CHANGE.equals(action)) {

                onDeviceSelected(b);
            }
            else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(IOSChatActivity.this, "connected", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(IOSChatActivity.this,"disconnected", Toast.LENGTH_SHORT).show();
                //need check characteristic whether reconnect
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mReader=new MessageReader(mBluetoothLeService);
                mReader.setUp();
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                if(!intent.getBooleanExtra(BluetoothLeService.EXTRA_DATA, false));
                //Toast.makeText(BluetoothChatActivity.this,"read data"+chose, Toast.LENGTH_SHORT).show();
                switch(chose)
                {
                    case 0:
                        mReader.setAddress(tmpSerial);
                        mReader.read(chose);
                        chose++;
                        break;
                    case 1:
                        mReader.setCache(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                        mReader.read(chose);
                        chose++;
                        break;
                    case 2://讀完資料了
                        mReader.setCache(intent.getLongExtra(BluetoothLeService.EXTRA_DATA,0));
                        chose=0;
                        mDBManager.record(mReader.getCache(),tmpSerial);
                        mReader.alreadyRead();
                        if(tmpSerial.equals(deviceSerial))
                        {
                            mFragment = (BluetoothChatFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);
                            mFragment.updateConversation();
                        }
                        break;
                }

            }
        }
    }
    /*設置內部廣播filter*/
    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ADVERTISER_SERVICE_CHANGE);
        intentFilter.addAction(Constants.SCANNER_SERVICE_CHANGE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*與Fragment溝通*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    //BluetoothChatFragment
    public void onSendMessage(Bundle bundle) {

        //Toast.makeText(this, "send 06"+bundle.getString(BluetoothChatFragment.EXTRAS_ADVERTISE_DATA), Toast.LENGTH_SHORT).show();
        //mAdvertiserService.updateADView(bundle);
        chatService.update(deviceSerial, bundle.getString(BluetoothChatFragment.EXTRAS_ADVERTISE_DATA),bundle.getLong(Constants.MSGID));
        mAdvertiserService.hub(bundle);
        //mAdvertiserService.connAD();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*服務操作*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    public void onDeviceSelected(Bundle bundle) {
        int flag = bundle.getInt(Constants.FRAGMENT_CHANGE);
        switch(flag) {
            case 2:
                bundle.putInt(Constants.SENDING,3);
                mAdvertiserService.hub(bundle);
                //mAdvertiserService.updateADdata(bundle);
                break;
            case 3:
                mFragment = (BluetoothChatFragment)
                        getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);
                mFragment.updateChatView(bundle);
                break;
            case 4:
                if(!mDBManager.FriendFilter(bundle.getString(Constants.ADVERTISER_DATA)))
                    break;
                tmpSerial=bundle.getString(Constants.ADVERTISER_DATA);
                Snackbar.make(findViewById(android.R.id.content), "Connect to ..." + tmpSerial, Snackbar.LENGTH_SHORT)
                        .show();
                mBluetoothLeService.connect(bundle.getString(Constants.DEVICE_ADDRESS),0);
                break;
        }
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
}

