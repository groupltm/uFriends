package group_1312141_1312269.ufriends;

import android.net.wifi.p2p.WifiP2pDevice;

public class MyPeer {
	WifiP2pDevice peerDevice;
	Info peerInfo;
	
	public MyPeer(){
		peerDevice = null;
		peerInfo = new Info();
	}
	
	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
