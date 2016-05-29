package instance;

import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;

public class MyPeer {
	public WifiP2pDevice peerDevice;
	public Info peerInfo;
	public Bitmap peerAvatar;
	
	public MyPeer(){
		peerDevice = null;
		peerInfo = new Info();
		peerAvatar = null;
	}
	
	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
