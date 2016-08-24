package com.example.deer.boochat.tab_fragment;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.deer.boochat.MainActivity;
import com.example.deer.boochat.R;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by deer on 2015/8/20.
 */
public class SettingFragment extends Fragment {
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton;
    private MainActivity mCallback;
    private Handler handler; //ui thread helper
    private DisplayMetrics dm;
    private ImageView g;
    private FrameLayout Fl;
    private ImageButton[] btn;
    private TextView textWho;
    private Map<Integer,String> m;

    public static SettingFragment newInstance()
    {
        SettingFragment f=new SettingFragment();
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback=(MainActivity)getActivity();
        dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        btn=new ImageButton[20];
        textWho=new TextView(getActivity());
        m=new HashMap<>();

        //Looper.getMainLooper()
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test, container, false);
        Fl = (FrameLayout)view.findViewById(R.id.planet);
        g = (ImageView)view.findViewById(R.id.findfriend);
        floatingActionButton=(FloatingActionButton)view.findViewById(R.id.fab);
        if(textWho.getParent()!=null)
        Fl.addView(textWho);
        /**ImageView img=(ImageView)view.findViewById(R.id.glidetest);
        Glide.with(getContext())
                .load(R.drawable.an)
                .asGif()
                .into(img);**/
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        //webview=(WebView)view.findViewById(R.id.webView);
        //webview.loadUrl("file:///android_asset/an.html");
        floatingActionButton.setOnClickListener(new FloatingActionButton.OnClickListener() {

            boolean isClick = true;

            @Override
            public void onClick(View view) {
                int x = floatingActionButton.getLeft();
                int y = floatingActionButton.getTop();

                if (isClick == true) {
                    g.setPadding(0, 0, -400, -100);
                    g.setVisibility(View.VISIBLE);
                    Animator mAnimator = ViewAnimationUtils.createCircularReveal(g, x + 120, y + 120, 0, g.getHeight());
                    mAnimator.setDuration(1000);
                    mAnimator.setInterpolator(new AccelerateInterpolator());
                    mAnimator.start();
                    isClick = false;
                    addButton(0);
                    addButton(1);
                    addButton(2);
                    rotate();
                } else {
                    removeButton();
                    m.clear();
                    g.setVisibility(View.GONE);
                    isClick = true;
                }
            }
        });

    }
    public void rotate() {
        Animation rotate= AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        floatingActionButton.startAnimation(rotate);
    }
    public void addButton(int i)
    {
            if(btn[i]==null)
                btn[i]=new ImageButton(getActivity());
            setButton(btn[i]);
            btn[i].setLabelFor(i);
            m.put(btn[i].getLabelFor(),"test"+i);
    }
    private void setButton(ImageButton b)
    {
        int[] posi= new int[2];
        g.getLocationOnScreen(posi);
        b.setBackground(getResources().getDrawable(background(), null));
        b.setScaleX((float) 0.1);
        b.setScaleY((float) 0.08);
        b.setScaleType(ImageView.ScaleType.FIT_XY);
        b.setX(posi[0]+(int)(Math.random() *8-4)*100);//  (Math.random() * (dm.widthPixels - 200)-500)1440
        b.setY(posi[1]+(int)(Math.random() *11-10)*100);//(Math.random() * (dm.heightPixels - 200)-500)2360
        b.setOnClickListener(new onClickListener());
        Fl.addView(b);
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(b, b.getWidth() / 2, b.getHeight() / 2, 0, b.getHeight());
        mAnimator.setDuration(800);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.start();
    }
    private int background()
    {
        int srcNumber=(int)(Math.random() *8+1);
        switch(srcNumber)
        {
            case 1:
                srcNumber=R.drawable.nw1;
                break;
            case 2:
                srcNumber=R.drawable.nw2;
                break;
            case 3:
                srcNumber=R.drawable.nw3;
                break;
            case 4:
                srcNumber=R.drawable.nw4;
                break;
            case 5:
                srcNumber=R.drawable.nw5;
                break;
            case 6:
                srcNumber=R.drawable.nw6;
                break;
            case 7:
                srcNumber=R.drawable.nw7;
                break;
            case 8:
                srcNumber=R.drawable.nw8;
                break;
            case 9:
                srcNumber=R.drawable.nw9;
                break;

        }
        return srcNumber;
    }
    public class onClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            Fl.removeView(textWho);
            int[] posi= new int[2];
            view.getLocationOnScreen(posi);
            textWho.setText(m.get(view.getLabelFor()) + " /id " + view.getId());
            textWho.setBackground(getResources().getDrawable(R.drawable.txtc, null));
            textWho.setX(posi[0]);
            textWho.setY(posi[1]-500);
            Fl.addView(textWho);
            ViewGroup.LayoutParams params = textWho.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            textWho.setLayoutParams(params);

        }
    }
    private void removeButton()
    {
            Fl.removeAllViews();
    }
}

/**Intent intent=new Intent(getActivity(), FindFriendsActivity.class);
 Bundle bundle_p = new Bundle();
 bundle_p.putInt("x", floatingActionButton.getScrollX());
 bundle_p.putInt("y", floatingActionButton.getScrollY());
 intent.putExtras(bundle_p);
 startActivity(intent);**/

//g.setScrollX(floatingActionButton.getScrollX()-610);
//g.setScrollY(floatingActionButton.getScrollY()-150);