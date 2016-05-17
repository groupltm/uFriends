package activity;

import instance.Info;
import instance.MyBundle;
import instance.MyPeer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mywifip2pkit.ReceiveSocketAsync.SocketReceiverDataListener;
import mywifip2pkit.WifiP2PBroadcast.WifiP2PBroadcastListener;

import com.example.ufriends.R;

import custom_view.ProgressWheel;

import adapter.ChatArrayAdapter;
import adapter.DeviceListAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class BrowseFragment extends Fragment implements
		WifiP2PBroadcastListener, SocketReceiverDataListener {

	DeviceListAdapter deviceListAdapter;
	DeviceListAdapter deviceConnectListAdapter;
	ListView lvDevice;
	ListView lvConnectDevice;
	View mView;
	ProgressWheel mProgress;
	ImageButton btnFind;

	MyBundle mBundle = MyBundle.getInstance();
	
	Thread checkPeerThread;

	Handler hd_Progress = null;
	Runnable r_Progress = null;
	
	boolean didSendAvatar = false;
	boolean isActive = false;
	boolean didReceive = false;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mBundle.mBroadcast.mP2PHandle.setReceiveDataListener(this);
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_browse, container, false);

		lvDevice = (ListView) mView.findViewById(R.id.lvDevice);
		mProgress = (ProgressWheel)mView.findViewById(R.id.progress_wheel);
		btnFind = (ImageButton)mView.findViewById(R.id.btnFind);
		//lvConnectDevice = (ListView)mView.findViewById(R.id.lvConnectDevice);
		

		deviceListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerList);
		deviceConnectListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerList);
		
		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MyPeer peer = mBundle.mPeerList.get(position);
				if (peer.peerInfo._status == 2){
					isActive = true;
					connectToPeer(position);
				}else if (peer.peerInfo._status == 0){
					showChatView();
				}
			}
		});
		
		lvDevice.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MyPeer peer = mBundle.mPeerList.get(position);
				if (peer.peerInfo._status == 0){
					mBundle.mBroadcast.disconnectFromPeer();
					//restartDiscovery();
				}
				
				return false;
			}
		});
		
//		lvConnectDevice.setAdapter(deviceConnectListAdapter);
//		lvConnectDevice.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				// TODO Auto-generated method stub
//				checkConnectPeer();
//			}
//		}); 
		
		btnFind.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mProgress.setVisibility(View.VISIBLE);
				if (hd_Progress == null){
					hd_Progress = new Handler();
				}
				if (r_Progress == null){
					r_Progress = new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mProgress.setVisibility(View.GONE);
						}
					};
				}
				hd_Progress.removeCallbacks(r_Progress);
				hd_Progress.postDelayed(r_Progress, 30000);
				restartDiscovery();
			}
			
		});
		
		mBundle.mBroadcast.mListener = this;
		mBundle.mBroadcast.mP2PHandle.setReceiveDataListener(this);
		
		return mView;
	}
	
	public void restartDiscovery(){
		mBundle.mPeerList.clear();
		
		Map<String, String> serviceInfo = createMap(mBundle.mInfo);
		mBundle.mBroadcast.stopDiscoveryService();
		mBundle.mBroadcast.advertiseService("ufriends", "_tcp", serviceInfo);
		mBundle.mBroadcast.discoverService(
				new DnsSdTxtRecordListener() {

					@Override
					public void onDnsSdTxtRecordAvailable(
							String fullDomainName,
							Map<String, String> txtRecordMap,
							WifiP2pDevice srcDevice) {
						// TODO Auto-generated method stub
						if (fullDomainName.equals("ufriends._tcp.local.")) {
							if (!checkPeerInMyPeerList(mBundle.mPeerList, srcDevice)){
								Info info = convertMapToInfo(txtRecordMap);
								MyPeer peer = new MyPeer();
								peer.peerDevice = srcDevice;
								peer.peerInfo = info;
								mBundle.mPeerList.add(peer);
								deviceListAdapter.notifyDataSetChanged();
							}					
						}
					}
				}, null);

	}

	private Map<String, String> createMap(Info info) {
		Map<String, String> result = new HashMap<>();

		result.put("name", info._name);
		result.put("age", String.valueOf(info._age));
		result.put("sex", String.valueOf(info._sex));

		return result;
	}

	private Info convertMapToInfo(Map<String, String> map) {
		Info info = new Info();
		info._name = map.get("name");
		info._age = Integer.parseInt(map.get("age"));
		info._sex = Boolean.parseBoolean(map.get("sex"));

		return info;
	}
	
