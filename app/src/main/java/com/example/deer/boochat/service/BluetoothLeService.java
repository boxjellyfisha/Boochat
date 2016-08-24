/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.deer.boochat.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.chat_room.MessageReader;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    private BluetoothGattCharacteristic mCharacteristric;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    /*綁定服務基本設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    public final IBinder mBinder = new LocalBinder();
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*初期與結尾設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*初始化*/
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*藍牙連線服務基本設置*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address ,int type) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if(type==0)
            mBluetoothGatt = device.connectGatt(this, false, iGattCallback);
        else
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    /*連線回報內容*/
    /*----------Android-----------------------------------------------------------------------------------------------------------------------*/
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            //寫入已結束 表示已經讀完資料
            disconnect();
        }
        @Override// Callback triggered as a result of a remote characteristic notification.
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            //可以開始讀取描述值
            Log.i(TAG, "Reading descriptor");
            broadcastUpdate(ACTION_DATA_AVAILABLE,descriptor);
        }

    };
    /*----------iOS-----------------------------------------------------------------------------------------------------------------------------*/
    private final BluetoothGattCallback iGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 訂閱遠端設備的characteristic，
                // 當此characteristic發生改變時當回調mBtGattCallback中的onCharacteristicChanged方法
                mService =mBluetoothGatt.getService(IOSGattService.IOS_PublicService_UUID);
                mCharacteristric = mService
                        .getCharacteristic(IOSGattService.IOS_PublicCharacter_UUID);
                mBluetoothGatt.setCharacteristicNotification( mCharacteristric,
                        true);
                BluetoothGattDescriptor descriptor = mCharacteristric
                        .getDescriptor(UUID
                                .fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    descriptor.setValue(val);
                    mBluetoothGatt.writeDescriptor(descriptor);
                ibroadcastUpdate(ACTION_DATA_AVAILABLE,  mCharacteristric);
            }
        }}
        @Override// Callback triggered as a result of a remote characteristic notification.
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            ibroadcastUpdate(Constants.ADVERTISER_ITEM_ADD, characteristic);
        }
    };
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

    /*內部廣播溝通*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*回報給綁定活動---狀態更新*/
    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    /*-----Android----------------------------------------------------------------------------------------------------------------------------*/
    /*回報給綁定活動---特徵內容讀取*/
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

            final byte[] data = characteristic.getValue(); //取出從remote device讀到的數據
            if (data[0]==1) { //沒有更新

            }
            else //有更新 尚未讀取
                intent.putExtra(EXTRA_DATA, true);
        sendBroadcast(intent);
    }
    /*回報給綁定活動---描述內容讀取*/
    private void broadcastUpdate(final String action, final BluetoothGattDescriptor descriptor)
    {
        Intent intent = new Intent(action);
        byte[] data = descriptor.getValue();
        String s=new String(data);
        UUID uuid=descriptor.getUuid();
        Log.i(TAG, s);
       // String getContent()
        if (uuid.equals(GattService.Descriptor_content_UUID)){
            Log.i(TAG, "Reading content");
           intent.putExtra(EXTRA_DATA,s);//
        }
        // Long getTime()
        else{
            Log.i(TAG, "Reading content");
            String tmp=new String(data);
            Long n=Long.parseLong(tmp);
            intent.putExtra(EXTRA_DATA, n);
        }

        sendBroadcast(intent);
    }
    /*-----iOS----------------------------------------------------------------------------------------------------------------------------------*/
    /*回報給綁定活動---特徵內容讀取*/
    private void ibroadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        String EOM="EOM";
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue(); //取出從remote device讀到的數據
        String s=new String(data);
            intent.putExtra(EXTRA_DATA, s);
        if(data[data.length-3]==EOM.getBytes()[0] && data[data.length-2]==EOM.getBytes()[1] && data[data.length-1]==EOM.getBytes()[2])
                disconnect();
        sendBroadcast(intent);
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/


    /*對GATTP內定義操作*/
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/
    /*讀取特徵值*/
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    /*讀取描述值*/
    public void readDescriptor(BluetoothGattDescriptor descriptor) {
        mBluetoothGatt.readDescriptor(descriptor);
    }
    /*寫入特徵值*/
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    /*讀取描述值*/
    public void writeDescriptor(BluetoothGattDescriptor descriptor) {
        mBluetoothGatt.writeDescriptor(descriptor);
    }
    /*設定特徵值關注*/
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
    public BluetoothGattService getChatGattServices(UUID gatt)
    {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getService(gatt);
    }
}
