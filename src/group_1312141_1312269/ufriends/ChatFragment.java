package group_1312141_1312269.ufriends;

import group_1312141_1312269.ufriends.ReceiveSocketAsync.SocketReceiverDataListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.example.ufriends.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ChatFragment extends Fragment implements SocketReceiverDataListener{

	ChatArrayAdapter mChatAdapter;

	ListView lvChat;
	EditText edtChatIn;
	Button btnSend;
	
	View mView;

	boolean side = false;
	
	MyBundle mBundle;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mBundle = mBundle.getInstance();
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.test, container, false);		
		
		LinearLayout layout = (LinearLayout) mView.findViewById(R.id.layout);
		
		lvChat = (ListView)mView.findViewById(R.id.lvChat);
		edtChatIn = (EditText)mView.findViewById(R.id.edtChatIn);
		btnSend = (Button)mView.findViewById(R.id.btnSend);
		
		mChatAdapter = new ChatArrayAdapter(getActivity(), R.layout.list_chat_left_item);
		
		edtChatIn.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
			}
		});
		
		layout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(edtChatIn.getWindowToken(), 0);
				
				return false;
			}
		});
		
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
		
		return mView;
	}
	
	private InputStream convertStringToIs(String s){
		InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
		return stream;
	}
	
	private boolean sendChatMessage(){
		String msg = edtChatIn.getText().toString();
		if (!msg.equals("")){
			side = false;
			mChatAdapter.add(new ChatMessage(side, edtChatIn.getText().toString()));
	        edtChatIn.setText("");
	        
	        InputStream is = convertStringToIs(msg);
	        mBundle.mBroadcast.send(is);
		}
		
		return true;
	}

	@Override
	public void onReceiveData(byte[] data) {
		// TODO Auto-generated method stub
		final String msg = new String(data, StandardCharsets.UTF_8);
		side = true;
		
		Handler hd = new Handler(getActivity().getMainLooper());
		
		hd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
				mChatAdapter.add(new ChatMessage(side, msg));
			}
		});
		
	}
}
