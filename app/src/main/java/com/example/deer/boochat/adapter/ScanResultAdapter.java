package com.example.deer.boochat.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ScanResultAdapter extends BaseAdapter {

    private ArrayList<ScanResult> mArrayList;

    private Context mContext;

    private LayoutInflater mInflater;

    private ArrayList<BluetoothDevice> mLeDevices;

    private ArrayList<byte[]> mDeviceData;

    private ArrayList<ParcelUuid> miOSUUID;

    private Map<String,String> adDataPair;

    public int size=0;

    public Boolean flag;

    byte[] packageData;

    byte count;

    String stringData;




    public ScanResultAdapter() {

        mArrayList = new ArrayList<>();
        mLeDevices = new ArrayList<>();
        mDeviceData = new ArrayList<>();
        adDataPair=new HashMap<>();
        miOSUUID=new ArrayList<>();
        miOSUUID.add(Constants.IOS_Service_UUID);        //0
        miOSUUID.add(Constants.IOS_Character_UUID);      //1
        miOSUUID.add(Constants.IOS_ADDService_UUID);    //2
        miOSUUID.add(Constants.IOS_ADDCharacter_UUID);  //3
        miOSUUID.add(Constants.IOS_Detect_UUID);         //4
        miOSUUID.add(Constants.IOS_EchoService_UUID);   //5
        miOSUUID.add(Constants.IOS_PublicService_UUID);  //6
        miOSUUID.add(Constants.IOS_PublicCharacter_UUID);//7


    }

    public ScanResultAdapter(Context context, LayoutInflater inflater) {
        super();
        mContext = context;
        mInflater = inflater;
        mArrayList = new ArrayList<>();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mDeviceData = new ArrayList<byte[]>();
        adDataPair=new HashMap<String,String>();

    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }
    public boolean readData(ScanResult scanResult)
    {
        ScanRecord record= scanResult.getScanRecord();

        //讀ServiceData 訊息內容 (此封包的主要內容) 0 : opcode  1 : length   2 : local num
        byte[] data =record.getServiceData(Constants.Service_UUID);
        //讀目前的封包序號
        byte[] d=record.getManufacturerSpecificData(1);

            if (count == d[1]) ;//重複就不加入
            else
                mDeviceData.add(data);

            count = d[1];//表示接收到第幾個封包  實際值為倒數封包數值

        //讀Record的內容並轉為16進位
        packageData=record.getBytes();
        final StringBuilder stringBuilder2 = new StringBuilder(packageData.length);
        for(byte byteChar : packageData)
            stringBuilder2.append(String.format("%02X ", byteChar));
        stringData=stringBuilder2.toString();

        return true;
    }

    public String getPackage() {return stringData;}//測試用

    public ArrayList<byte[]> getadData()
    {
        return mDeviceData;
    }

    public int getDataCount() {return mDeviceData.size();}

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mArrayList.get(position).getDevice().getAddress().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Reuse an old view if we can, otherwise create a new one.
        if (view == null) {
            view = mInflater.inflate(R.layout.tab_chat, null);
        }

        //TextView deviceNameView = (TextView) view.findViewById(R.id.device_name);
        //TextView deviceAddressView = (TextView) view.findViewById(R.id.device_address);
       // TextView lastSeenView = (TextView) view.findViewById(R.id.last_seen);

        //ScanResult scanResult = mArrayList.get(position);

        //deviceNameView.setText(scanResult.getDevice().getName());
        //deviceAddressView.setText(scanResult.getDevice().getAddress());
       // lastSeenView.setText(getTimeSinceString(mContext, scanResult.getTimestampNanos()));

        return view;
    }

    /**
     * Search the adapter for an existing device address and return it, otherwise return -1.
     */
    private int getPosition(String address) {
        int position = -1;

            for (int i = 0; i < mArrayList.size(); i++) {
                if (mArrayList.get(i).getDevice().getAddress().equals(address)) {
                    position = i;
                    break;
                }

        }
        return position;
    }
    private int getPosition(String address,ParcelUuid uuid) {
        int position = -1;

        for (int i = 0; i < mArrayList.size(); i++) {
            if (mArrayList.get(i).getDevice().getAddress().equals(address)
                    && mArrayList.get(i).getScanRecord().getServiceUuids().get(0).equals(uuid)) {
                if(overFive(mArrayList.get(i).getTimestampNanos()))
                    position = i;
                else
                    position=-2;
                break;
            }

        }
        return position;
    }


    /**
     * Add a ScanResult item to the adapter if a result from that device isn't already present.
     * Otherwise updates the existing position with the new ScanResult.
     */
    public boolean add(ScanResult scanResult) {

        int existingPosition = getPosition(scanResult.getDevice().getAddress());

        if (existingPosition >=0) {
            // Device is already in list, update its record.
            mArrayList.set(existingPosition, scanResult);
            flag=false;
        }
        else{
            // Add new Device's ScanResult to list.
            mArrayList.add(scanResult);
            flag=true;
        }
        return flag;
    }

    //識別android封包 暫設
    public boolean adkChecking(ScanResult scanResult)
    {

        ParcelUuid tmp=scanResult.getScanRecord().getServiceUuids().get(0);
        if(!tmp.equals(Constants.Service_UUID))
        {
            return false;
        }
        else{
            if(scanResult.getScanRecord().getServiceData()!=null && scanResult.getScanRecord().getManufacturerSpecificData()!=null)
                return true;
        }
        return false;
    }
    public int iOSChecking(ScanResult scanResult)
    {
        ParcelUuid tmp=scanResult.getScanRecord().getServiceUuids().get(0);
        int k=-2;
        for(int i=0;i<miOSUUID.size();i++)
        {
            if(tmp.equals(miOSUUID.get(i)))
            {
                k=i;
                break;
            }
        }
        if(k>-2){
        int existingPosition = getPosition(scanResult.getDevice().getAddress(),tmp);
        if (existingPosition >=0) {
            // Device is already in list, update its record.
            mArrayList.set(existingPosition, scanResult);
        }
        else if(existingPosition==-2)
            k=-1;
        else{
            // Add new Device's ScanResult to list.
            mArrayList.add(scanResult);
        }}
        return k;
    }

    /**
     * Clear out the adapter.
     */
    public void clear() {
        mArrayList.clear();
        adDataPair.clear();
        mLeDevices.clear();
        //mDeviceData.clear();
    }

    public void tmpClear(){mDeviceData.clear();}


    /*確認時間是否超過五秒*/
    private boolean overFive(long timeNanoseconds)
    {
        long timeSince = SystemClock.elapsedRealtimeNanos() - timeNanoseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.NANOSECONDS);
        if (secondsSince >5) {
            return true;
        }
        else
            return false;

    }


    /**
     * Takes in a number of nanoseconds and returns a human-readable string giving a vague
     * description of how long ago that was.
     */
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
