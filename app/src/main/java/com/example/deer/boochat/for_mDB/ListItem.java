package com.example.deer.boochat.for_mDB;

/**
 * Created by deer on 2015/8/18. 放入資料庫的自訂義物件
 */public class ListItem {
    private int number;
    private int ostype;
    private int isAlready;
    private int opcode;
    private int length;
    private long id;
    private long time;
    private long msgid;
    private long agency;
    private String name;
    private String address;
    private String data;

    ListItem(){}

    /*--------------     for Chat room           --------------------------------------------------------------------------------------------*/
    public ListItem(String Address,long Time,String Data, int IsAlready)
    {
        address=Address;
        time=Time;
        data=Data;
        isAlready=IsAlready;
    }

    /*--------------     for Device Name      ---------------------------------------------------------------------------------------------*/
    public ListItem(long Id, long Time, String Name, String Address, int Number, int IsAlready){

        id=Id;
        time=Time;
        name=Name;
        address=Address;
        ostype=Number;
        isAlready=IsAlready;
    }

    /*--------------     for Advertising Tmp      ---------------------------------------------------------------------------------------------*/
    /*--------------     for Connected  Tmp      ---------------------------------------------------------------------------------------------*/
    /*--------------     for IOS Device Tmp      ---------------------------------------------------------------------------------------------*/
    public ListItem(long Time, String Sender, String Address,int DeviceType,int IsAlready){

        time=Time;
        name=Sender; //sender
        address=Address; //receiver
        ostype=DeviceType; //android: 01 ios: 02 other: 03
        isAlready=IsAlready; //是否已傳送完成

    }

    public ListItem(long msg,int Opcode,int Size,int Number, long Agency, String content){

        msgid=msg; //本機的訊息編號 訊息table_id
        opcode=Opcode; //M1操作代碼 訊息類型
        length=Size; //M2整群數量
        number=Number; //M3自己的代碼
        agency=Agency; //MSG id
        data=content;

    }

    /*--------------           Set            ---------------------------------------------------------------------------------------------*/
    public void setId(long i){
        id=i;
    }
    public void setDatetime(long t){
        time=t;
    }
    public void setDeviceName(String n){
        name=n;
    }
    public void setSerial(String a){
        address=a;
    }
    public void setMsgId(long t){
        msgid=t;
    }
    public void setOpcode(int o){
        opcode=o;
    }
    public void setPLength(int s){
        length=s;
    }
    public void setOSType(int os){
        ostype=os;
    }
    public void setLocalNo(int n){
        number=n;
    }
    public void setAgency(long a){
        agency=a;
    }
    public void setContent(String d){data=d;}
    public void setIsAlready(int f){isAlready=f;}
    /*--------------           Get              ---------------------------------------------------------------------------------------------*/
    public long getId(){
        return id;
    }
    public long getMsgId(){
        return msgid;
    }
    public long getDatetime(){
        return time;
    }
    public String getDeviceName(){
        return name;
    }
    public String getSerial(){
        return address;
    }

    public int getOpcode(){
        return opcode;
    }
    public int getPLength(){
        return length;
    }
    public int getOSType(){return ostype;}
    public int getLocalNo(){
        return number;
    }
    public long getAgency(){
        return agency;
    }
    public String getContent(){ return data; }
    public int getIsAlready(){
        return isAlready;
    }

    public void clear()
    {
        agency=0;
        number=0;
        isAlready=0;
        id=0;
        time=0;
        name="";
        address="";
        data="";
    }
}
