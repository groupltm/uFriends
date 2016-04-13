package group_1312141_1312269.ufriends;

import group_1312141_1312269.ufriends.ReceiveSocketAsync.SocketReceiverDataListener;
import group_1312141_1312269.ufriends.WifiP2PBroadcast.WifiP2PBroadcastListener;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ufriends.R;


public class MainActivity extends Activity implements WifiP2PBroadcastListener{

	DeviceListAdapter deviceListAdapter;
    ListView lvDevice;
    List<WifiP2pDevice> mPeerList = new ArrayList<WifiP2pDevice>();

    public static WifiP2PBroadcast mBroadcast;
    IntentFilter filter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvDevice = (ListView)findViewById(R.id.lvDevice);

        deviceListAdapter = new DeviceListAdapter(this, R.layout.list_device_item, mPeerList);
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
    }
    
    private void setBroadcast() {

        mBroadcast = new WifiP2PBroadcast(this);
        mBroadcast.setManager();

        mBroadcast.mListener = this;

        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mBroadcast.register(filter);
        mBroadcast.advertiseWifiP2P();
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

            LayoutInflater inflater = getLayoutInflater();
            v = inflater.inflate(mResource, null);

            TextView tvDeviceName = (TextView)v.findViewById(R.id.tvLabel);
            tvDeviceName.setText(mList.get(position).deviceName);

            return v;
        }

    }

    private void connectToPeer(int position){
        WifiP2pDevice device = mPeerList.get(position);
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 15;
        
        mBroadcast.connectPeer(config);
    }
    
    @Override
    public void onPeers(WifiP2pDeviceList peers) {
        // TODO Auto-generated method stub
        mPeerList.clear();
        mPeerList.addAll(peers.getDeviceList());
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnection() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDisconnect() {
        // TODO Auto-generated method stub
    	mBroadcast.advertiseWifiP2P();
    }
}
