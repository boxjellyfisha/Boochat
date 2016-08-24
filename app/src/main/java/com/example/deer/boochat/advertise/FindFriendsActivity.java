package com.example.deer.boochat.advertise;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.deer.boochat.Constants;
import com.example.deer.boochat.R;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by deer on 2015/12/12.
 */
public class FindFriendsActivity extends Activity {

    private int x;
    private int y;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.find_planet);
        //DisplayMetrics dm = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);
        //int vWidth = dm.widthPixels;
        ImageView g = (ImageView)findViewById(R.id.imageP);
        //g.getLayoutParams().height=vWidth;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            x = bundle.getInt("x");
            y = bundle.getInt("y");
        }

        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animator mAnimator = ViewAnimationUtils.createCircularReveal(v, v.getWidth()/2,v.getHeight()/2 , 0, v.getHeight());
                mAnimator.setDuration(2000);
                mAnimator.setInterpolator(new AccelerateInterpolator());
                mAnimator.start();
            }
        });


    }
    public void rotateyAnimRun(final View view)
    {
        ObjectAnimator anim = ObjectAnimator//
                .ofFloat(view, "zhy", 1.0F,  0.0F)//
                .setDuration(500);//
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float cVal = (Float) animation.getAnimatedValue();
                view.setAlpha(cVal);
                view.setScaleX(cVal);
                view.setScaleY(cVal);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
