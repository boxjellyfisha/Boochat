package com.example.deer.boochat.chat_room;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.example.deer.boochat.for_mDB.ListItem;
import com.example.deer.boochat.service.BluetoothLeService;
import com.example.deer.boochat.service.GattService;
import com.example.deer.boochat.service.IOSGattService;

import java.util.UUID;

/**
 * Created by deer on 2015/10/16.
 */
public class MessageReader {

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattService mChatService;
    private BluetoothGattCharacteristic mCheckMail;
    private BluetoothGattDescriptor mTime;
    private BluetoothGattDescriptor mContent;
    private ListItem cache;

    public MessageReader(BluetoothLeService mBluetoothLeService)
    {
        this.mBluetoothLeService=mBluetoothLeService;
        cache=new ListItem(null,0,null,1);
    }
    public void setUpIOS()
    {
        mChatService=mBluetoothLeService.getChatGattServices(IOSGattService.IOS_PublicService_UUID);
        mCheckMail=mChatService.getCharacteristic(IOSGattService.IOS_PublicCharacter_UUID);//
        mBluetoothLeService.readCharacteristic(mCheckMail);
        mBluetoothLeService.setCharacteristicNotification(mCheckMail,true);
        BluetoothGattDescriptor descriptor = mCheckMail
                .getDescriptor(UUID
                        .fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
            mBluetoothLeService.writeDescriptor(descriptor);
        } mBluetoothLeService.readCharacteristic(mCheckMail);
    }
    public void setUp()
    {
        mChatService=mBluetoothLeService.getChatGattServices(GattService.Chat_Service_UUID);
        mCheckMail=mChatService.getCharacteristic(createUUID(Constants.BuildSERIAL));//
        mContent=mCheckMail.getDescriptor(GattService.Descriptor_content_UUID);
        mTime=mCheckMail.getDescriptor(GattService.Descriptor_time_UUID);

        mBluetoothLeService.readCharacteristic(mCheckMail);
    }
    public void read(int a)
    {
        if(a==0)//讀內容
            mBluetoothLeService.readDescriptor(mContent);
        else//讀時間
            mBluetoothLeService.readDescriptor(mTime);
    }
    public void setAddress(String address)
    {
        cache.setSerial(address);
    }
    public void setCache(String data)
    {
        cache.setContent(data);
    }
    public void setCache(Long time)
    {
        cache.setDatetime(time);
    }
    public ListItem getCache()
    {
        return cache;
    }
    public void alreadyRead()
    {
        byte[] a={1};
        mCheckMail.setValue(a);
        mBluetoothLeService.writeCharacteristic(mCheckMail);
        //mBluetoothLeService.disconnect();
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
