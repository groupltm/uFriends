package activity;

import instance.MyBundle;

import java.util.List;

import com.example.ufriends.R;

import mywifip2pkit.ReceiveSocketAsync.OnStreamListener;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class StreamerActivity extends Activity implements OnStreamListener{

	public static int PORT = 5544;
	private SurfaceView mSurfaceView;
	
	private MyBundle mBundle;

	private float mDist;
	
	ImageButton btnStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_streamer);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		btnStop = (ImageButton)findViewById(R.id.btnStop);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		
		mBundle = MyBundle.getInstance();
		
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
		
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(RtspServer.KEY_PORT, String.valueOf(PORT));
		editor.commit();
		
		SessionBuilder.getInstance()
		.setSurfaceView(mSurfaceView)
		.setPreviewOrientation(90)
		.setContext(getApplicationContext())
		.setAudioEncoder(SessionBuilder.AUDIO_NONE)
		.setVideoEncoder(SessionBuilder.VIDEO_H264);
		
		mBundle.mBroadcast.notifyAlreadyStream();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mBundle.mBroadcast.mP2PHandle.setStreamListener(this);
		
		this.startService(new Intent(this,RtspServer.class));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		this.stopService(new Intent(this,RtspServer.class));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    // Get the pointer ID

	    Camera.Parameters params = VideoStream.mCamera.getParameters();
	    int action = event.getAction();


	    if (event.getPointerCount() > 1) {
	        // handle multi-touch events
	        if (action == MotionEvent.ACTION_POINTER_DOWN) {
	            mDist = getFingerSpacing(event);
	        } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
	            VideoStream.mCamera.cancelAutoFocus();
	            handleZoom(event, params);
	        }
	    } else {
	        // handle single touch events
	        if (action == MotionEvent.ACTION_UP) {
	            handleFocus(event, params);
	        }
	    }
	    return true;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub	
		mBundle.mBroadcast.notifyStopStream();
		finish();	
	}

	private void handleZoom(MotionEvent event, Camera.Parameters params) {
	    int maxZoom = params.getMaxZoom();
	    int zoom = params.getZoom();
	    float newDist = getFingerSpacing(event);
	    if (newDist > mDist) {
	        //zoom in
	        if (zoom < maxZoom)
	            zoom++;
	    } else if (newDist < mDist) {
	        //zoom out
	        if (zoom > 0)
	            zoom--;
	    }
	    mDist = newDist;
	    params.setZoom(zoom);
	    VideoStream.mCamera.setParameters(params);
	}

	public void handleFocus(MotionEvent event, Camera.Parameters params) {
	    int pointerId = event.getPointerId(0);
	    int pointerIndex = event.findPointerIndex(pointerId);
	    // Get the pointer's current position
	    float x = event.getX(pointerIndex);
	    float y = event.getY(pointerIndex);

	    List<String> supportedFocusModes = params.getSupportedFocusModes();
	    if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
	        VideoStream.mCamera.autoFocus(new Camera.AutoFocusCallback() {
	            @Override
	            public void onAutoFocus(boolean b, Camera camera) {
	                // currently set to auto-focus on single touch
	            }
	        });
	    }
	}

	/**
	 * Determine the space between the first two fingers
	 */
	private float getFingerSpacing(MotionEvent event) {
	    // ...
	    float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
	    return (float) Math.sqrt(x * x + y * y);
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
