package com.example.deer.boochat.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.chat_room.BluetoothChatFragment;
import com.example.deer.boochat.adapter.MsgItemAdapter;
import com.example.deer.boochat.for_mDB.ListItem;
import com.example.deer.boochat.for_mDB.ListTMPItemDAO;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
public class AdvertiserService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private ListTMPItemDAO mTMPManager;
    private Handler handler; //主執行續的小幫手
    private Handler mHandler; //計時執行緒(mTread)的小幫手
    private HandlerThread mThread;
    private int lock=0;
    private boolean flag=false;
    private boolean adDevice=false;
    private boolean sameGroup=false;

    private List<byte[]> mArrayList; //轉放切割完的訊息
    String mToast;
    String message="" ; //Let's Chatting.//一個常用中文字=3byte (UTF8)
    String packData;    //用來觀察封包內容
    boolean sendwho;    //用來判定有無發過對象的裝置序號
    boolean forward;    //用來判斷是否為轉傳
    boolean error;
    byte[] who;         //暫放 裝置序號 為設定包內使用的變數
    byte[] toWhom;      //暫放 接收者'
    byte[] toForwardReciever;      //暫放 轉傳接收者'
    byte[] opData;       //此封包的操作3B (manufactruer Data)
    byte[] send ;         //此封包的訊息 (service Data)
    private int packageLongth; //此次發送的內容總包數
    private int localnum=0;
    private int msgid;
    int count;              //用來倒數發送包

    int percent;            //下一階段%數
    int progress=0;

    public AdvertiseData mAdvertiseData;

    private final IBinder mBinder = new LocalBinder();
    private MsgItemAdapter mItemAdapter;
    private SequenceNumber mSequenceNumber;

    public int getLock(){return lock;}

    /*綁定服務基本設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public IBinder onBind(Intent intent) {
        handler=new Handler(Looper.getMainLooper());
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        stopAdvertising();
        if(mHandler != null){
            mHandler.removeCallbacks(r1);
        }
        if(mThread != null){
            mThread.quit();
        }
        return super.onUnbind(intent);
    }
    public class LocalBinder extends Binder {
       public AdvertiserService getService() {
            return AdvertiserService.this;
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
    /*取得main的藍牙實例*/
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    }
    /*取得main的訊息轉接實例*/
    public void setMsgItemAdapter(MsgItemAdapter Adapter) {
        mItemAdapter = Adapter;
    }
    /*取得main的流水號轉接實例*/
    public void setSequenceNumber(SequenceNumber Adapter) {
        mSequenceNumber= Adapter;
    }
    /*--------------------------------------------------------------------------------------------------------------------------------------------*/

    /*服務創建*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onCreate() {
        super.onCreate();

        /*開啟或創建資料庫*/
        mTMPManager=new ListTMPItemDAO(this);
        /*新建檢查的執行續*/
        mThread=new HandlerThread("timer");
        mThread.start();
        mHandler=new Handler(mThread.getLooper());

    }
    /*展示訊息*/
    private void showToast(String msg)
    {
        mToast=msg;
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), mToast, Toast.LENGTH_LONG).show();
            }
        });
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*執行事件*/
    /*事件一: 15秒關閉一次廣播  (表示15秒內沒有收到任何回應)--------------------------------------------------------------*/
    private Runnable r1=new Runnable(){

        public void run(){
                try {
                    mThread.sleep(15000);
                    flag=true;
                    stopAdvertising();
                    showToast("訊息傳送失敗");
                    if(lock==2)
                    msgItemUpdate(Constants.SENDING_ERROR,null);
                    error=true;
                    //將失敗的資料傳進資料庫
                    insert(mArrayList, mItemAdapter.getItemFirst(), packageLongth, count);
                    //開啟做另一件事
                    mHandler.post(r2);
                    flag=false;

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    };
    /*事件二: 檢查訊息佇列狀態---------------------------------------------------------------------------------------------------------*/
    private Runnable r2=new Runnable(){

        public void run(){
            try {


                if(count<1 || error) {
                    mItemAdapter.remove(1);
                    error=false;
                    check();
                }
               else
                    check();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    /*--------------------------------------------------------------------------------------------------------------------------------------------*/

    /*檢查佇列是否有人在等待*/
    public void check(){

        if(mItemAdapter.getCount()==1 && !sameGroup)
        {lock=0;showToast("unlock");
            sameGroup=false;}
        else{
            Map<String,Object> map=mItemAdapter.getItemFirst();
            showToast("傳下一個訊息"+map.get(Constants.CONTENT));
            msgid=mItemAdapter.getIndexMsgId((String)map.get(Constants.SENDER),(long)map.get(Constants.MSGID));
            updateADdata(map);
        }
    }
    /*存入資料庫*/
    public void insert( List<byte[]> l,Map<String,Object> m,int p,int c)
    {
        ListItem i=new ListItem((long)m.get("time"), (String)m.get("sender"),(String)m.get("receiver"),(int)m.get("os"),0);
        long index=mTMPManager.insert(i,Constants.AD_TABLE).getId();
        while(c>1)
        {
            ListItem ii=new ListItem(index, (int)m.get("opcode"), c, (int)m.get("localnum"), (long)m.get("msgid"), new String(l.get(p-c)));
            mTMPManager.insertItem(ii,Constants.AD_MSG);
            c--;
        }
    }

    /*藍牙廣播基本設置*/
    /*--------------------------------------------------------------------------------------------------------------------------------------------*/
    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising(AdvertiseData buildAdvertiseData) {

        mAdvertiseCallback = new SampleAdvertiseCallback();

        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData,
                    mAdvertiseCallback);
            //5秒後停止廣播該封包
            mHandler.post(r1);

        } else {

            showToast(getString(R.string.bt_null));
        }
    }

    /**
     * Custom callback after Advertising succeeds or fails to start.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            String errorMessage = getString(R.string.start_error_prefix);
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    errorMessage += " " + getString(R.string.start_error_already_started);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    errorMessage += " " + getString(R.string.start_error_too_large);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    errorMessage += " " + getString(R.string.start_error_unsupported);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    errorMessage += " " + getString(R.string.start_error_internal);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    errorMessage += " " + getString(R.string.start_error_too_many);
                    break;
            }

            showToast(errorMessage);

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // Don't need to do anything here, advertising successfully started.
        }
    }

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {

        if(mHandler != null && !flag){
            mHandler.removeCallbacks(r1);
        }
        if (mBluetoothLeAdvertiser != null) {

            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
            mAdvertiseData=null;

        } else {

            showToast(getString(R.string.bt_null));
        }
    }
    /*--------------------------------------------------------------------------------------------------------------------------------------------*/

   /*設置封包內容*/
    //---Android---------------------------------------------------------------------------------------------------------------------------------//

    /*0x01主動加友包*/
    public void advertiseMyDevice() {

        if(mAdvertiseCallback != null)
            stopAdvertising();
        lock=2;
        setOperation((byte) 0x01, (byte) 0x02,(byte) localnum,(byte)0x00, (byte) 0x00);
        startAdvertising(buildAdvertiseData((byte) 0x02));
        showToast(packData);
        startDownLoad(0, 50);
    }
    /*設置0x01的封包 ---0x01發送裝置序號 0x02發送裝置名稱 TxPower*/
    private AdvertiseData buildAdvertiseData(byte i) {

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.addManufacturerData(1, opData); //廠商代號先使用1
        switch(i)
        {
            case (byte)0x01:
                dataBuilder.addServiceData(Constants.Service_UUID, who);
                break;
            case (byte)0x02:
                dataBuilder.setIncludeDeviceName(true);
                dataBuilder.setIncludeTxPowerLevel(true);
                break;
        }

        packData=(dataBuilder.build()).toString();
        return dataBuilder.build();
    }
    /*設置0x02的內容封包*/
    protected void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();

        mBuilder.addServiceUuid(Constants.Service_UUID);
        mBuilder.addManufacturerData(1, opData);
        mBuilder.addServiceData(Constants.Service_UUID, send);

        mAdvertiseData= mBuilder.build();
        packData=mAdvertiseData.toString();

    }
    private AdvertiseData bAdvertiseData()
    {
       setAdvertiseData();
        return mAdvertiseData;
    }
    /*設置0x06的內容封包*/
    protected AdvertiseData setConnectableData() {
        byte[] op={(byte)0x06};
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();

        mBuilder.addServiceUuid(Constants.Service_UUID);
        mBuilder.addManufacturerData(1, op);
        mBuilder.addServiceData(Constants.Service_UUID, who);

        packData=(mBuilder.build()).toString();
        return mBuilder.build();

    }
    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life).
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setConnectable(true);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        return settingsBuilder.build();
    }
    /*設定規劃的5B'*/
    private void setOperation(byte op,byte size,byte who,byte msgid,byte rfu)
    {
        opData = null;
        ByteBuffer mManufacturerData = ByteBuffer.allocate(5);
        mManufacturerData.put(0, op); //發送註冊封包
        mManufacturerData.put(1, size); //封包總個數
        mManufacturerData.put(2, who); //地區代號 尚未完成
        mManufacturerData.put(3, msgid); //訊息代碼
        mManufacturerData.put(4, rfu); //RFU 尚未完成
        opData = mManufacturerData.array();
    }
    /*缺少RFU*/
    private void setOperation(byte op,byte size,byte who,byte msgid)
    {
        opData = null;
        ByteBuffer mManufacturerData = ByteBuffer.allocate(4);
        mManufacturerData.put(0, op); //發送註冊封包
        mManufacturerData.put(1, size); //封包總個數
        mManufacturerData.put(2, who); //地區代號 尚未完成
        mManufacturerData.put(3, msgid); //訊息代碼
        opData = mManufacturerData.array();
    }
    /*選擇0x02廣播內容段*/
    private void setChatMessage()
    {
        send = mArrayList.get(packageLongth-count);
        count--;
    }
    /*切割封包訊息*/
    private ArrayList<byte[]> cut(String msg)
    {
    /*用賴切割傳遞訊息 將12Byte為一單位 放入arrayList中*/
        byte[] tmp=msg.getBytes(); //將字串先轉成位元組
        ByteBuffer mtmp ;
        int a=tmp.length%12;        //最後一包為多少
        int b=tmp.length/12;        // 總數 / 12 = b ... a
        ArrayList<byte[]> sent=new ArrayList<>();
        if(b==0 || b==1 && a==0)//一包不滿 與剛好一包
        {
            sent.add(0,tmp);
            return sent;
        }
        for(int i=0;i<=b;i++)
        {
        mtmp= ByteBuffer.allocate(12);
        mtmp.clear();
        for(int k=0;k<12;k++)
        mtmp.put(k, (byte)0);

            if(i==b)     //最後一包
            {
                if (a == 0);//沒有剩


                else
                {
                    for(int j=0;j<a;j++)
                          mtmp.put(tmp[j+i*12]);

                    sent.add(i,mtmp.array());
                }
            }

            else{
            for(int j=0;j<12;j++)
                mtmp.put(tmp[j+i*12]);

            sent.add(i,mtmp.array());

            }
        }
        return sent;
    }

    //----iOS-------------------------------------------------------------------------------------------------------------------------------------//

    protected AdvertiseData setData() {
        //byte[] op={(byte)0x06};
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();

        mBuilder.addServiceUuid(Constants.IOS_EchoService_UUID);
        //mBuilder.addManufacturerData(1, op);
        //mBuilder.addServiceData(Constants.Service_UUID, who);
        mBuilder.setIncludeDeviceName(true);
        packData=(mBuilder.build()).toString();
        return mBuilder.build();

    }
    public void testAD()
    {
        if(mAdvertiseCallback != null)
            stopAdvertising();
        startAdvertising(setData());
        showToast("send IOS add packet\n" + packData);
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*封包前往接口*/
    /*所有來源封包需先往此，判斷lock狀態*/
    public void hub(Bundle bundle)
    {
        int chose=bundle.getInt(Constants.SENDING);

        if(lock>0)//&& mAdvertiseCallback!=null
        {

            if(bundle.getInt(Constants.OPCODE)==1)
                //if(mItemAdapter.addfriendcopare(bundle.getInt(Constants.LOCALNUM), bundle.getInt(Constants.MSGID)))
                updateADdata(bundle);

            if(lock==2 && bundle.getInt(Constants.LYAOUTTYPE)==1)
            {//現在是主動的發訊方  接收 03 for 01 type & 05
                updateADdata(bundle);
            }

            else if(chose==3 && bundle.getInt(Constants.OPCODE)==3) {
                //接收03封包

                if(mItemAdapter.compareMsgId(bundle.getString(Constants.SENDER), bundle.getInt(Constants.MSGID)))
                { updateADdata(bundle);sameGroup=true;}
                else
                    mItemAdapter.add(bundle);
                        /*switch(bundle.getInt(Constants.PRIORITY))
                        {
                            case 1: //03 01

                                break;
                            case 2: //01 02       break;
                }*/
            }
            else
            {mItemAdapter.add(bundle);
                showToast("等待，目前數量"+String.valueOf(mItemAdapter.getCount()-1));}
        }//||isWaiting()
        else {
            lock = 1;
            localnum=bundle.getInt(Constants.LOCALNUM);
            switch(chose) {
                case 1: //from chat2 room
                    mItemAdapter.add(bundle);
                    connAD();
                    break;
                case 2: //from public
                    mItemAdapter.add(bundle);
                    updateADView(bundle);
                    break;
                case 3: //frome scanner (ACK)
                    updateADdata(bundle);
                    break;
                case 4:
                    testAD();
                    break;
            }
        }
    }

    /*實際準備廣播動作*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*接收由Bluetooth Chat Fragment而來的訊息包 Sender要廣播的訊息*/
    public void updateADView(Bundle bundle)
    {
        if (bundle == null);
        else
        {
            //先將訊息取出 並做好分裝動作
            message = null;
            message = bundle.getString(BluetoothChatFragment.EXTRAS_ADVERTISE_DATA);
            mArrayList=cut(message);           //分割訊息
            count=mArrayList.size();           //倒數封包數用
            packageLongth=mArrayList.size(); //訊息封包總數 (不含頭兩包)
            sendwho=true;       //為傳送第二封包(收件人裝置序號)判斷用  true:還沒寄過 false:已經寄過
            forward = false;
            showToast((byte) (packageLongth + 2) + "size");
           if(mAdvertiseCallback != null)
                stopAdvertising();
            //0x02 訊息型封包 內容:發送方裝置序號 (第一個封包)
            setOperation((byte) 0x02, (byte) (packageLongth+2),(byte) localnum,(byte)msgid, (byte)0x00);
            who=Constants.sendSERIAL;       //放入傳送方序號
            startAdvertising(buildAdvertiseData((byte) 0x01));
            //showToast("send my name\n" + packData);

        }
    }
    /*廣播訊息服務 聊天用連接的方式*/
    public void connAD()
    {
        if(mAdvertiseCallback != null)
            stopAdvertising();
        setOperation((byte) 0x06, (byte) 0x00,(byte) localnum,(byte)msgid, (byte) 0x00);
        who=Constants.sendSERIAL;
        startAdvertising(setConnectableData());
        //showToast("send 06 packet\n" + packData);

    }

    /*接收由 Scanner Fragment 來的訊息包 屬於回傳03的封包判斷*/
    public void updateADdata(Map<String,Object> mItem) {
        int opcode=(int)mItem.get(Constants.OPCODE);
        int LocalNumP=(int)mItem.get(Constants.PRIORITY);
        Bundle bun=new Bundle();
        switch(opcode)  //判斷為何種封包 並做出相對應的操作
        {
            case 1:
                localnum=(int)mItem.get(Constants.LOCALNUM);
                msgid=(int)mItem.get(Constants.MSGID);
                if(LocalNumP==2)//接收到0x01註冊型封包 ----將回傳03ACK通知對方傳下一個封包
                {
                    if(mAdvertiseCallback != null)
                    {stopAdvertising();}
                    //0x03 回覆型封包 內容:Device name & TX power
                    setOperation((byte) 0x03, (byte) 0x01,(byte) localnum,(byte)msgid, (byte) 0x00);
                    startAdvertising(buildAdvertiseData((byte) 0x02));
                    showToast("get package1 send package3name\n" + packData);
                    broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                }
                else//接收到0x01註冊2號封包 ----將回傳05確認註冊完成並通知對方 並將對方的裝置序號存起來
                {
                    if(mAdvertiseCallback != null)
                        stopAdvertising();
                    //0x05 確認註冊型封包 內容:裝置序號
                    setOperation((byte) 0x05, (byte) 0x01,(byte) localnum,(byte)msgid,(byte) 0x00);
                    who=Constants.sendSERIAL;
                    startAdvertising(buildAdvertiseData((byte) 0x01));
                    broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                }
                break;

            case 2://接收0x02訊息型封包 ----將回傳03ACK通知對方傳下一個封包 內容放自己的名字
                if(mAdvertiseCallback != null)
                    stopAdvertising();
                setOperation((byte) 0x03, (byte) 0x02, (byte) localnum, (byte) msgid, (byte) 0x00);
                send=Constants.sendSERIAL; //在回覆包附上自己的序號
                startAdvertising(bAdvertiseData());
                showToast("get message send ACK\n" + packData);
                broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                break;

            case 3://接收0x03回覆型封包 ----將停止目前動作，準備廣播下一個封包 (在此須先檢查是否已傳完)
                if(LocalNumP==1) //表示此封包為回傳確定註冊封包
                {
                    if(mAdvertiseCallback != null)
                        stopAdvertising();
                    //傳主動註冊的下個封包 內容 : 裝置序號
                    setOperation((byte)0x01,(byte)0x01,(byte) localnum,(byte)msgid,(byte)0x00);
                    who=Constants.sendSERIAL;
                    startAdvertising(buildAdvertiseData((byte) 0x01));
                    showToast("My Serial number\n" + packData);
                    startDownLoad(percent, 90);

                }
                break;
            case 4://重新廣播目前的封包02
                break;

            case 5://接收0x05確認註冊型封包 ----將停止廣播
                if(mAdvertiseCallback != null)
                    stopAdvertising();
                mHandler.post(r2);
                startDownLoad(90,100);
                break;

        }
    }

    /*接收由 Scanner Fragment 來的訊息包 將掃描結果作判讀的動作*/
    public void updateADdata(Bundle bundle) {
        int opcode=bundle.getInt(Constants.OP_CODE);
        int LocalNumP = bundle.getInt(Constants.PRIORITY);
        Bundle bun=new Bundle();
        localnum= bundle.getInt(Constants.LOCALNUM);
        msgid=(int)bundle.getLong(Constants.MSGID);
        switch(opcode)  //判斷為何種封包 並做出相對應的操作
        {
            case 1:
                if(bundle.getInt(Constants.PRIORITY)==2)//接收到0x01註冊型封包 ----將回傳03ACK通知對方傳下一個封包
                {
                    count=0;
                    if(mAdvertiseCallback != null)
                    {stopAdvertising();}
                    //0x03 回覆型封包 內容:Device name & TX power
                    setOperation((byte) 0x03, (byte) 0x01, (byte) localnum, (byte) msgid, (byte) 0x00);
                    startAdvertising(buildAdvertiseData((byte) 0x02));
                    showToast("get package1 send package3name\n" + packData);
                    broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);

                }
                else if(bundle.getInt(Constants.PRIORITY)==1)//接收到0x01註冊2號封包 ----將回傳05確認註冊完成並通知對方 並將對方的裝置序號存起來
                {
                    if(mAdvertiseCallback != null)
                    stopAdvertising();
                    //0x05 確認註冊型封包 內容:裝置序號
                    setOperation((byte) 0x05, (byte) 0x01,(byte) localnum,(byte)msgid);
                    who=Constants.sendSERIAL;
                    startAdvertising(buildAdvertiseData((byte) 0x01));
                    showToast("send package5\n" + bundle.getString(Constants.DEVICE_NAME) + "\n" + packData);
                    broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                    //聊天對象確立
                    //toWhom=bundle.getString(Constants.ADVERTISER_DATA).getBytes();
                    //將從ScanResultAdapter接收的資料 (裝置名稱 裝置序號) 轉綁傳給BluetoothChatFragment
                    //bun.putString(Constants.DEVICE_NAME, bundle.getString(Constants.DEVICE_NAME));
                    //bun.putString(Constants.ADVERTISER_DATA,bundle.getString(Constants.ADVERTISER_DATA));
                    //bun.putInt(Constants.OP_CODE, 1);
                    //mCallback.onAdvertise(bun);
                    /*mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopAdvertising();
                }
            }, 5000);*/
                }
                break;

            case 2://接收0x02訊息型封包 ----將回傳03ACK通知對方傳下一個封包 內容放自己的名字
                if(mAdvertiseCallback != null)
                stopAdvertising();
                setOperation((byte) 0x03, (byte) 0x02,(byte) localnum,(byte)msgid,(byte) 0x00);
                send=Constants.sendSERIAL; //在回覆包附上自己的序號
                startAdvertising(bAdvertiseData());
                showToast("get message send ACK\n" + packData);
                broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                //進行轉傳
                if(bundle.getBoolean(Constants.FORWARD))
                    forward(bundle);
                break;

            case 3://接收0x03回覆型封包 ----將停止目前動作，準備廣播下一個封包 (在此須先檢查是否已傳完)
                if(LocalNumP==1) //表示此封包為回傳確定註冊封包
                {
                    if(mAdvertiseCallback != null)
                    stopAdvertising();
                    //傳主動註冊的下個封包 內容 : 裝置序號
                    setOperation((byte)0x01,(byte)0x01,(byte) localnum,(byte)msgid);
                    who=Constants.sendSERIAL;
                    startAdvertising(buildAdvertiseData((byte) 0x01));
                    showToast("My Serial number\n" + packData);
                    startDownLoad(percent,90);
                }
                else    //表示此封包為回傳確定訊息封包
                {
                    if(sendwho) {
                        if (mAdvertiseCallback != null)
                            stopAdvertising();
                        //0x02 訊息型封包 內容:接受方裝置序號 (第二個封包)
                        setOperation((byte) 0x02, (byte) (packageLongth + 1), (byte) localnum,(byte)msgid,(byte) 0x00);
                        if(forward)
                            who=toForwardReciever;
                        else
                        who = toWhom; //對象序號
                        startAdvertising(buildAdvertiseData((byte) 0x01));
                        sendwho=false;  //表示發過
                        showToast("get ACK send toWhom\n" + packData);
                        broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                    }
                    else if(count==0)
                    {
                        if (mAdvertiseCallback != null)
                            stopAdvertising();
                        //0x02 訊息型封包 已經發完了
                        mHandler.post(r2);
                        bun.putBoolean(Constants.SEND_BTN_ENABLE, true);
                        bun.putInt(Constants.OP_CODE, 2);
                        broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                        //mCallback.onAdvertise(bun); //將前往聊天視窗顯示 發送完畢
                    }
                    else {
                        if (mAdvertiseCallback != null)
                            stopAdvertising();

                        //0x02 訊息型封包 主要內容
                        setOperation((byte) 0x02, (byte) (count),(byte) localnum,(byte)msgid,(byte) 0x00);
                        setChatMessage();
                        startAdvertising(bAdvertiseData());
                        showToast("get ACK send message\n" + packData);
                        broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE, null);
                    }
                }
                break;
            case 4://重新廣播目前的封包02
                break;

            case 5://接收0x05確認註冊型封包 ----將停止廣播
                if(mAdvertiseCallback != null)
                stopAdvertising();
                lock=1;
                mHandler.post(r2);
                showToast("get package5 from" + bundle.getString(Constants.DEVICE_NAME));
                startDownLoad(90,100);
                break;

        }
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*判讀改變對象為聊天視窗*/
    public void updateToWhom(String serial)
    {
        toWhom=serial.getBytes();
        showToast("Change: " + serial);
    }

    /*註冊進度控制*/
    public void startDownLoad(int op,int ep){
        progress=op;
        percent=ep;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(progress < percent){
                    progress += 1;

                    broadcastUpdate(Constants.ADVERTISER_SERVICE_CHANGE,null);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    /*應用程式內部廣播*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*傳遞失敗通知*/
    private void msgItemUpdate(final String action,Bundle bun) {
        final Intent intent = new Intent(action);
        intent.putExtras(bun);
        sendBroadcast(intent);
    }
    /*進度更新通知*/
    private void broadcastUpdate(final String action,Bundle bun) {
        final Intent intent = new Intent(action);
        intent.putExtra("progress", progress);
        sendBroadcast(intent);
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/









    /*檢查是否有人在等待*/
    public boolean isWaiting()
    {
        if( mTMPManager.isWaiting(Constants.AD_TABLE)||
        mTMPManager.isWaiting(Constants.CONN_IOS_TABLE)||
        mTMPManager.isWaiting(Constants.CONN_TABLE))
            return true;
        else return false;
    }

    /*轉傳的動作*/
    public void forward(Bundle bundle)
    {
        //先將訊息取處 並做好分裝動作
        message = null;
        message = bundle.getString(Constants.ADVERTISER_DATA);
        mArrayList=cut(message);           //分割訊息
        count=mArrayList.size();           //倒數封包數用
        packageLongth=mArrayList.size(); //訊息封包總數 (不含頭兩包)
        sendwho=true;       //為傳送第二封包(收件人裝置序號)判斷用  true:還沒寄過 false:已經寄過
        forward=bundle.getBoolean(Constants.FORWARD);
        toForwardReciever=(bundle.getString(Constants.FORWARD_RECEIVER)).getBytes();
        showToast((byte) (packageLongth + 2) + "size");
        if(mAdvertiseCallback != null)
            stopAdvertising();
        //0x02 訊息型封包 內容:發送方裝置序號 (第一個封包)
        setOperation((byte) 0x02, (byte) (packageLongth+2),(byte) localnum,(byte)msgid, (byte) 0x00);
        who=(bundle.getString(Constants.FORWARD_SENDER)).getBytes();       //放入傳送方序號
        startAdvertising(buildAdvertiseData((byte) 0x01));
        showToast("send my name\n" + packData);

    }


}


