package mywifip2pkit;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;

/**
 * Created by hungmai on 07/04/2016.
 */
public class P2PHandleNetwork implements WifiP2pManager.ConnectionInfoListener,
		ServerSendSocket_Thread.ServerSocketListener,
		ReceiveSocketAsync.onReceivedDataListener {

	private static final int SOCKET_TIMEOUT = 1000;

	ServerReceiveSocket_Thread serverReceiveThread;
	ServerSendSocket_Thread serverSendThread;

	// List<SendingData> list_sendData = new ArrayList<>();

	List<ConnectedPeer> mConnectedPeers = new ArrayList<>();

	ConnectedPeer tempPeer;

	boolean isGroupOwner = false;

	public P2PHandleNetworkListener mListener;
	public ReceiveSocketAsync.SocketReceiverDataListener mReceiveDataListener;

	public P2PHandleNetwork() {
		mReceiveDataListener = null;
	}

	public void setReceiveDataListener(
			ReceiveSocketAsync.SocketReceiverDataListener listener) {
		mReceiveDataListener = listener;
		for (ConnectedPeer peer : mConnectedPeers) {
			if (peer != null) {
				if (peer.mReceiveThread != null) {
					peer.mReceiveThread.mReceiveListener = listener;
				}
			}
		}
	}
	
	public void setStreamListener(
			ReceiveSocketAsync.OnStreamListener listener) {
		for (ConnectedPeer peer : mConnectedPeers) {
			if (peer != null) {
				if (peer.mReceiveThread != null) {
					peer.mReceiveThread.mStreamListener = listener;
				}
			}
		}
	}

	public class ConnectedPeer {
		Socket mSendSocket;
		Socket mReceiveSocket;
		ReceiveSocketAsync mReceiveThread;

		Boolean isAlive;

		public ConnectedPeer() {
			isAlive = true;
		}
	}

	// public class SendingData {
	// boolean _isMessage;
	// InputStream _is;
	//
	// public SendingData(boolean isMessage, InputStream is){
	// _isMessage = isMessage;
	// _is = is;
	// }
	// }

	public void sendMessage(final InputStream is) {
		try {
			// list_sendData.add(new SendingData(true, is));
			for (ConnectedPeer peer : mConnectedPeers) {
				if (peer.mSendSocket != null) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					FileTransferService.sendMessage(is, os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendImage(final InputStream is) {
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					FileTransferService.sendImage(is, os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void requestStream() {
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					sendRequestStream(os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void allowStream() {
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					sendAllowStreamCode(os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disallowStream() {
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					sendDisallowStream(os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyAlreadyStream() {
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					sendAlreadyStream(os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void notifyStopStream(){
		try {
			for (ConnectedPeer peer : mConnectedPeers) {
				if (!peer.mSendSocket.isClosed()) {
					final OutputStream os = peer.mSendSocket.getOutputStream();
					sendStopStream(os);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (serverSendThread != null) {
			serverSendThread.stop();
			serverReceiveThread.stop();

			serverSendThread = null;
			serverReceiveThread = null;
		}

		closeAllSockets();
	}

	private void closeAllSockets() {
		for (ConnectedPeer peer : mConnectedPeers) {
			if (peer != null) {
				closeSocket(peer);
			}
		}

		mConnectedPeers.clear();
	}

	private void closeSocket(ConnectedPeer peer) {
		try {
			if (peer.mSendSocket != null) {
				peer.mSendSocket.close();
				peer.mReceiveSocket.close();
				peer.mReceiveThread.stop();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		int position = mConnectedPeers.indexOf(peer);
		mConnectedPeers.set(position, null);
		mListener.onDisconnectComplete();
	}

	private void addPeer(ConnectedPeer peer) {
		boolean isAdd = false;
		for (int i = 0; i < mConnectedPeers.size(); i++) {
			if (mConnectedPeers.get(i) == null) {
				mConnectedPeers.set(i, peer);
				isAdd = true;
			}
		}

		if (isAdd == false) {
			mConnectedPeers.add(peer);
		}
	}

	public void checkConnection() {
		formatConnectionPeers();

		for (ConnectedPeer peer : mConnectedPeers) {
			try {
				if (peer != null) {
					if (peer.mSendSocket != null) {
						sendPing(peer.mSendSocket.getOutputStream());
					} else {
						peer = null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Handler hd = new Handler(Looper.getMainLooper());
		hd.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (ConnectedPeer peer : mConnectedPeers) {
					if (!peer.isAlive) {
						closeSocket(peer);
					}
				}
			}
		}, SOCKET_TIMEOUT);
	}

	private void formatConnectionPeers() {
		for (ConnectedPeer peer : mConnectedPeers) {
			if (peer != null) {
				peer.isAlive = false;
			}
		}
	}

	public boolean checkEmptyConnectionPeers() {
		for (ConnectedPeer peer : mConnectedPeers) {
			if (peer != null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		if (info.groupOwnerAddress == null) {
			return;
		}
		final String hostIP = info.groupOwnerAddress.getHostAddress();
		isGroupOwner = info.isGroupOwner;

		if (info.groupFormed) {
			tempPeer = new ConnectedPeer();

			if (info.isGroupOwner) {
				try {

					if (serverSendThread == null && serverReceiveThread == null) {
						serverReceiveThread = new ServerReceiveSocket_Thread(
								this);
						serverReceiveThread.start();

						serverSendThread = new ServerSendSocket_Thread(this);
						serverSendThread.start();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			} else {

				final Socket mSendSocket = new Socket();
				final Socket mReceiveSocket = new Socket();
				addPeer(tempPeer);
				ReceiveSocketAsync receiveThread = new ReceiveSocketAsync(this,
						mReceiveDataListener, mReceiveSocket,
						mConnectedPeers.indexOf(tempPeer));

				tempPeer.mSendSocket = mSendSocket;
				tempPeer.mReceiveSocket = mReceiveSocket;
				tempPeer.mReceiveThread = receiveThread;

				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							mReceiveSocket.bind(null);
							mReceiveSocket.connect(new InetSocketAddress(
									hostIP, ServerSendSocket_Thread.PORT),
									SOCKET_TIMEOUT);

							mSendSocket.bind(null);
							mSendSocket.connect(new InetSocketAddress(hostIP,
									ServerReceiveSocket_Thread.PORT),
									SOCKET_TIMEOUT);

							tempPeer.mReceiveThread.start();
							mListener.onConnectComplete();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

						}

					}
				};
				Thread connect = new Thread(runnable);
				connect.start();
			}
		}
	}

	private void sendCodeReceivedData(OutputStream os) {
		String receivedCode = "250 ";
		InputStream stream = new ByteArrayInputStream(
				receivedCode.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendPing(OutputStream os) {
		String ping = "340 ";
		InputStream stream = new ByteArrayInputStream(
				ping.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendPingOK(OutputStream os) {
		String pingOK = "360 ";
		InputStream stream = new ByteArrayInputStream(
				pingOK.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendRequestStream(OutputStream os) {
		String requestStream = "380 ";
		InputStream stream = new ByteArrayInputStream(
				requestStream.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendAllowStreamCode(OutputStream os) {
		String allowStream = "400 ";
		InputStream stream = new ByteArrayInputStream(
				allowStream.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendDisallowStream(OutputStream os) {
		String disallowStream = "420 ";
		InputStream stream = new ByteArrayInputStream(
				disallowStream.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	private void sendAlreadyStream(OutputStream os) {
		String ip = getLocalIpAddress();
		String alreadyStream = "440 " + ip + "\r\n";
		InputStream stream = new ByteArrayInputStream(
				alreadyStream.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}
	
	private void sendStopStream(OutputStream os) {
		String stopStream = "460 ";
		InputStream stream = new ByteArrayInputStream(
				stopStream.getBytes(StandardCharsets.UTF_8));
		FileTransferService.sendCode(stream, os);
	}

	public String getLocalIpAddress() {
		String ipAddress = "";
		
		try {
	        List<NetworkInterface> interfaces = Collections
	                .list(NetworkInterface.getNetworkInterfaces());

	        for (NetworkInterface intf : interfaces) {
	            if (!intf.getName().contains("p2p"))
	                continue;        

	            List<InetAddress> addrs = Collections.list(intf
	                    .getInetAddresses());

	            for (InetAddress addr : addrs) {

	                if (!addr.isLoopbackAddress()) {
	                    String sAddr = addr.getHostAddress().toString();
	                    
	                    if (sAddr.contains("192.168.49.")) {
	                    	ipAddress = sAddr;
                        }
	                }

	            }
	        }
	    } catch (Exception ex) {
	    } 
		
		return ipAddress;
	}

	@Override
	public void onReceive_SendSocket(Socket sendSocket) {
		// TODO Auto-generated method stub
		Socket mSendSocket = sendSocket;

		tempPeer.mSendSocket = mSendSocket;
	}

	@Override
	public void onReceive_ReceiveSocket(Socket receiveSocket) {
		// TODO Auto-generated method stub
		Socket mReceiveSocket = receiveSocket;

		addPeer(tempPeer);
		tempPeer.mReceiveSocket = mReceiveSocket;

		tempPeer.mReceiveThread = new ReceiveSocketAsync(this,
				mReceiveDataListener, receiveSocket,
				mConnectedPeers.indexOf(tempPeer));
		tempPeer.mReceiveThread.start();

		mListener.onConnectComplete();
	}

	@Override
	public void onCompleteReceivedData(int peer) {
		try {
			sendCodeReceivedData(mConnectedPeers.get(peer).mSendSocket
					.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPing(int peer) {
		try {
			sendPingOK(mConnectedPeers.get(peer).mSendSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPingOK(int peer) {
		mConnectedPeers.get(peer).isAlive = true;
	}

	public interface P2PHandleNetworkListener {
		public void onConnectComplete();

		public void onDisconnectComplete();
	}
}