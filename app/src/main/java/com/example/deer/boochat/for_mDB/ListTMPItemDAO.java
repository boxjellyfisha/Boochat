package com.example.deer.boochat.for_mDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by deer on 2015/10/22.
 * 規劃站存表的部分 類似訊息佇列 為避免錯誤發生還能還原
 */
public class ListTMPItemDAO {

    /*--------------------------------------------------------------------------------------------------------------*/

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";
    // 其它表格欄位名稱
    //public static final String DATETIME_COLUMN="datetime";
    public static final String DATETIME_COLUMN="datetime";
    public static final String SENDER_NAME_COLUMN = "name";    //傳送方名稱
    public static final String RECEIVER_SERIAL_NUMBER_COLUMN = "serialNumber";//接收方裝置序號
    public static final String DEVICE_TYPE_COLUMN = "deviceOS";//使用者區域代碼
    public static final String SEND_DONE_COLUMN="beingSending";//是否發送完成並成功
    public static final String MSG_ID = "number";//本機的訊息編號
    public static final String M_1st_COLUMN = "opcode";//opcode message type 為何種格式
    public static final String M_2nd_COLUMN = "length";//封包群總個數
    public static final String M_3rd_COLUMN = "localNum";//
    public static final String M_4th_COLUMN = "msgid";//寄件方編號
    public static final String CONTENT_COLUMN = "content";//該封包之內容

    /*--------------------------------------------------------------------------------------------------------------*/

    // 資料庫物件   
    private SQLiteDatabase db;
    // 建構子
    public ListTMPItemDAO(Context context) {
        db = ListDB.getDatabase(context);
    }
    public void setCreateMSGTable(String name,String item)
    {
        String TABLE=name;
        String ITEM_TABLE=item;
        String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DATETIME_COLUMN + " INTEGER NOT NULL, " +
                        SENDER_NAME_COLUMN + " TEXT NOT NULL, " +
                        RECEIVER_SERIAL_NUMBER_COLUMN + " TEXT, " +
                        DEVICE_TYPE_COLUMN + "INTEGER NOT NULL," +
                        SEND_DONE_COLUMN + " INTEGER NOT NULL," +
                        "FOREIGN KEY("+KEY_ID+") REFERENCES "+ITEM_TABLE+"("+MSG_ID+") ON DELETE CASCADE" +
                        ")";
        db.execSQL(CREATE_TABLE);
    }
    public void setCreateTXTTable(String name)
    {
        String TABLE=name;
        String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MSG_ID + " INTEGER NOT NULL," +
                        M_1st_COLUMN + " INTEGER NOT NULL, " +
                        M_2nd_COLUMN + " INTEGER NOT NULL, " +
                        M_3rd_COLUMN + " INTEGER , " +
                        M_4th_COLUMN + " INTEGER , " +
                        CONTENT_COLUMN + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }


    public void dropTable(String name)
    {
        db.execSQL("DROP TABLE IF EXISTS "+name);
    }
    // 關閉資料庫
    public void close() {
        db.close();
    }
    // 新增參數指定的物件
    public ListItem insert(ListItem item,String TABLE) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(SENDER_NAME_COLUMN, item.getDeviceName());
        cv.put(RECEIVER_SERIAL_NUMBER_COLUMN, item.getSerial());
        cv.put(DEVICE_TYPE_COLUMN, item.getOSType());
        cv.put(SEND_DONE_COLUMN, item.getIsAlready());


        // 新增一筆資料並取得編號
        long id = db.insert(TABLE, null, cv);
        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }

    public ListItem insertItem(ListItem item,String TABLE) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        cv.put(MSG_ID,item.getMsgId());
        cv.put(M_1st_COLUMN,item.getOpcode());
        cv.put(M_2nd_COLUMN,item.getPLength());
        cv.put(M_3rd_COLUMN,item.getLocalNo());
        cv.put(M_4th_COLUMN,item.getAgency());
        cv.put(CONTENT_COLUMN,item.getContent());

        // 新增一筆資料並取得編號
        long id = db.insert(TABLE, null, cv);
        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }

    public void deleteAll(String tableName){
        db.delete(tableName, null, null);
    }
    public void removeDone(String tableName)
    {
        String where = SEND_DONE_COLUMN + "=" + 1;
        db.delete(tableName, where, null);
    }
    // 修改參數指定的物件
    public boolean update(ListItem item,String TABLE) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(SENDER_NAME_COLUMN, item.getDeviceName());
        cv.put(RECEIVER_SERIAL_NUMBER_COLUMN, item.getSerial());
        cv.put(DEVICE_TYPE_COLUMN, item.getOSType());
        cv.put(SEND_DONE_COLUMN, item.getIsAlready());

        // 設定修改資料的條件為編號
        String where = KEY_ID + "=" + item.getId();
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE, cv, where, null) > 0;
    }

    public boolean updateItem(ListItem item,String TABLE) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        cv.put(MSG_ID, item.getMsgId());
        cv.put(M_1st_COLUMN, item.getOpcode());
        cv.put(M_2nd_COLUMN,item.getPLength());
        cv.put(M_3rd_COLUMN,item.getLocalNo());
        cv.put(M_4th_COLUMN, item.getAgency());
        cv.put(CONTENT_COLUMN,item.getContent());

        // 設定修改資料的條件為編號
        String where = KEY_ID + "=" + item.getId();
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE, cv, where, null) > 0;
    }


    // 取得指定編號的資料物件
    public ListItem get(long id,String TABLE_NAME) {
        // 準備回傳結果用的物件
        ListItem item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }
    // 把Cursor目前的資料包裝為物件
    public ListItem getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        ListItem result = new ListItem();

        result.setId(cursor.getLong(0));
        result.setDatetime(cursor.getLong(1));
        result.setDeviceName(cursor.getString(2));
        result.setSerial(cursor.getString(3));
        result.setOSType(cursor.getInt(4));
        result.setIsAlready(cursor.getInt(5));

        // 回傳結果
        return result;
    }

    public ListItem getRecordItem(Cursor cursor) {
        // 準備回傳結果用的物件
        ListItem result = new ListItem();
        result.setId(cursor.getLong(0));
        result.setMsgId(cursor.getInt(1));
        result.setOpcode(cursor.getInt(2));
        result.setPLength(cursor.getInt(3));
        result.setLocalNo(cursor.getInt(4));
        result.setAgency(cursor.getInt(5));
        result.setContent(cursor.getString(6));

        // 回傳結果
        return result;
    }
    // 取得資料數量
    public int getCount(String TABLE_NAME) {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }
    // 取得指定編號的資料物件
    public ListItem getNext(long id,String TABLE_NAME) {
        // 準備回傳結果用的物件
        ListItem item = null;
        // 使用編號為查詢條件
        String where = SEND_DONE_COLUMN + "=" + 0;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 檢查是否有封包在等待
    public boolean isWaiting(String TABLE_NAME) {

        String where = SEND_DONE_COLUMN + "=" + 0;
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        if (result.moveToFirst()) {
            return true;
        }
        return false;
    }
}
