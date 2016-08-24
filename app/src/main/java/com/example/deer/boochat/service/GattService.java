package com.example.deer.boochat.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.for_mDB.ListDBItemDAO;

import java.util.Date;
import java.util.UUID;

/**
 * Created by deer on 2015/9/29.
 */
public class GattService {

    private final static String TAG = GattService.class.getSimpleName();

    public static final UUID Chat_Service_UUID =
            UUID.fromString("2bc2eba8-5942-11e5-885d-feff819cdc9f");
    public static final UUID Descriptor_time_UUID=
            UUID.fromString("122099a4-6697-11e5-9d70-feff819cdc9f");
    public static final UUID Descriptor_content_UUID=
            UUID.fromString("222099a4-6697-11e5-9d70-feff819cdc9f");

    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCheckMail;
    private BluetoothGattDescriptor mTime;
    private BluetoothGattDescriptor mContent;
    private BluetoothManager manager;
    private BluetoothGattServer server;
    private Context context;
    private ListDBItemDAO mDBManager;
    private long id;
    private String who;

    public GattService( BluetoothManager m,Context c)
    {
        context = c;
        manager = m;
        mDBManager=new ListDBItemDAO(c);
        if(mGattService==null)
        mGattService=new BluetoothGattService(Chat_Service_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        else mGattService=server.getService(Chat_Service_UUID);
    }
    public boolean existence(String Who)
    {
       if( mGattService.getCharacteristic(createUUID(Who))==null)
        return false;
           else
        return true;
    }
    public void setChatData(String Who)
    {
        mCheckMail=new BluetoothGattCharacteristic(
                createUUID(Who),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_READ| BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE);
        mTime=new BluetoothGattDescriptor(
                Descriptor_time_UUID,
                BluetoothGattCharacteristic.PERMISSION_READ);
        mContent=new BluetoothGattDescriptor(
                Descriptor_content_UUID,
                BluetoothGattCharacteristic.PERMISSION_READ);
        mCheckMail.addDescriptor(mTime);
        mCheckMail.addDescriptor(mContent);
        mGattService.addCharacteristic(mCheckMail);

        server = manager.openGattServer(context, mGattServerCallback);
        if(mGattService!=null)
            server.removeService(mGattService);
        server.addService(mGattService);
    }

    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        //读写Characteristic，在此获得客户端发来的消息
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,int offset, byte[] value) {
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            mDBManager.updateState(id,who,1);
        }
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Our gatt characteristic was read.");
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        }
        @Override
     public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor)
        {
            Log.d(TAG, "Our gatt descriptor was read.");
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }
    };

    public String get(String who)
    {
        mCheckMail= mGattService.getCharacteristic(createUUID(who));
        mContent=mCheckMail.getDescriptor(Descriptor_content_UUID);
        return new String(mContent.getValue());
    }
    public void update(String who,String data,long id)
    {
        this.id=id;
        this.who=who;
        String update="0";
        Long t=new Date().getTime();
        mCheckMail= mGattService.getCharacteristic(createUUID(who));
        mCheckMail.setValue(update.getBytes());
        //mGatt.writeCharacteristic(mCheckMail);
        mCheckMail.getDescriptor(Descriptor_time_UUID).setValue(t.toString().getBytes());
        //mGatt.writeDescriptor(mCheckMail.getDescriptor(Descriptor_time_UUID));
        mCheckMail.getDescriptor(Descriptor_content_UUID).setValue(data.getBytes());
        //mGatt.writeDescriptor(mCheckMail.getDescriptor(Descriptor_content_UUID));
    }
    /*利用serial number轉換成uuid來給character*/
    private UUID createUUID(String toWhom)
    {
        byte[] tmp=toWhom.getBytes();
        int middle=tmp.length/2;
        long mostSigBit=0;
        long leastSigBit=0;

        for (int i = 0; i < tmp.length; i++)
        {
            if(i>middle)
                leastSigBit += ((long) tmp[i] & 0xffL) << (8 * i);
            mostSigBit += ((long) tmp[i] & 0xffL) << (8 * i);
        }
        return new UUID(mostSigBit,leastSigBit);
    }

}
