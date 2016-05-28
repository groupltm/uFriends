package activity;

import mywifip2pkit.ReceiveSocketAsync.OnStreamListener;
import instance.MyBundle;

import com.example.ufriends.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.VideoView;

public class PlayerActivity extends Activity implements OnStreamListener{

	VideoView mVideoView;
	ImageButton btnStop;
	
	MyBundle mBundle;
	
	String streamerIP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
		mBundle = MyBundle.getInstance();
		
		Intent intent = getIntent();
		streamerIP = intent.getStringExtra("IPSTREAMER");
		
		mVideoView = (VideoView) findViewById(R.id.vdo);
		mVideoView.setMediaController(new android.widget.MediaController(this));
		mVideoView.setVideoURI(Uri.parse("rtsp://" + streamerIP + ":5544"));
		
		btnStop = (ImageButton)findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBundle.mBroadcast.notifyStopStream();
				
				Handler hd = new Handler(getMainLooper());
				hd.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mBundle.mBroadcast.mP2PHandle.setStreamListener(this);
		
		
		mVideoView.requestFocus();
		mVideoView.start();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub	
		mBundle.mBroadcast.notifyStopStream();
		finish();	
	}

	@Override
	public void onReceiveRequestStream() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveAllowStream() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveDisallowStream() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAlreadyStream(String streamerIP) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopStream() {
		// TODO Auto-generated method stub
		finish();
	}
}
