package com.example.deer.boochat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

/**
 * Created by deer on 2015/8/24.
 */
public class SplashScreen extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash2);

        VideoView View = (VideoView) findViewById(R.id.video); //設定一個 view
        View.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.black1_1));
        View.requestFocus();
        View.start();
        //Object vSource = "file:///android_asset/black1_1.mp4";

        /* 設定VideoView的OnCompletionListener */
        View.setOnCompletionListener(new MediaPlayer
                .OnCompletionListener() {
            public void onCompletion(MediaPlayer arg0) {

                Intent intent = new Intent();
                intent.setClass(SplashScreen.this,CheckFirst.class); //這邊是撥完後要轉去哪個layout
                startActivity(intent);
                SplashScreen.this.finish();
            }
        });

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
