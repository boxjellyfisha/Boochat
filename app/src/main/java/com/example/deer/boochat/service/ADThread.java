package com.example.deer.boochat.service;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.net.URL;

/**
 * Created by deer on 2015/10/22.
 要使用 AsyncTask，必定要建立一個繼承自 AsyncTask 的子類別，並傳入 3 項資料：
 Params -- 要執行 doInBackground() 時傳入的參數，數量可以不止一個
 Progress -- doInBackground() 執行過程中回傳給 UI thread 的資料，數量可以不止一個
 Rsesult -- 傳回執行結果， 若您沒有參數要傳入，則填入 Void (注意 V 為大寫)。

 AsyncTask 的運作有 4 個階段：
 onPreExecute -- AsyncTask 執行前的準備工作，例如畫面上顯示進度表，
 doInBackground -- 實際要執行的程式碼就是寫在這裡，
 onProgressUpdate -- 用來顯示目前的進度，
 onPostExecute -- 執行完的結果 - Result 會傳入這裡。

 * AsyncTask 必須在 UI 主執行緒載入(JELLY_BEAN 版本開始會自動執行此事)。
 * 必須在 UI 主執行緒建立 AsyncTask。
 * 必須在 UI 主執行緒呼叫 AsyncTask.execute()。
 * 不要自行呼叫 onPreExecute()，onPostExecute()，doInBackground()， onProgressUpdate()。
 * AsyncTask 只能執行一次。
 */
public class ADThread extends AsyncTask<URL, Integer, Long> {

    protected Long doInBackground(URL... urls)
    {
        long totalSize = 0;
        return totalSize;
    }

    protected void onProgressUpdate(Integer... progress)
    {
        // 這裡接收傳入的 progress 值, 並更新進度表畫面
        // 參數是 Integer 型態的陣列
        // 但是因為在 doInBackground() 只傳一個參數
        // 所以以 progress[0] 取得傳入參數

    }

    protected void onPostExecute(Long result)
    {

    }

}