//	private void addConnectPeerToList(Info peerInfo, int status){
//		peerInfo._status = status;
//		mBundle.mPeerInfoConnectList.clear();
//		mBundle.mPeerInfoConnectList.add(peerInfo);
//		deviceConnectListAdapter.notifyDataSetChanged();
//	}

	private void connectToPeer(int position) {
		WifiP2pDevice device = mBundle.mPeerList.get(position).peerDevice;
		final WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 15;

		mBundle.mBroadcast.connectPeer(config);
	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		 //TODO Auto-generated method stub
//		 mBundle.mPeerList.clear();
//		 mBundle.mPeerList.addAll(peers.getDeviceList());
//		 
//		 
//		 mBundle.mPeerInfoList.clear();
//		 for (int i = 0; i < mBundle.mPeerList.size(); i++){
//			 mBundle.mPeerInfoList.add(new Info());
//			 
//			 if (mBundle.mPeerList.get(i).status == 1){
//				 addConnectPeerToList(mBundle.mPeerInfoList.get(i), 1);
//			 }
//			 else if (mBundle.mPeerList.get(i).status == 0){
//				 addConnectPeerToList(mBundle.mPeerInfoList.get(i), 2);
//			 }
//
//		 }
//		 
//		 deviceListAdapter.notifyDataSetChanged();
		
		List<WifiP2pDevice> collecPeers = new ArrayList<WifiP2pDevice>();
		collecPeers.addAll(peers.getDeviceList());
		for (MyPeer peer:mBundle.mPeerList){
			int i = checkAvailablePeers(collecPeers, peer.peerDevice); 
			int peerPosition = mBundle.mPeerList.indexOf(peer);
			if(i == -1){
				mBundle.mPeerList.remove(peerPosition);
			} else {
				WifiP2pDevice tempPeer = collecPeers.get(i);
				if (tempPeer.status == 0 || tempPeer.status == 1){
//					int position = mBundle.mPeerList.indexOf(peer);
//					Info infoConnectPeer = mBundle.mPeerInfoList.get(position);
//					infoConnectPeer._status = collecPeers.get(i).status;
//					mBundle.mPeerInfoConnectList.add(mBundle.mPeerInfoList.get(position));
					peer.peerInfo._status = tempPeer.status;
					mBundle.mPeerList.remove(peerPosition);
					mBundle.mPeerList.add(0, peer);
				}
			}
		}
		
		checkConnectedPeer(collecPeers);
		
		deviceListAdapter.notifyDataSetChanged();
		//deviceConnectListAdapter.notifyDataSetChanged();
	}
	
	private int checkAvailablePeers(List<WifiP2pDevice> peers, WifiP2pDevice peer){
		for (WifiP2pDevice tempPeer:peers){
			if (tempPeer.deviceAddress.equals(peer.deviceAddress)){
				return peers.indexOf(tempPeer);
			}
		}
		
		return -1;
	}
	
	private boolean checkPeerInMyPeerList(List<MyPeer>mPeerList, WifiP2pDevice peerDevice){
		for (MyPeer peer:mPeerList){
			if (peer.peerDevice.deviceAddress.equals(peerDevice.deviceAddress)){
				return true;
			}
		}
		
		return false;
	}
	
	private void checkConnectedPeer(List<WifiP2pDevice> peers){
		for (WifiP2pDevice peer:peers){
			if (!checkPeerInMyPeerList(mBundle.mPeerList, peer)){
				if (peer.status == 0 || peer.status == 1){
					MyPeer mPeer = new MyPeer();
					mPeer.peerDevice = peer;
					mBundle.mPeerList.add(0, mPeer);
				}
			}
		}
	}
	
	private void showChatView(){
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		startActivity(intent);
	}
	
	private void sendInfo(){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(out);
			os.writeObject(mBundle.mInfo);
			byte[] bInfo = out.toByteArray();
			InputStream is = new ByteArrayInputStream(bInfo);
			mBundle.mBroadcast.sendMessage(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    
	}
	
	private void sendAvatar(){
		try {
			InputStream is;
			if (!mBundle.mInfo._imagePath.equals("")){
				is = new FileInputStream(mBundle.mInfo._imagePath);
			}else {
				is = getActivity().getResources().openRawResource(R.drawable.ic_launcher);
			}
			
			mBundle.mBroadcast.sendImage(is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onConnection() {
		 //TODO Auto-generated method stub
		mBundle.mBroadcast.stopDiscoveryService();
		ChatArrayAdapter.Clear();
		if (isActive == true){
			sendInfo();
			Log.d("send", "send info");
		}
//		Handler hd = new Handler(getActivity().getMainLooper());
//		hd.post(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				// MainActivity.viewPager.setCurrentItem(1, true);
//				mBundle.isConnect = true;
//
//				MainActivity tab = ((MainActivity) getActivity());
//				tab.setCurrentTab(2);	
//
//			}
//		});

	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
//		mBundle.mPeerInfoConnectList.clear();
//		mBundle.mBroadcast.advertiseWifiP2P();
		
		restartDiscovery();
	}

	@Override
	public void onStopDiscovery() {
		// TODO Auto-generated method stub
//		Handler hd = new Handler(getActivity().getMainLooper());
//		hd.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				mProgress.setVisibility(View.GONE);
//			}
//		});	
	}

	@Override
	public void onStartDiscovery() {
		// TODO Auto-generated method stub
//		Handler hd = new Handler(getActivity().getMainLooper());
//		hd.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				mProgress.setVisibility(View.VISIBLE);
//			}
//		});	
	}

	@Override
	public void onReceiveMessageData(byte[] data) {
		// TODO Auto-generated method stub
		ByteArrayInputStream in = new ByteArrayInputStream(data);
	    
	    try {
	    	ObjectInputStream is = new ObjectInputStream(in);
			mBundle.peerInfo = (Info)is.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onReceiveImageData(final byte[] data) {
		// TODO Auto-generated method stub
		Handler hd = new Handler(getActivity().getMainLooper());
		hd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBundle.peerAvatar = BitmapFactory.decodeByteArray(data , 0, data.length);
			}
		});		
		if (isActive == false){
			sendInfo();
		}else {
			didSendAvatar = false;
			showChatView();
		}
	}

	@Override
	public void onCompleteSendData() {
		// TODO Auto-generated method stub
		if (!didSendAvatar){
			sendAvatar();
			Log.d("send", "send avatar");
			didSendAvatar = true;
		}else {
			if (!isActive){
				didSendAvatar = false;
				showChatView();
			}
		}
	}
}