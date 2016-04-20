package group_1312141_1312269.ufriends;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.p2p.WifiP2pDevice;

public class MyBundle implements Serializable{
	
	public List<WifiP2pDevice> mPeerList; 
	public WifiP2PBroadcast mBroadcast;
	
	public MyBundle() {
		// TODO Auto-generated constructor stub
		mPeerList = new ArrayList<WifiP2pDevice>();
	}
}
