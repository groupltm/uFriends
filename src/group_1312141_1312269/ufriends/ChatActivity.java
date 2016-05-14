package group_1312141_1312269.ufriends;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import group_1312141_1312269.ufriends.ReceiveSocketAsync.SocketReceiverDataListener;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.ufriends.R;

public class ChatActivity extends Activity implements SocketReceiverDataListener{
	
	WifiP2PBroadcast mBroadcast;
	
	ChatArrayAdapter mChatAdapter;
	
	ListView lvChat;
	EditText edtChatIn;
	Button btnSend;
	
	boolean side = false;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		lvChat = (ListView)findViewById(R.id.lvChat);
		edtChatIn = (EditText)findViewById(R.id.edtChatIn);
		btnSend = (Button)findViewById(R.id.btnSend);
		
		mBroadcast = MyBundle.getInstance().mBroadcast;
		mBroadcast.mP2PHandle.setReceiveDataListener(this);
		
		mChatAdapter = new ChatArrayAdapter(this, R.layout.list_chat_left_item);
		
		edtChatIn.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
			}
		});
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
		
//		layout.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				
//				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(edtChatIn.getWindowToken(), 0);
//				
//				return false;
//			}
//		});
		
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				sendChatMessage();
			}
		});
		
		lvChat.setAdapter(mChatAdapter);
		
		mChatAdapter.registerDataSetObserver(new DataSetObserver() {
			
			@Override
			public void onChanged() {
				// TODO Auto-generated method stub
				super.onChanged();
				lvChat.setSelection(mChatAdapter.getCount() - 1);
			}
		});
		
//		MyBundle myBundle = MyBundle.getInstance();
//		mBroadcast = myBundle.mBroadcast;
//		mBroadcast.mP2PHandle.setReceiveDataListener(this);
//		
//		setContentView(R.layout.activity_chat);
//		
//		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
//		
//		lvChat = (ListView)findViewById(R.id.lvChat);
//		edtChatIn = (EditText)findViewById(R.id.edtChatIn);
//		btnSend = (Button)findViewById(R.id.btnSend);
//		
//		mChatAdapter = new ChatArrayAdapter(this, R.layout.list_chat_left_item);
//		
//		edtChatIn.setOnKeyListener(new View.OnKeyListener() {
//			
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    return sendChatMessage();
//                }
//                return false;
//			}
//		});
//		
//		layout.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				
//				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(edtChatIn.getWindowToken(), 0);
//				
//				return false;
//			}
//		});
//		
//		btnSend.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//				sendChatMessage();
//			}
//		});
//		
//		lvChat.setAdapter(mChatAdapter);
//		
//		mChatAdapter.registerDataSetObserver(new DataSetObserver() {
//			
//			@Override
//			public void onChanged() {
//				// TODO Auto-generated method stub
//				super.onChanged();
//				lvChat.setSelection(mChatAdapter.getCount() - 1);
//			}
//		});
	}
	
	private InputStream convertStringToIs(String s){
		InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
		return stream;
	}
	
	private boolean sendChatMessage(){
		String msg = edtChatIn.getText().toString();
		if (!msg.equals("")){
			side = false;
			mChatAdapter.add(new ChatMessage(side, edtChatIn.getText().toString(), false));
	        edtChatIn.setText("");
	        
	        InputStream is = convertStringToIs(msg);
	        mBroadcast.sendMessage(is);
		}
		
		return true;
	}
	
	private boolean sendImageInGallery(){
		
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 100){
			Uri uri = data.getData();
			String realPath;
			if (Build.VERSION.SDK_INT < 11){
				realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getApplicationContext(), uri);
			}else if (Build.VERSION.SDK_INT < 20){
				realPath = RealPathUtil.getRealPathFromURI_API11to19(getApplicationContext(), uri);
			}else {
				realPath = RealPathUtil.getRealPathFromURI_API20(getApplicationContext(), uri);
			}
			mChatAdapter.add(new ChatMessage(false, realPath, true));
			
			ContentResolver cr = getContentResolver();
            InputStream is;

            try {
				is = cr.openInputStream(uri);
				mBroadcast.sendImage(is);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String createImageWithData(byte[] data) {

        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        os.write(data, 0, data.length);

        try {
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + getApplicationContext().getPackageName() + "/wifip2pImageShare-" + System.currentTimeMillis()
                    + ".jpg");

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();
            os.writeTo(new FileOutputStream(f));
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	@Override
	public void onReceiveMessageData(byte[] data) {
		// TODO Auto-generated method stub
		final String msg = new String(data, StandardCharsets.UTF_8);
		side = true;
		
		Handler hd = new Handler(getMainLooper());
		
		hd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
				mChatAdapter.add(new ChatMessage(side, msg, false));
			}
		});
		
	}
	@Override
	public void onCompleteSendData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveImageData(byte[] data) {
		// TODO Auto-generated method stub
		String imagePath = createImageWithData(data);
		mChatAdapter.add(new ChatMessage(true, imagePath, true));
	}

}
