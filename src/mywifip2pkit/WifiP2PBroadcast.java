package mywifip2pkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by hungmai on 07/04/2016.
 */
public class WifiP2PBroadcast extends BroadcastReceiver implements WifiP2pManager.PeerListListener, P2PHandleNetwork.P2PHandleNetworkListener {

    public static int SERVICE_BROADCASTING_INTERVAL = 10000;

    public WifiP2pManager mManager = null;
    public WifiP2pManager.Channel mChannel = null;

    public WifiP2pDnsSdServiceInfo service;
    public WifiP2pDnsSdServiceRequest serviceRequest;

    public Context mContext;
    public Context contextRegister;

    public WifiP2PBroadcastListener mListener = null;

    public P2PHandleNetwork mP2PHandle;
    
    IntentFilter mFilter;
    
    boolean isFirst = true;
    boolean isConnected = false;

    public WifiP2PBroadcast(Context context, IntentFilter filter){
        mContext = context;
        mFilter = filter;

        mP2PHandle = new P2PHandleNetwork();
        mP2PHandle.mListener = this;
    }

    public void setManager() {

        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);
    }

    public void registerWithContext(Context context){
    	mContext = context;
        mContext.registerReceiver(this, mFilter);
    }
    
    public void unregister(){
    	try{
    		mContext.unregisterReceiver(this);
    	}
    	catch(IllegalArgumentException e){
    		
    	}
    }
    
    public void createGroup(){
    	mManager.createGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    public void removeGroup(){
    	mManager.removeGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
    }

    public void advertiseWifiP2P() {

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
            	
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub
            	Log.d("failure", "failure of discoverPeers!!!");
            }
        });
    }

    public void advertiseService(final String serviceName, final String serviceType, final Map<String, String> serviceInfo){
    	
        service = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, serviceInfo);


        mManager.addLocalService(mChannel, service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            	Log.d("failure", "failure of addLocalService!!!");
            }
        });
    }

    private void setServiceRequest(WifiP2pManager.DnsSdTxtRecordListener txtListener, WifiP2pManager.DnsSdServiceResponseListener servListener){

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
    }

    public void discoverService(final WifiP2pManager.DnsSdTxtRecordListener txtListener, final WifiP2pManager.DnsSdServiceResponseListener servListener){
    	
    	setServiceRequest(txtListener, servListener);

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
         
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub

            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
//                Handler hd = new Handler();
//                hd.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        discoverService(txtListener, servListener);
//                    }
//                }, SERVICE_BROADCASTING_INTERVAL);
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub
            	Log.d("failure", "failure of discoverService!!!");
            }
        });

    }
    
    public void stopAdvertise(){
    	mManager.stopPeerDiscovery(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Log.d("failure", "failure of stopPeerDiscovery!!!");
			}
		});
    }
    
    public void stopDiscoveryService() {
    	
        mManager.stopPeerDiscovery(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
        mManager.clearServiceRequests(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Log.d("failure", "failure of clearServiceRequests!!!");
			}
		});
        
        mManager.clearLocalServices(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Log.d("failure", "failure of clearLocalServices!!!");
			}
		});
    }

    public void connectPeer(WifiP2pConfig config){
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub
            	Log.d("failure", "failure of connect!!!");
            }
        });
    }
    
    public void notifyDisconnect(){
    	mP2PHandle.notifyDisconnect();
    	Handler hd = new Handler();
    	hd.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				disconnectFromPeer();
			}
		}, 500);
    }

    public void disconnectFromPeer() {
    	stopDiscoveryService();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
            	Log.d("failure", "failure of removeGroup!!!");
            }
        });

        deletePersistentGroups();

        mP2PHandle.disconnect();
        
        mListener.onDisconnect();
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
        	NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Toast.makeText(mContext.getApplicationContext(), "Connection change", Toast.LENGTH_SHORT).show();

            NetworkInfo.State state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED){
            	mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener)mP2PHandle);
           
            }
//            else if (state == NetworkInfo.State.DISCONNECTED){
//            	isConnected = false;
//            	
//            	if (isFirst){
//            		isFirst = false;
//            	}else{
//            		if (mP2PHandle.isGroupOwner){
//                        mP2PHandle.checkConnection();
//                    }else{
//                    	if (mListener != null){
//                    		disconnectFromPeer();
//                        	
//                    		mListener.onDisconnect();
//                    	}
//                    		
//                    }
//            	}              
//            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
          mManager.requestPeers(mChannel, this);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // TODO Auto-generated method stub
        if (mListener != null){
            mListener.onPeers(peers);
        }
    }

    public void sendMessage(InputStream is){
        mP2PHandle.sendMessage(is);
    }
    
    public void sendImage(InputStream is){
    	mP2PHandle.sendImage(is);
    }
    
    public void requestStream(){
    	mP2PHandle.requestStream();
    }
    
    public void allowStream(){
    	mP2PHandle.allowStream();
    }
    
    public void disallowStream(){
    	mP2PHandle.disallowStream();
    }
    
    public void notifyAlreadyStream(){
    	mP2PHandle.notifyAlreadyStream();
    }
    
    public void notifyStopStream(){
    	mP2PHandle.notifyStopStream();
    }

    @Override
    public void onConnectComplete() {
        // TODO Auto-generated method stub
        if (mListener != null){
            mListener.onConnection();
        }
    }

    @Override
    public void onDisconnectComplete() {
    	disconnectFromPeer();
    	if (mListener != null)
    		mListener.onDisconnect();
//        if(mP2PHandle.checkEmptyConnectionPeers()){
//        	disconnectFromPeer();
//        }
//        Log.d("Disconnect", "Disconnected!!!");
    }

    public interface WifiP2PBroadcastListener{
        public void onPeers(WifiP2pDeviceList peers);
        public void onConnection();
        public void onDisconnect();
    }
}