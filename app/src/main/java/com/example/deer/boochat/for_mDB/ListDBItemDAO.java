package com.example.deer.boochat.for_mDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.deer.boochat.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deer on 2015/8/17.  用來拿取資料的各種功能
 */

       // 資料功能類別
public class ListDBItemDAO {

    // 表格名稱   
    public static final String TABLE_NAME = "device_friend";
    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";
    // 其它表格欄位名稱
    public static final String DATETIME_COLUMN="datetime";
    public static final String DEVICE_NAME_COLUMN = "name";    //藍芽裝置名稱
    public static final String DEVICE_SERIAL_NUMBER_COLUMN = "serialNumber";//裝置序號
    public static final String CHATROOM_ISALREADY_COLUMN="chatroom";//與該方是否已有建立聊天紀錄表
    public static final String OS_COLUMN = "deviceOS";//代申請方->換成放置os類型 0: android 1: iOS

            // 使用上面宣告的變數建立表格的SQL指令
            public static final String CREATE_TABLE =
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            DATETIME_COLUMN + " INTEGER NOT NULL, " +
                            DEVICE_NAME_COLUMN + " TEXT NOT NULL, " +
                            DEVICE_SERIAL_NUMBER_COLUMN + " TEXT NOT NULL, " +
                            OS_COLUMN + " INTEGER , " +
                            CHATROOM_ISALREADY_COLUMN + " INTEGER)";

    public String RECORD_TABLE_NAME;//與OO的聊天室table
    public static final String SENDER_COLUMN="sender";//發迅人
    public static final String TIME_COLUMN = "time";//發訊時間
    public static final String CONTENT_COLUMN="content"; //發訊內容
    public static final String SEND_DONE_COLUMN="beingSending";//是否發送完成並成功

   // 資料庫物件   
    private SQLiteDatabase db;
    // 建構子
    public ListDBItemDAO(Context context) {
            db = ListDB.getDatabase(context);
        //db.execSQL("DROP TABLE IF EXISTS "+ListDBItemDAO.TABLE_NAME);
        db.execSQL(ListDBItemDAO.CREATE_TABLE);
        }

    public void dropTable(String name)
    {
        db.execSQL("DROP TABLE IF EXISTS "+name);
    }
    // 關閉資料庫
    public void close() {
    db.close();
    }

    //建新的資料表 聊天對象OO的 聊天紀錄table
    public void setCreateTable(String name)
    {
        RECORD_TABLE_NAME=name;
        String CREATE_RECORD_TABLE=
                "CREATE TABLE IF NOT EXISTS " + RECORD_TABLE_NAME + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SENDER_COLUMN + " TEXT NOT NULL," +
                        TIME_COLUMN + " INTEGER NOT NULL," +
                        CONTENT_COLUMN + " TEXT,"+
                        SEND_DONE_COLUMN + " INTEGER NOT NULL)";
        db.execSQL(CREATE_RECORD_TABLE);
    }

    //新增聊天紀錄
    public ListItem record(ListItem item,String tableName){
        RECORD_TABLE_NAME=tableName;
        ContentValues cvr=new ContentValues();
        cvr.put(SENDER_COLUMN,item.getSerial());
        cvr.put(TIME_COLUMN,item.getDatetime());
        cvr.put(CONTENT_COLUMN, item.getContent());
        cvr.put(SEND_DONE_COLUMN,item.getIsAlready());
        long id = db.insert(RECORD_TABLE_NAME, null, cvr);
        // 設定編號
        item.setId(id);
        return item;
    }

    // 新增參數指定的物件
    public ListItem insert(ListItem item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(DEVICE_NAME_COLUMN, item.getDeviceName());
        cv.put(DEVICE_SERIAL_NUMBER_COLUMN, item.getSerial());
        cv.put(OS_COLUMN, item.getOSType());
        cv.put(CHATROOM_ISALREADY_COLUMN,item.getIsAlready());

                // 新增一筆資料並取得編號
                // 第一個參數是表格名稱
                // 第二個參數是沒有指定欄位值的預設值
                // 第三個參數是包裝新增資料的ContentValues物件
                long id = db.insert(TABLE_NAME, null, cv);
                // 設定編號
                item.setId(id);
                // 回傳結果
                return item;
       }
    public void deleteAll(String tablename){
        db.delete(tablename, null, null);
    }

