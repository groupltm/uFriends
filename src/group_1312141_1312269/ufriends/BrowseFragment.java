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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class BrowseFragment extends Fragment implements
		WifiP2PBroadcastListener {

	DeviceListAdapter deviceListAdapter;
	ListView lvDevice;
	View mView;

	IntentFilter filter = new IntentFilter();

	MyBundle mBundle = MyBundle.getInstance();

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

		deviceListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerInfoList);
		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				connectToPeer(position);
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
						if (fullDomainName.equals("ufriends._tcp.local.")) {
							if (!mBundle.mPeerList.contains(srcDevice)){
								Info info = convertMapToInfo(txtRecordMap);
								mBundle.mPeerInfoList.add(info);
								mBundle.mPeerList.add(srcDevice);
								deviceListAdapter.notifyDataSetChanged();
							}					
						}
					}
				}, null);
	}

	public class DeviceListAdapter extends ArrayAdapter<Info> {

		Context mContext;
		int mResource;
		// List <WifiP2pDevice> mList;
		List<Info> mInfoList;

		public DeviceListAdapter(Context context, int resource,
				List<Info> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub

			mContext = context;
			mResource = resource;
			// mList = objects;
			mInfoList = objects;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			View v = convertView;

			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(mResource, null);

			TextView tvDeviceName = (TextView) v.findViewById(R.id.tvLabel);
			TextView tvSubInfo = (TextView) v.findViewById(R.id.tvSubInfo);

			tvDeviceName.setText(mInfoList.get(position)._name);
			Info info = mInfoList.get(position);
			String subInfo;

			if (info._sex) {
				subInfo = "Sex: Male; " + "Age: " + String.valueOf(info._age);
			} else {
				subInfo = "Sex: Female; " + "Age: " + String.valueOf(info._age);
			}
			tvSubInfo.setText(subInfo);

			return v;
		}

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
		// TODO Auto-generated method stub
		// mBundle.mPeerList.clear();
		// mBundle.mPeerList.addAll(peers.getDeviceList());
		// deviceListAdapter.notifyDataSetChanged();
		
		Collection<WifiP2pDevice> collecPeers = peers.getDeviceList();
		for (WifiP2pDevice peer:mBundle.mPeerList){
			if(!checkAvailablePeers(collecPeers, peer)){
				int peerPosition = mBundle.mPeerList.indexOf(peer);
				mBundle.mPeerInfoList.remove(peerPosition);
				mBundle.mPeerList.remove(peerPosition);
			}
		}
		
		deviceListAdapter.notifyDataSetChanged();
	}
	
	private boolean checkAvailablePeers(Collection<WifiP2pDevice> peers, WifiP2pDevice peer){
		if (peers.contains(peer)){
			return true;
		}
		return false;
	}

	@Override
	public void onConnection() {
		// TODO Auto-generated method stub
		// Intent intent = new Intent(getActivity(), ChatActivity.class);
		// startActivity(intent);

		Handler hd = new Handler(getActivity().getMainLooper());
		hd.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// MainActivity.viewPager.setCurrentItem(1, true);
				mBundle.isConnect = true;

				MainActivity tab = ((MainActivity) getActivity());
				tab.setCurrentTab(2);

			}
		});

	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		mBundle.mBroadcast.advertiseWifiP2P();
	}
}
