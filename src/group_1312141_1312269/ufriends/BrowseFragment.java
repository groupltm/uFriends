package group_1312141_1312269.ufriends;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.ufriends.R;
import group_1312141_1312269.ufriends.WifiP2PBroadcast.WifiP2PBroadcastListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class BrowseFragment extends Fragment implements
		WifiP2PBroadcastListener {

	DeviceListAdapter deviceListAdapter;
	DeviceListAdapter deviceConnectListAdapter;
	ListView lvDevice;
	ListView lvConnectDevice;
	View mView;

	IntentFilter filter = new IntentFilter();

	MyBundle mBundle = MyBundle.getInstance();
	
	Thread checkPeerThread;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_browse, container, false);

		lvDevice = (ListView) mView.findViewById(R.id.lvDevice);
		lvConnectDevice = (ListView)mView.findViewById(R.id.lvConnectDevice);
		

		deviceListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerInfoList);
		deviceConnectListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerInfoConnectList);
		
		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				connectToPeer(position);
			}
		});
		
		lvConnectDevice.setAdapter(deviceConnectListAdapter);
		lvConnectDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				checkConnectPeer();
			}
		});

		setBroadcast();

		return mView;
	}

	private void setBroadcast() {

		mBundle.mBroadcast = new WifiP2PBroadcast(getActivity());
		mBundle.mBroadcast.setManager();

		mBundle.mBroadcast.mListener = this;

		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

		mBundle.mBroadcast.register(filter);
		// mBundle.mBroadcast.advertiseWifiP2P();

		Map<String, String> serviceInfo = createMap(mBundle.mInfo);

		mBundle.mBroadcast.advertiseService("ufriends", "_tcp", serviceInfo);
		mBundle.mBroadcast.discoverService("ufriends",
				new DnsSdTxtRecordListener() {

					@Override
					public void onDnsSdTxtRecordAvailable(
							String fullDomainName,
							Map<String, String> txtRecordMap,
							WifiP2pDevice srcDevice) {
						// TODO Auto-generated method stub
//						if (fullDomainName.equals("ufriends._tcp.local.")) {
//							if (!mBundle.mPeerList.contains(srcDevice)){
//								Info info = convertMapToInfo(txtRecordMap);
//								mBundle.mPeerInfoList.add(info);
//								mBundle.mPeerList.add(srcDevice);
//								deviceListAdapter.notifyDataSetChanged();
//							}					
//						}
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
	
	private void addConnectPeerToList(Info peerInfo, int status){
		peerInfo._status = status;
		mBundle.mPeerInfoConnectList.clear();
		mBundle.mPeerInfoConnectList.add(peerInfo);
		deviceConnectListAdapter.notifyDataSetChanged();
	}
	
	private void checkConnectPeer(){
		Info info = mBundle.mPeerInfoConnectList.get(0);
		if (info._status == 1){
			mBundle.mBroadcast.disconnectFromPeer();
		}else {
			showChatView();
		}
	}

	private void connectToPeer(int position) {
		WifiP2pDevice device = mBundle.mPeerList.get(position);
		final WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 15;

		mBundle.mBroadcast.connectPeer(config);
	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		 //TODO Auto-generated method stub
		 mBundle.mPeerList.clear();
		 mBundle.mPeerList.addAll(peers.getDeviceList());
		 
		 
		 mBundle.mPeerInfoList.clear();
		 for (int i = 0; i < mBundle.mPeerList.size(); i++){
			 mBundle.mPeerInfoList.add(new Info());
			 
			 if (mBundle.mPeerList.get(i).status == 1){
				 addConnectPeerToList(mBundle.mPeerInfoList.get(i), 1);
			 }
			 else if (mBundle.mPeerList.get(i).status == 0){
				 addConnectPeerToList(mBundle.mPeerInfoList.get(i), 2);
			 }

		 }
		 
		 deviceListAdapter.notifyDataSetChanged();
		
//		Collection<WifiP2pDevice> collecPeers = peers.getDeviceList();
//		for (WifiP2pDevice peer:mBundle.mPeerList){
//			if(!checkAvailablePeers(collecPeers, peer)){
//				int peerPosition = mBundle.mPeerList.indexOf(peer);
//				mBundle.mPeerInfoList.remove(peerPosition);
//				mBundle.mPeerList.remove(peerPosition);
//			}
//		}
//		
//		deviceListAdapter.notifyDataSetChanged();
	}
	
	private boolean checkAvailablePeers(Collection<WifiP2pDevice> peers, WifiP2pDevice peer){
		if (peers.contains(peer)){
			return true;
		}
		return false;
	}
	
	private void showChatView(){
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		startActivity(intent);
	}

	@Override
	public void onConnection() {
		 //TODO Auto-generated method stub
		mBundle.mBroadcast.stopAdvertise();
		showChatView();

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
		mBundle.mPeerInfoConnectList.clear();
		mBundle.mBroadcast.advertiseWifiP2P();
	}

	@Override
	public void onConnecting() {
		// TODO Auto-generated method stub
		
	}
}
