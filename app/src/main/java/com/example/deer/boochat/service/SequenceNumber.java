package com.example.deer.boochat.service;

import com.example.deer.boochat.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

/**n
 * Created by deer on 2015/12/3.
 */
public class SequenceNumber {

    private int pointer;
    private Map<String,Integer> NamelocalNumMap=new HashMap<String,Integer>();
    private Map<Integer,String> localNumNameMap=new HashMap<Integer,String>();
    private List<Map<String,Object>> routerTable=new ArrayList<>();

    public SequenceNumber()
    {
        pointer=1;
        NamelocalNumMap.put(Constants.BuildSERIAL, add(0, Constants.BuildSERIAL, 0, null));
    }

    public void setPointer(int a)
    {
        pointer=a;
    }
    public byte createLocalNum(String name)
    {
        pointer++;
        NamelocalNumMap.put(name,pointer);
        return (byte)pointer;
    }
    public void addLocalNum(String name, int i)
    {
        setPointer(i);
        localNumNameMap.put(i,name);
    }
    public void addLocalNum(int num,String address)
    {
        String name=localNumNameMap.get(num);
        int me=(int)routerTable.get(NamelocalNumMap.get(Constants.BuildSERIAL)).get(Constants.LOCALNUM);
        Byte[] b={(byte)me};
        int index=add(num,address,1,b);
        NamelocalNumMap.remove(name);
        NamelocalNumMap.put(address,index);
    }
    public String getNamebyLN(int ln)
    {
        return localNumNameMap.get(ln);
    }

    public int getLocalNum()
    {
        return (int)routerTable.get(NamelocalNumMap.get(Constants.BuildSERIAL)).get(Constants.LOCALNUM);
    }
    public void updateRouterTable(int num,String address)
    {
        int index=NamelocalNumMap.get(address);
        routerTable.get(index).remove(Constants.LOCALNUM);
        routerTable.get(index).put(Constants.LOCALNUM, num);

    }
    private int add(int num,String serial,int count,Byte[] path)
    {
        int index;
        Map<String,Object> map=new LinkedHashMap<>();
        map.put(Constants.LOCALNUM,num); //此次的區域碼
        map.put(Constants.DEVICE_ADDRESS,serial); //該裝置的裝置序號
        map.put(Constants.COUNT,count); //經過的距離
        map.put(Constants.PATH, path); //路徑規劃
        routerTable.add(map);
        index=routerTable.indexOf(map);
        return index;

    }

    public void clear()
    {
        localNumNameMap.clear();
        NamelocalNumMap.clear();
        routerTable.clear();
    }

}
