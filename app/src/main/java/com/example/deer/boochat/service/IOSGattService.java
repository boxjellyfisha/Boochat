package com.example.deer.boochat.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.example.deer.boochat.Constants;

import java.util.Date;
import java.util.UUID;

/**
 * Created by deer on 2015/10/22.
 */
public class IOSGattService {
    private final static String TAG = GattService.class.getSimpleName();

    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCheckMail;
    private BluetoothGattDescriptor mTime;
    private BluetoothGattDescriptor mContent;
    private BluetoothManager manager;
    private BluetoothGattServer server;
    private Context context;
    public static final UUID IOS_PublicService_UUID =
            UUID.fromString("ACF3DEE0-75F9-4381-8EF3-1859C8A8BCCA");
    public static final UUID IOS_PublicCharacter_UUID =
            UUID.fromString("49F83D40-5722-4DBF-9215-71D30D539589");


    public IOSGattService( BluetoothManager m,Context c)
    {
        context = c;
        manager = m;
        if(mGattService==null)
            mGattService=new BluetoothGattService(IOS_PublicService_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        else mGattService=server.getService(IOS_PublicService_UUID);
    }
    public void setIOSChatData()
    {
        mCheckMail=new BluetoothGattCharacteristic(
                IOS_PublicCharacter_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_READ| BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE);
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
        }
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Our gatt characteristic was read.");
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        }
        @Override
        public void onNotificationSent (BluetoothDevice device, int status)
        {

        }

    };

    public void update(String data)
    {
        mCheckMail= mGattService.getCharacteristic(IOS_PublicCharacter_UUID);
        mCheckMail.setValue(data);
    }
}
