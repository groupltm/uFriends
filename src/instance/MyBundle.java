package instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mywifip2pkit.WifiP2PBroadcast;

import com.example.ufriends.R;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Environment;
import android.provider.OpenableColumns;

public class MyBundle implements Serializable {

	public List<MyPeer> mPeerList;
	// public List<Info> mPeerInfoList;
	public List<Info> mPeerInfoConnectList;
	public List<String> receivedImagePath;
	
	public WifiP2PBroadcast mBroadcast;
	public boolean isConnect;
	public Info mInfo;
	public Bitmap peerAvatar;
	public Bitmap myAvatar;
	public Info peerInfo;
	
	public boolean isKilled = false;
	
	

	private static MyBundle mBundle = new MyBundle();

	private MyBundle() {
		// TODO Auto-generated constructor stub
		mPeerList = new ArrayList<MyPeer>();

		mPeerInfoConnectList = new ArrayList<>();
		
		receivedImagePath = new ArrayList<>();
		
		mInfo = new Info();
		
	}

	public static MyBundle getInstance() {
		return mBundle;
	}
	
	public void setBroadcast(Context context) {
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		
		mBundle.mBroadcast = new WifiP2PBroadcast(context, filter);
		mBundle.mBroadcast.setManager();
		
		//mBundle.mBroadcast.createGroup();
		// mBundle.mBroadcast.advertiseWifiP2P();
	}
	
	public void setMyAvatar(Context context){
		myAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.applogo_256);
	}

	public void getInfoFromJSONFile(Context context)
			throws JsonSyntaxException, JsonIOException, IOException {
		Gson gson = new Gson();

		File file = new File("data/data/com.example.ufriends/test.json");

		InputStream inputStream = context.openFileInput("test.json");
		String jsonString = "";

		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String receiveString = "";
			StringBuilder stringBuilder = new StringBuilder();

			while ((receiveString = bufferedReader.readLine()) != null) {
				stringBuilder.append(receiveString);
			}

			inputStream.close();
			jsonString = stringBuilder.toString();
		}
		if (file.exists()) {
			// 1. JSON to Java object, read it from a file.
			mInfo = gson.fromJson(jsonString, Info.class);

			File imgFile = new File(mInfo._imagePath);

			if (imgFile.exists()) {
				myAvatar = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			}
		}
		
		setDeviceName(mInfo._name);
	}

	public void setInfoToJSONFile(Context context) throws JsonIOException,
			IOException {

		Gson gson = new Gson();

		// 1. Java object to JSON, and save into a file
		File file = new File("data/data/com.example.ufriends/test.json");
		if (!file.exists()) {
			file.createNewFile();
			context.sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
		}
		String jsonString = gson.toJson(mInfo);
		OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(
				"test.json", Context.MODE_PRIVATE));
		out.write(jsonString);
		out.close();
		
		setDeviceName(mInfo._name);
	}
	
	private void setDeviceName(String name){
		try {
            Method m = mBroadcast.mManager.getClass().getMethod(
                    "setDeviceName",
                    new Class[]{WifiP2pManager.Channel.class, String.class,
                            WifiP2pManager.ActionListener.class});

            m.invoke(mBroadcast.mManager, mBroadcast.mChannel, name, new ActionListener() {
    			
    			@Override
    			public void onSuccess() {
    				// TODO Auto-generated method stub
    				
    			}
    			
    			@Override
    			public void onFailure(int reason) {
    				// TODO Auto-generated method stub
    				
    			}
    		});
        } catch (Exception e) {
        }
	}
}
