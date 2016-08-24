package com.example.deer.boochat.tab_fragment;

        import android.bluetooth.BluetoothAdapter;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.drawable.BitmapDrawable;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentActivity;
        import android.view.Gravity;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.EditorInfo;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.GridView;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.PopupWindow;
        import android.widget.SimpleAdapter;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.deer.boochat.MainActivity;
        import com.example.deer.boochat.R;
        import com.example.deer.boochat.adapter.ConversationAdapter;
        import com.example.deer.boochat.adapter.StickerAdapter;
        import com.example.deer.boochat.chat_room.BluetoothChatActivity;
        import com.example.deer.boochat.service.AdvertiserService;
        import com.example.deer.boochat.service.ScannerService;

        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;

/**
 * Created by deer on 2015/8/20.
 */
public class PublicFragment extends Fragment {

    private EditText editText;
    private String mConnectedDeviceName = null;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_ADVERTISE_DATA = "ADVERTISE_DATA";
    public static final String EXTRAS_PACKAGE_DATA = "PACKAGE_DATA";

    private ScannerService mScanner;
    private AdvertiserService mAdvertiser;
    private MainActivity mCallback;

    // Layout Views
    private PopupWindow popupWindow;
    private ListView mConversationView;
    private EditText mOutEditText;
    private ImageView mIcon;
    private Button mSendButton;
    private Button mSticker;
    String mDeviceName;
    String mDeviceAddress;
    String mData;
    String mPackage;
    Boolean Send_btn=false;  //表示現在的發送狀態

    private ConversationAdapter mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private StickerAdapter mStickerAdapter;
    private GridView mGridView;   //MyGridView
    //定義圖標數組
    private int[] imageRes = { R.drawable.addfriend };/*, R.drawable.blel3,
    R.drawable.blel5, R.drawable.blel7, R.drawable.blel9, R.drawable.blel10, R.drawable.blel11,
    R.drawable.blel13, R.drawable.blel15, R.drawable.blel17,
    R.drawable.blel19, R.drawable.blel20*/


    public static PublicFragment newInstance()
    {
        PublicFragment f=new PublicFragment();
        return f;
    }
    public void setScanner(ScannerService s)
    {
        mScanner=s;
    }
    public void setAdvertiser(AdvertiserService a)
    {
        mAdvertiser=a;
    }
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mCallback=(MainActivity)getActivity();
        editText = (EditText)getActivity().findViewById(R.id.edit_text_out);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mStickerAdapter = new StickerAdapter(LayoutInflater.from(getActivity()));
        mConversationArrayAdapter = new ConversationAdapter(LayoutInflater.from(getActivity()));

    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat2 session
        }
        setupChat();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        //mSendButton.setEnabled(Send_btn);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_public, container, false);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mSticker = (Button) view.findViewById(R.id.sticker);
    }
    /**
     * Set up the UI and background operations for chat2.
     */
    private void setupChat() {

        // Initialize the array adapter for the conversation thread
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    /*
                    Bundle bundle=new Bundle();
                    bundle.putString(BluetoothChatFragment.EXTRAS_ADVERTISE_DATA, message);
                    mCallback.onSendMessage(bundle);
                    */
                    //對話框上顯示
                    mConversationArrayAdapter.add(0,new Date().getTime(),message,0,0);
                    mConversationArrayAdapter.notifyDataSetChanged();
                    send();
                    //設為不可發送 並清除訊息文字編輯區
                    Send_btn = false;
                    //mSendButton.setEnabled(Send_btn);
                    mOutStringBuffer.setLength(0);
                    mOutEditText.setText(mOutStringBuffer);

                }
            }
        });
        mSticker.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                View v=getView();
                if(v!=null)
                {
                    initPopupWindowView();
                    showUp(v);
                    Toast.makeText(getActivity(), "click",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void initPopupWindowView()
    {

        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_sticker, null, false);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mStickerAdapter.add(imageRes[0]);
        mGridView = (GridView) popupView.findViewById(R.id.gridView);
        mIcon=(ImageView) popupView.findViewById(R.id.test_icon);
        mIcon.setImageResource(R.drawable.addfriend);
        mGridView.setAdapter(mStickerAdapter);
        //为mGridView添加點擊事件監聽器
        mGridView.setOnItemClickListener(new GridViewItemOnClick());

    }
    public class GridViewItemOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
            Toast.makeText(getActivity(), "pic",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void showUp(View parent) {
        popupWindow.setAnimationStyle(R.style.AnimationPopup);
        popupWindow.showAtLocation(parent,Gravity.LEFT |Gravity.BOTTOM,0,400);
    }
    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if ( actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                /*Send_btn &&
                Bundle bundle=new Bundle();
                bundle.putString(EXTRAS_ADVERTISE_DATA, message);
                mCallback.onSendMessage(bundle);
                */
                //對話框上顯示
                mConversationArrayAdapter.add(0,new Date().getTime(),message,1,0);
                mConversationArrayAdapter.notifyDataSetChanged();
                //設定為不可發送 清除訊息文字編輯區
                Send_btn=false;
                //mSendButton.setEnabled(Send_btn);
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);

            }
            return true;
        }
    };

    public void renew(String name,String message){
        mConversationArrayAdapter.setDeviceName(name);
        mConversationArrayAdapter.add(0,new Date().getTime(),message,1,1);
        mConversationArrayAdapter.notifyDataSetChanged();
    }

    public void send()
    {   mCallback.test();
    }
}
