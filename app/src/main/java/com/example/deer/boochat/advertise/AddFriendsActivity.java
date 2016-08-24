package com.example.deer.boochat.advertise;

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
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.MainActivity;
import com.example.deer.boochat.R;
import com.example.deer.boochat.service.AdvertiserService;
import com.example.deer.boochat.service.ScannerService;

/**
 * Created by deer on 2015/8/26.
 */
public class AddFriendsActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private AdvertiserService mAdvertiserService;
    private ScannerService mScannerService;
    private ProgressBar mProgressBar;
    private UpdateReceiver myUpdateReceiver;
    private TextView textView;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mAdvertiserService = ((AdvertiserService.LocalBinder) service).getService();
            mAdvertiserService.setBluetoothAdapter(mBluetoothAdapter);
            // Automatically advertises the device upon successful start-up initialization.
            mAdvertiserService.advertiseMyDevice();
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
            // Automatically advertises the device upon successful start-up initialization.
            //mScannerService.startScanning();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mScannerService = null;
        }
    };

    /*一般生命週期設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_bar);

        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Slide());
        textView=(TextView)findViewById(R.id.state);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        Intent adServiceIntent = new Intent(this, AdvertiserService.class);
        bindService(adServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Intent scServiceIntent = new Intent(this, ScannerService.class);
        bindService(scServiceIntent, mScanServiceConnection, BIND_AUTO_CREATE);
        myUpdateReceiver = new UpdateReceiver();
        registerReceiver(myUpdateReceiver, makeUpdateIntentFilter());
        mProgressBar = (ProgressBar) findViewById(R.id.addfriend_prograss);
        //順利完成變回傳2
        setResult(RESULT_OK);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unbindService(mScanServiceConnection);
        unregisterReceiver(myUpdateReceiver);
        mAdvertiserService = null;
        mScannerService = null;
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*內部廣播*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*與服務溝通*/
    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle b=intent.getExtras();
            int progress;
            if (Constants.ADVERTISER_SERVICE_CHANGE.equals(action)) {
                progress = intent.getIntExtra("progress", 0);
                mProgressBar.setProgress(progress);

                if(progress==100) {
                    AddFriendsActivity.this.finish();
                }

            }
        else if(Constants.SCANNER_SERVICE_CHANGE.equals(action)) {
                if (b.getInt(Constants.OPCODE) == 3)
                    textView.setText("正在加入朋友 " + b.getString(Constants.DEVICE_NAME) + " 請稍候...");
                if (b.getInt(Constants.OPCODE) == 5)
                    textView.setText("你們已經成為朋友了! 正在結束...");
                onDeviceSelected(b);
            }
            else if(Constants.SENDING_ERROR.equals(action)) {
                AddFriendsActivity.this.finish();
                }
        }
    }

    /*廣播濾波器*/
    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ADVERTISER_SERVICE_CHANGE);
        intentFilter.addAction(Constants.SCANNER_SERVICE_CHANGE);
        return intentFilter;
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    //scannerFragment 使用之傳遞訊息(裝置位址、名稱)
    public void onDeviceSelected(Bundle bundle) {
        int flag = bundle.getInt(Constants.FRAGMENT_CHANGE);
        if (flag == 2) {
            bundle.putInt(Constants.SENDING, 3);
            mAdvertiserService.hub(bundle);
        }   //mAdvertiserService.updateADdata(bundle);
    }

}