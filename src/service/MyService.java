package service;

import instance.MyBundle;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service{

	MyBundle mBundle;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		mBundle = MyBundle.getInstance();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mBundle.mBroadcast.disconnectFromPeer();
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
		
		mBundle.isKilled = true;
		mBundle.mBroadcast.disconnectFromPeer();
	}
}
