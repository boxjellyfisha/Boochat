package com.example.deer.boochat;

import android.os.ParcelUuid;


/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     *
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     *
     */
    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid Chat_Service_UUID = ParcelUuid
            .fromString("2bc2eba8-5942-11e5-885d-feff819cdc9f");

    public static final ParcelUuid IOS_Service_UUID = ParcelUuid
            .fromString("E20A39F4-73F5-4BC4-A12F-17D1AD07A961");
    public static final ParcelUuid IOS_Character_UUID = ParcelUuid
            .fromString("08590F7E-DB05-467E-8757-72F6FAEB13D4");

    public static final ParcelUuid IOS_ADDService_UUID = ParcelUuid
            .fromString("F5096C15-A391-47EC-8C14-3D4F88C0FE4A");
    public static final ParcelUuid IOS_ADDCharacter_UUID = ParcelUuid
            .fromString("A1B78907-E60B-46E5-8ED6-76C42A5AE6BF");
    /*2016.3.9  new iOS uuid*/
    public static final ParcelUuid IOS_Detect_UUID = ParcelUuid
            .fromString("2CD088F1-9809-4077-A0B7-07CB9D992318");
    public static final ParcelUuid IOS_EchoService_UUID = ParcelUuid
            .fromString("68D3E367-87DB-4FE5-BADD-5D6070ED619C");
    public static final ParcelUuid IOS_PublicService_UUID = ParcelUuid
            .fromString("ACF3DEE0-75F9-4381-8EF3-1859C8A8BCCA");
    public static final ParcelUuid IOS_PublicCharacter_UUID = ParcelUuid
            .fromString("49F83D40-5722-4DBF-9215-71D30D539589");

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int RESULT_FROM_P0101=2;
    public static final int STOP_CATTING=3;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //再傳遞參數時 透過此變數 來對應包裝後的資料要給誰用(往何處進行下一步)
    public static final String FRAGMENT_CHANGE="WHICH_NEED_CHANGE";
    public static final int SCANNER_FRAGMENT_CHANGE =  1;
    public static final int ADVERTISER_FRAGMENT_CHANGE =  2;
    public static final int CHAT_FRAGMENT_CHANGE = 3;
    public static final int BLEGATT_CONNECT_REQUEST = 4;


    public static final String SENDING="WHICH_NEED_AD";
    public static final String SENDING_ERROR="sendingerror";
    public static final int FROM_CHATROOM =  1;
    public static final int FROM_PUBLIC =  2;
    public static final int FROM_SCANNER =  3;
    public static final int FROM_IOS =  4;


    public static final String SCANNER_SERVICE_CHANGE ="scanner_service";
    public static final String ADVERTISER_SERVICE_CHANGE ="advertiser_service";
    public static final String ADVERTISER_ITEM_ADD ="advertiser_item";
    public static final String IOS_CONNECT_REQUEST = "ios_connect";
    public static final String IOS_DETECT_REQUEST = "ios_detect";
    //之後要控制掃描的固定值
    public static final int STOP_SCANNING =  1;
    public static final int START_SCANNING = 2;

    //存放 廣播包內容 (Service Data)
    public static final String ADVERTISER_DATA =  "ADVERTISER_DATA";
    //控制可以傳送 (在傳遞給對方成功後，才再次開放)
    public static final String SEND_BTN_ENABLE =  "SEND_BTN_ENABLE";
    //操作控制碼
    public static final String OP_CODE ="opcode";
    //0x01 0x02包使用到
    public static final String LOCAL_NUM ="set_local_number";
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name_list";
    public static final String DEVICE_ADDRESS = "device_random_address";
    public static final String DEVICE_SERIAL = "device_address";
    public static final String DEVICE_ID = "device_id";

    public static final String FORWARD = "forward";
    public static final String FORWARD_SENDER = "forward_sender";
    public static final String FORWARD_RECEIVER = "forward_receiver";

    public static final String COUNT = "data_total_size";

    public static final String POPUP_WINDOW_IS_SHOWING = "popupWindow_isShowing";
    //裝置序號
    public static final String BuildSERIAL = android.os.Build.SERIAL;
    public static final byte[] sendSERIAL = BuildSERIAL.getBytes();  //用來傳送的

    public static final String ACCEPT_TIME = "accpt_time";

    //暫存封包
    public static final String TIME = "time"; //寫下訊息時間
    public static final String MSGID = "msgid"; //寄件人寫給該收件人的第幾封信
    public static final String SENDER = "sender";  //寄件人
    public static final String RECEIVER = "receiver"; //收件人
    public static final String OPCODE = "opcode"; //此訊息類型
    public static final String CONTENT = "content"; //此訊息的主要內容
    public static final String PRIORITY = "priority"; //此訊息的優先等級
    public static final String LOCALNUM = "localnum";//寄件人的區域代號
    public static final String OSTYPE = "os";  //此訊息屬於哪種裝置
    public static final String LYAOUTTYPE = "type"; //此訊息的排版類型
    public static final String PATH = "path"; //此訊息的路徑規劃

    /*---------------      Database table name        ------------------------------------------------------------------------------------------*/
    public static final String TABLE_NAME = "device_friend";
    // 表格名稱     廣播傳訊之佇列 暫存表
    public static final String AD_TABLE = "AD_Packet_Queue";
    public static final String AD_MSG = "AD_Packet_Message";

    public static final String SC_TABLE = "SC_Packet_Queue";
    public static final String SC_MSG = "SC_Packet_Message";
    // 表格名稱     連線傳輸訊息之佇列 暫存表   /M3 M4 基本上不會用到
    public static final String CONN_TABLE = "Conn_Packet_Queue";
    // 表格名稱     IOS傳輸用訊息之佇列 暫存表
    public static final String CONN_IOS_TABLE = "IOS_Packet_Queue";
    /*-------------------------------------------------------------------------------------------------------------------------------------------*/

}