    // 修改參數指定的物件
     public boolean update(ListItem item) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料       
         cv.put(DATETIME_COLUMN, item.getDatetime());
         cv.put(DEVICE_NAME_COLUMN, item.getDeviceName());
         cv.put(DEVICE_SERIAL_NUMBER_COLUMN, item.getSerial());
         cv.put(OS_COLUMN, item.getOSType());
         cv.put(CHATROOM_ISALREADY_COLUMN,item.getIsAlready());
        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
         String where = KEY_ID + "=" + item.getId();
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
     }

    // 修改參數指定訊息的傳送狀態 0:尚未傳送 1:傳送成功
    public boolean updateState(long id,String tableName,int state) {

        ListItem item=get(id,tableName);
        RECORD_TABLE_NAME=tableName;
        ContentValues cv = new ContentValues();
        cv.put(SENDER_COLUMN,item.getSerial());
        cv.put(TIME_COLUMN,item.getDatetime());
        cv.put(CONTENT_COLUMN, item.getContent());
        cv.put(SEND_DONE_COLUMN,state);

        String where = KEY_ID + "=" + item.getId();
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(RECORD_TABLE_NAME, cv, where, null) > 0;
    }


    // 刪除參數指定編號的資料
     public boolean delete(long id){
         // 設定條件為編號，格式為「欄位名稱=資料」
         String where = KEY_ID + "=" + id;
         // 刪除指定編號資料並回傳刪除是否成功
         return db.delete(TABLE_NAME, where, null) > 0;
     }

    public boolean delete(long id,String tableName){
        RECORD_TABLE_NAME=tableName;
        String where = KEY_ID + "=" + id;
        return db.delete(RECORD_TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
     public List<ListItem> getAll() {
            List<ListItem> result = new ArrayList<>();
            Cursor cursor = db.query(
             TABLE_NAME, null, null, null, null, null, null, null);

         while (cursor.moveToNext()) {
             result.add(getRecord(cursor));
            }
        cursor.close();
        return result;
     }
    public List<ListItem> getAll(String tableName) {
        List<ListItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                tableName, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecordtxt(cursor));
        }
        cursor.close();
        return result;
    }
    // 讀取特定記事資料
    public List<ListItem> getFriends(String[] s) {

        List<ListItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, s, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        cursor.close();
        return result;
    }
    public boolean FriendFilter(String serial)
    {
        boolean b=false;
        String where=DEVICE_SERIAL_NUMBER_COLUMN + "=" + "'" + serial + "'";
        Cursor cursor = db.query(
                TABLE_NAME,null ,where, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            b=true;
        }
        // 關閉Cursor物件
        cursor.close();
        return b;
    }

    public List<Map<String,Object>> getLatest() {

        List<Map<String,Object>> result = new ArrayList<>();
        List<String> tmp=new ArrayList<>();
        List<String> tmp2=new ArrayList<>();
        String[] s={DEVICE_NAME_COLUMN,DEVICE_SERIAL_NUMBER_COLUMN};
        String where = CHATROOM_ISALREADY_COLUMN+ "=" + 1;
        Cursor cursor = db.query(
                TABLE_NAME,s, where, null, null, null, null, null);

        while (cursor.moveToNext()) {
            tmp2.add(cursor.getString(0));
            tmp.add(cursor.getString(1));
        }
        cursor.close();
        for(int i=0;i<tmp.size();i++)
        {
            cursor = db.query(
                    tmp.get(i),null, null, null, null, null, null, null);

            if (cursor.moveToLast()) {
                Map<String,Object> map=new HashMap<>();
                map.put("result",getRecordtxt(cursor));
                map.put("serial",tmp.get(i));
                map.put("name",tmp2.get(i));

                result.add(map);
            }
            cursor.close();
        }

        return result;
    }


    // 取得指定編號的資料物件
     public ListItem get(long id) {
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
    public ListItem get(long id,String tableName) {

        RECORD_TABLE_NAME=tableName;
        // 準備回傳結果用的物件
        ListItem item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                RECORD_TABLE_NAME, null, where, null, null, null, null, null);

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
    public ListItem getRecordtxt(Cursor cursor) {
        // 準備回傳結果用的物件
        ListItem result = new ListItem();

        result.setId(cursor.getLong(0));
        result.setSerial(cursor.getString(1));
        result.setDatetime(cursor.getLong(2));
        result.setContent(cursor.getString(3));
        result.setIsAlready(cursor.getInt(4));
        // 回傳結果
        return result;

    }

    // 取得資料數量
    public int getCountByName(String name) {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + name, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }
    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    // 建立範例資料
    public void sample() {

        ListItem item = new ListItem(0, new Date().getTime(), "Kelly", "fake01friend",0,0);
        insert(item);
        setCreateTable("fake01friend");
        ListItem item1 = new ListItem(0, new Date().getTime(), "Alice", "fake02friend",0,0);
        insert(item1);
        setCreateTable("fake02friend");
        ListItem item2 = new ListItem(0, new Date().getTime(), "Cyril", "fake03friend",0,0);
        insert(item2);
        setCreateTable("fake03friend");
        ListItem item3 = new ListItem(0, new Date().getTime(), "Gerge", "fake04friend",0,0);
        insert(item3);
        setCreateTable("fake04friend");
        ListItem item4 = new ListItem(0, new Date().getTime(), "Liliane", "fake05friend",0,0);
        insert(item4);
        setCreateTable("fake05friend");
    }

}