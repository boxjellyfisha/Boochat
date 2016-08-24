package com.example.deer.boochat.service;



import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.adapter.MsgItemAdapter;
import com.example.deer.boochat.adapter.ScanResultAdapter;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;
import com.example.deer.boochat.for_mDB.ListItem;
import com.example.deer.boochat.for_mDB.ListTMPItemDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class ScannerService extends Service {

    private static final long SCAN_PERIOD = 3000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private MsgItemAdapter mItemAdapter;
    private SequenceNumber mSequenceNumber;
    private ListDBItemDAO mDBManager;
    private ListTMPItemDAO mTMPManager;
    private GattService chatService;
    private ScanCallback mScanCallback;
    private ScanResultAdapter mAdapter;
    private Handler mHandler; //其他執行緒(mTread)的小幫手
    private HandlerThread mThread;
    private Handler handler; //ui thread 小幫手
    private int device_localNum;
    String mToast;
    String device_name;

    /*綁定服務基本設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        stopScanning();
        if(mHandler != null){
            mHandler.removeCallbacks(r1);
        }
        if(mThread != null){
            mThread.quit();
        }
        return super.onUnbind(intent);
    }
    public class LocalBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*取得綁定活動實例*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /**
     * Must be called after object creation by MainActivity.
     *
     * @param btAdapter the local BluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }
    public void setMsgItemAdapter(MsgItemAdapter Adapter) {
        mItemAdapter = Adapter;
    }
    public void setSequenceNumber(SequenceNumber Adapter) {
         mSequenceNumber= Adapter;
    }
    public void setGattService(GattService mgatt)
    {
        chatService=mgatt;
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*服務創建*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = new ScanResultAdapter();
        mHandler = new Handler();
        /*開啟或創建資料庫*/
        mDBManager=new ListDBItemDAO(this);
        mTMPManager=new ListTMPItemDAO(this);
        /*新建檢查的執行續*/
        mThread=new HandlerThread("scanner");
        mThread.start();
        mHandler=new Handler(mThread.getLooper());

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanning();
        mDBManager.close();
    }
    /*訊息提示*/
    private void showToast(String msg)
    {
        mToast=msg;
        handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), mToast, Toast.LENGTH_LONG).show();
            }
        });
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*處理事件*/
    /*事件一: 開始掃描--------------------------------------------------------------------------------------------------------------------*/
    private Runnable r1=new Runnable(){
        public void run(){
            // TODO Auto-generated method stub
            try {
                startScanning();
            } catch (Exception e) {
                e.printStackTrace();
            }}};
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*藍牙掃描基礎設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*利用新執行續作掃描*/
    public void restartScanning()
    {
        mHandler.post(r1);
    }
    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning() {
        if (mScanCallback == null) {
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(null, buildScanSettings(), mScanCallback);
            showToast("is Scanning");
        } else {
            showToast(getString(R.string.already_scanning));
        }
    }
    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;

        // Even if no new results, update 'last seen' times.
        mAdapter.notifyDataSetChanged();
    }
    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

         ScanFilter.Builder filter = new ScanFilter.Builder();;
         filter.setServiceUuid(Constants.Service_UUID);
         ScanFilter.Builder filter2 = new ScanFilter.Builder();
         filter2.setServiceUuid(Constants.IOS_Service_UUID);

        scanFilters.add(filter.build());
        scanFilters.add(filter2.build());
        return scanFilters;
    }
    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }
    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class SampleScanCallback extends ScanCallback {

       @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results)
            {
                if(result.getScanRecord().getAdvertiseFlags()==6);

                else{
                    int caseNum=mAdapter.iOSChecking(result);
                    if(caseNum==-1);
                    else if(caseNum>=0)
                    iOSCheckThread(result,caseNum);
                else{
                    if(mAdapter.add(result)) {

                        CheckThread(result);//如果不是已接收封包 便採取判斷
                        mAdapter.addDevice(result.getDevice());
                        mAdapter.notifyDataSetChanged();
                    }}}
            }
        }

       @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

           if(result.getScanRecord().getAdvertiseFlags()==6);
           else{
           int caseNum=mAdapter.iOSChecking(result);
           if(caseNum==-1);
           else if(caseNum>=0)
               iOSCheckThread(result,caseNum);
           else{
            if(mAdapter.add(result)) {

                CheckThread(result);//如果不是已接收封包 便採取判斷
                mAdapter.addDevice(result.getDevice());
                mAdapter.notifyDataSetChanged();
            }}}
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            showToast("Scan failed with error: " + errorCode);
        }
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*儲存掃描內容 暫存使用*/
    private void SaveThread(ScanResult r)
    {
        ScanRecord re=r.getScanRecord();
        byte[] check=re.getManufacturerSpecificData(1);
        //ListItem i=new ListItem(new Date().getTime(), (String)m.get("sender"),(String)m.get("receiver"),(int)m.get("os"),0);
       // long index=mTMPManager.insert(i,Constants.AD_TABLE).getId();
        //ListItem ii=new ListItem(index, (int)m.get("opcode"), c, (int)m.get("localnum"), (long)m.get("msgid"), new String(l.get(p-c)));

    }

    /*封包組裝*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*重新組裝接收的0x02封包內容*/
    private String restructure(int i)
    {
        //取得掃描儲存的結果
        ArrayList<byte[]> array=mAdapter.getadData();
        String sentence=null;
        byte[] tmp = null;

        if(i>1) //表示為0x02型 需先剪掉頭兩包
        {
            array.remove(0);
            array.remove(0);
            tmp = sysCopy(array);
        }
        else  //表示為0x01型
            tmp=array.get(0);

        sentence=new String(tmp);
        return sentence;

    }
    /*將內容組裝*/
    public static byte[] sysCopy(List<byte[]> srcArrays) {

        int len = 0;
        int destLen = 0;

        for (byte[] srcArray : srcArrays)
        { len += srcArray.length; }

        byte[] destArray = new byte[len];
        for (byte[] srcArray : srcArrays)
        {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }

        return destArray;
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*對接收包作判斷*/
    /*----Android-----------------------------------------------------------------------------------------------------------------------------*/
    private  void  CheckThread(ScanResult r)
        {
            ScanRecord re=r.getScanRecord();
            Bundle b = new Bundle();

            byte[] check = re.getManufacturerSpecificData(1);
            switch (check[0]) //先瞭解為何種類型封包
            {
                case (byte) 0x00:  //服務端

                    break;

                case (byte) 0x01:  //註冊類型封包

                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putInt(Constants.LYAOUTTYPE, 2);
                    b.putInt(Constants.OPCODE, 1);
                    b.putInt(Constants.OSTYPE, 0);
                    if (check[1] != (byte) 0x01) //0x01 0x02
                    {
                        if (mSequenceNumber.getLocalNum() == 0)
                            mSequenceNumber.updateRouterTable(1, Constants.BuildSERIAL);
                        device_localNum = (int) mSequenceNumber.createLocalNum(device_name);
                        device_name = re.getDeviceName();
                        b.putLong(Constants.TIME, new Date().getTime());
                        b.putLong(Constants.MSGID, (long) device_localNum); //
                        b.putString(Constants.SENDER, device_name);
                        b.putString(Constants.RECEIVER, Constants.BuildSERIAL);
                        b.putInt(Constants.PRIORITY, 2); //代替為第幾個封包
                        b.putInt(Constants.LOCALNUM, mSequenceNumber.getLocalNum()); //本機的區域代碼
                        //b.putString(Constants.CONTENT, null);//b.putString(Constants.DEVICE_NAME, device_name);
                        mItemAdapter.addR(b);
                        broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);//將回傳03ACK通知對方傳下一個封包
                    } else {                  //0x01 0x01

                        mAdapter.readData(r);
                        mSequenceNumber.addLocalNum(check[2], restructure(0));
                        addNewFriend(restructure(0), device_name);
                        b.putString(Constants.DEVICE_NAME, device_name);
                        b.putLong(Constants.TIME, new Date().getTime());
                        b.putLong(Constants.MSGID, (long) device_localNum);
                        b.putString(Constants.SENDER, restructure(0));
                        b.putString(Constants.RECEIVER, Constants.BuildSERIAL);
                        b.putString(Constants.CONTENT, null);
                        b.putInt(Constants.PRIORITY, 1);
                        b.putInt(Constants.LOCALNUM, mSequenceNumber.getLocalNum());
                        mItemAdapter.addR(b);
                        broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);//將回傳05確認註冊完成並通知對方 並將對方之裝秩序好存起來
                        mAdapter.tmpClear();
                    }
                    break;

                case (byte) 0x02:  //訊息類型封包

                    mAdapter.readData(r);
                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putInt(Constants.LYAOUTTYPE, 1);
                    b.putInt(Constants.OPCODE, 2);
                    showToast("接收 " + check[1] + " package");
                    broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);//將回傳03ACK通知對方傳下一個封包

                    if (check[1] == (byte) 0x01)//最後一個
                    {
                        String tmp2 = null;
                        String tmp3 = null;
                        tmp2 = new String(mAdapter.getadData().get(1));
                        tmp3 = new String(mAdapter.getadData().get(0));
                        long t = new Date().getTime();
                        b.putInt(Constants.FRAGMENT_CHANGE, Constants.CHAT_FRAGMENT_CHANGE);
                        b.putInt(Constants.OPCODE, 2);

                        if ((Constants.BuildSERIAL).equals(tmp2)) {
                            byte[] tmp = mAdapter.getadData().get(0);

                            b.putLong(Constants.ACCEPT_TIME, t);
                            b.putString(Constants.ADVERTISER_DATA, restructure(mAdapter.getadData().size()));
                            broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);
                            //加入資料庫中
                            ListItem i = new ListItem(tmp3, t, restructure(mAdapter.getadData().size()), 1);
                            mDBManager.record(i, tmp3);
                            mAdapter.tmpClear();
                        } else {

                            tmp2 = new String(mAdapter.getadData().get(1));
                            tmp3 = new String(mAdapter.getadData().get(0));
                            showToast("不是我的訊息");
                            //進行轉發動作
                            b.putString(Constants.FORWARD_SENDER, tmp3);
                            b.putString(Constants.FORWARD_RECEIVER, tmp2);
                            b.putString(Constants.ADVERTISER_DATA, restructure(mAdapter.getadData().size()));
                            b.putBoolean(Constants.FORWARD, true);
                            broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);
                        }
                    }
                    mItemAdapter.addR(b);
                    break;

                case (byte) 0x03:  //確認接收封包 ACK的長度用來決定會傳TYPE

                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putInt(Constants.LYAOUTTYPE, 1);
                    b.putInt(Constants.OPCODE, 3);
                    if (check[1] == (byte) 0x01) //註冊類型
                    {
                        device_name = re.getDeviceName();
                        mSequenceNumber.addLocalNum(device_name, (int) check[2]);
                        mSequenceNumber.updateRouterTable((int) check[3], Constants.BuildSERIAL);
                        b.putString(Constants.DEVICE_NAME, device_name);
                        b.putLong(Constants.MSGID, (long) check[2]);
                        b.putInt(Constants.PRIORITY, 1);
                        b.putInt(Constants.LOCALNUM, mSequenceNumber.getLocalNum());
                        broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);//將回傳本機的裝置序號
                    } else {                  //訊息類型

                        b.putString(Constants.SENDER, new String(re.getServiceData(Constants.Service_UUID)));
                        b.putInt(Constants.MSGID, (int) check[3]);
                        b.putBoolean(Constants.LOCAL_NUM, false);
                        broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);//將停止目前動作，準備廣播02下一個封包 (在此須先檢查是否已傳完)
                    }

                    break;
                case (byte) 0x04:  //要求重傳
                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putInt(Constants.LYAOUTTYPE, 2);
                    b.putInt(Constants.OPCODE, 4);//重新廣播目前的封包02
                    broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);

                    break;
                case (byte) 0x05:  //確認註冊
                    mAdapter.readData(r);
                    //mDeviceTable.add(restructure(mAdapter.getDataCount()), device_name);

                    addNewFriend(restructure(mAdapter.getDataCount()), mSequenceNumber.getNamebyLN((int) check[2]));
                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putInt(Constants.LYAOUTTYPE, 1);
                    b.putInt(Constants.OPCODE, 5);//將註冊結果與自己的裝置序號配對 之後以此為地區代號
                    broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);
                    mAdapter.tmpClear();

                    break;

                case (byte) 0x06:  //連接型通訊
                    //showToast("收到06");
                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.BLEGATT_CONNECT_REQUEST);
                    b.putInt(Constants.LYAOUTTYPE, 2);
                    b.putString(Constants.DEVICE_ADDRESS, r.getDevice().getAddress());
                    b.putString(Constants.ADVERTISER_DATA, new String(r.getScanRecord().getServiceData(Constants.Service_UUID))); //給誰
                    broadcastUpdate(Constants.SCANNER_SERVICE_CHANGE, b);
                    mItemAdapter.addR(b);
                    break;
                case (byte) 0x09:
                    mItemAdapter.addR(b);
                    break;
            }

        }
    /*----iOS-----------------------------------------------------------------------------------------------------------------------------------*/
    private  void  iOSCheckThread(ScanResult r,int caseNum)
    {
        ScanRecord re=r.getScanRecord();
        Bundle b = new Bundle();
        showToast("收到IOS封包from" +re.getDeviceName());
        switch (caseNum)
        {
            case 0: //IOS_Service_UUID
                break;
            case 1: //IOS_Character_UUID
                break;
            case 2: //IOS_ADDService_UUID
                break;
            case 3: //IOS_ADDCharacter_UUID
                break;
            case 4: //IOS_Detect_UUID
                    b.putInt(Constants.FRAGMENT_CHANGE, Constants.ADVERTISER_FRAGMENT_CHANGE);
                    b.putString(Constants.DEVICE_NAME, re.getDeviceName());
                    broadcastUpdate(Constants.IOS_DETECT_REQUEST, b);
                break;
            case 5: //IOS_EchoService_UUID
                break;
            case 6: //IOS_PublicService_UUID
                b.putInt(Constants.FRAGMENT_CHANGE, Constants.BLEGATT_CONNECT_REQUEST);
                b.putString(Constants.DEVICE_NAME, re.getDeviceName());
                b.putString(Constants.DEVICE_ADDRESS, r.getDevice().getAddress());
                broadcastUpdate(Constants.IOS_CONNECT_REQUEST, b);
                break;
            case 7: //IOS_PublicCharacter_UUID
                break;
        }
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*將新朋友加入資料庫*/
    private void addNewFriend(String serial,String name)
    {
        ListItem i=new ListItem(0,new Date().getTime(),name, serial,0,0);//0表示還沒有聊天紀錄
        mDBManager.insert(i);
        mDBManager.setCreateTable(serial);
        chatService.setChatData(serial);
    }

    /*應用程式內部廣播*/
    private void broadcastUpdate(final String action,Bundle bun) {
        final Intent intent = new Intent(action);
        intent.putExtras(bun);
        sendBroadcast(intent);
    }

    private void friendState(String serial,int state)
    {

    }
}
