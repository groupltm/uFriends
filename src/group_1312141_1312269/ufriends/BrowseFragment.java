package group_1312141_1312269.ufriends;

import java.util.List;

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

public class BrowseFragment extends Fragment implements WifiP2PBroadcastListener{
	
	DeviceListAdapter deviceListAdapter;
    ListView lvDevice;
    View mView;

    IntentFilter filter = new IntentFilter();
    
    public static MyBundle mBundle = new MyBundle();
    
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
    	
    	lvDevice = (ListView)mView.findViewById(R.id.lvDevice);

        deviceListAdapter = new DeviceListAdapter(getActivity(), R.layout.list_device_item, mBundle.mPeerList);
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
        mBundle.mBroadcast.advertiseWifiP2P();
    }


    public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {

        Context mContext;
        int mResource;
        List <WifiP2pDevice> mList;

        public DeviceListAdapter(Context context, int resource,
                                 List<WifiP2pDevice> objects) {
            super(context, resource, objects);
            // TODO Auto-generated constructor stub

            mContext = context;
            mResource = resource;
            mList = objects;
        }

        @SuppressLint("ViewHolder") @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            View v = convertView;

            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(mResource, null);

            TextView tvDeviceName = (TextView)v.findViewById(R.id.tvLabel);
            tvDeviceName.setText(mList.get(position).deviceName);

            return v;
        }

    }

    private void connectToPeer(int position){
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
        mBundle.mPeerList.clear();
        mBundle.mPeerList.addAll(peers.getDeviceList());
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnection() {
        // TODO Auto-generated method stub
//        Intent intent = new Intent(getActivity(), ChatActivity.class);
//        startActivity(intent);

    	Handler hd = new Handler(getActivity().getMainLooper());
    	hd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//MainActivity.viewPager.setCurrentItem(1, true);
				MyBundle.isConnect = true;
				
		    	MainActivity tab = ((MainActivity)getActivity());
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
