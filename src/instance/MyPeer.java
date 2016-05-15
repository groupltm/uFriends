package instance;

import android.net.wifi.p2p.WifiP2pDevice;

public class MyPeer {
	public WifiP2pDevice peerDevice;
	public Info peerInfo;
	
	public MyPeer(){
		peerDevice = null;
		peerInfo = new Info();
	}
	
	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
