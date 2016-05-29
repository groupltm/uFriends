package activity;

import instance.Info;
import instance.MyBundle;
import instance.MyPeer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utility_class.RealPathUtil;

import mywifip2pkit.ReceiveSocketAsync.SocketReceiverDataListener;
import mywifip2pkit.WifiP2PBroadcast.OnWifiP2PStateListener;
import mywifip2pkit.WifiP2PBroadcast.WifiP2PBroadcastListener;

import com.example.ufriends.R;

import custom_view.ProgressWheel;
import custom_view.RippleBackground;

import adapter.ChatArrayAdapter;
import adapter.DeviceListAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class BrowseFragment extends Fragment implements
		WifiP2PBroadcastListener, SocketReceiverDataListener,
		OnWifiP2PStateListener {

	DeviceListAdapter deviceListAdapter;
	ListView lvDevice;
	ListView lvConnectDevice;
	View mView;
	ProgressWheel mProgress;
	ImageButton btnFind;

	MyBundle mBundle = MyBundle.getInstance();

	Thread checkPeerThread;

	Handler hd_Progress = null;
	Runnable r_Progress = null;

	boolean didSendAvatar = false;
	boolean isActive = false;
	boolean didReceive = false;

	AlertDialog alertDialog;

	RippleBackground rippleBackground;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		checkWifi();
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_browse, container, false);

		lvDevice = (ListView) mView.findViewById(R.id.lvDevice);
		mProgress = (ProgressWheel) mView.findViewById(R.id.progress_wheel);
		btnFind = (ImageButton) mView.findViewById(R.id.btnFind);
		rippleBackground = (RippleBackground) mView.findViewById(R.id.rpFind);

		deviceListAdapter = new DeviceListAdapter(getActivity(),
				R.layout.list_device_item, mBundle.mPeerList);

		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MyPeer peer = mBundle.mPeerList.get(position);
				if (peer.peerInfo._status == 2) {
					mProgress.setVisibility(View.GONE);
					rippleBackground.stopRippleAnimation();

					isActive = true;
					connectToPeer(position);
				} else if (peer.peerInfo._status == 0) {
					showChatView();
				}
			}
		});

		lvDevice.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MyPeer peer = mBundle.mPeerList.get(position);
				if (peer.peerInfo._status == 0) {
					mBundle.mBroadcast.disconnectFromPeer();
					// restartDiscovery();
				}

				return false;
			}
		});

		// lvConnectDevice.setAdapter(deviceConnectListAdapter);
		// lvConnectDevice.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // TODO Auto-generated method stub
		// checkConnectPeer();
		// }
		// });

		btnFind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rippleBackground.startRippleAnimation();

				mProgress.setVisibility(View.VISIBLE);
				if (hd_Progress == null) {
					hd_Progress = new Handler();
				}
				if (r_Progress == null) {
					r_Progress = new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mProgress.setVisibility(View.GONE);
							rippleBackground.stopRippleAnimation();
						}
					};
				}
				hd_Progress.removeCallbacks(r_Progress);
				hd_Progress.postDelayed(r_Progress, 30000);
				// restartDiscovery();
				mBundle.mPeerList.clear();
				mBundle.mBroadcast.advertiseWifiP2P();
			}

		});

		return mView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mBundle.mBroadcast.mListener = this;
		mBundle.mBroadcast.setWifiP2PStateListener(this);

		if (!ChatActivity.isChat) {
			mBundle.mBroadcast.mP2PHandle.setReceiveDataListener(this);
			ChatArrayAdapter.Clear();
			mBundle.mPeerList.clear();
			deviceListAdapter.notifyDataSetChanged();
		}
	}

	public void restartDiscovery() {
		mBundle.mPeerList.clear();

		Map<String, String> serviceInfo = createMap(mBundle.mInfo);
		mBundle.mBroadcast.stopDiscoveryService();
		mBundle.mBroadcast.advertiseService("ufriends", "_tcp", serviceInfo);
		mBundle.mBroadcast.discoverService(new DnsSdTxtRecordListener() {

			@Override
			public void onDnsSdTxtRecordAvailable(String fullDomainName,
					Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
				// TODO Auto-generated method stub
				if (fullDomainName.equals("ufriends._tcp.local.")) {
					if (checkPeerInMyPeerList(mBundle.mPeerList, srcDevice) == -1) {
						Info info = convertMapToInfo(txtRecordMap);
						MyPeer peer = new MyPeer();
						peer.peerDevice = srcDevice;
						peer.peerInfo = info;
						mBundle.mPeerList.add(peer);
						deviceListAdapter.notifyDataSetChanged();
					}
				}
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

	// private void addConnectPeerToList(Info peerInfo, int status){
	// peerInfo._status = status;
	// mBundle.mPeerInfoConnectList.clear();
	// mBundle.mPeerInfoConnectList.add(peerInfo);
	// deviceConnectListAdapter.notifyDataSetChanged();
	// }

	private void checkWifi() {
		WifiManager wifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			showTurnOnWifiAlert();
		}
	}

	private void turnOnWifi() {
		WifiManager wifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
	}

	private void showTurnOnWifiAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());
		alertDialogBuilder
				.setMessage("uFriends needs to enable Wifi to run correctly. Turn on Wifi");
		alertDialogBuilder.setPositiveButton("Allow",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						turnOnWifi();
					}
				});

		alertDialogBuilder.setNegativeButton("Disallow",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActivity().finish();
					}
				});

		AlertDialog dialog = alertDialogBuilder.create();
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Button positiveButton = ((AlertDialog) dialog)
						.getButton(DialogInterface.BUTTON_POSITIVE);
				positiveButton.setBackgroundColor(Color.BLUE);
				positiveButton.invalidate();
			}
		});

		alertDialogBuilder.show();
	}

	private void connectToPeer(int position) {
		WifiP2pDevice device = mBundle.mPeerList.get(position).peerDevice;
		final WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 15;

		mBundle.mBroadcast.connectPeer(config);
	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub

		// Check follow service discovery
		// List<WifiP2pDevice> collecPeers = new ArrayList<WifiP2pDevice>();
		// collecPeers.addAll(peers.getDeviceList());
		// for (MyPeer peer:mBundle.mPeerList){
		// int i = checkAvailablePeers(collecPeers, peer.peerDevice);
		// int peerPosition = mBundle.mPeerList.indexOf(peer);
		// if(i == -1){
		// mBundle.mPeerList.remove(peerPosition);
		// } else {
		// WifiP2pDevice tempPeer = collecPeers.get(i);
		// if (tempPeer.status == 0 || tempPeer.status == 1){
		// peer.peerInfo._status = tempPeer.status;
		// mBundle.mPeerList.remove(peerPosition);
		// mBundle.mPeerList.add(0, peer);
		// }
		// }
		// }
		//
		// checkConnectedPeer(collecPeers);
		//
		// deviceListAdapter.notifyDataSetChanged();

		// check for normal discovery
		List<WifiP2pDevice> collecPeers = new ArrayList<WifiP2pDevice>();
		collecPeers.addAll(peers.getDeviceList());
		for (WifiP2pDevice peerDevice : collecPeers) {
			int position = checkPeerInMyPeerList(mBundle.mPeerList, peerDevice);
			if (position == -1) {
				MyPeer peer = new MyPeer();
				peer.peerDevice = peerDevice;
				peer.peerInfo._name = peerDevice.deviceName;
				mBundle.mPeerList.add(peer);
			} else {
				int peerStatus = peerDevice.status;
				if (peerStatus == 0 || peerStatus == 1) {
					MyPeer peer = mBundle.mPeerList.get(position);
					peer.peerInfo._status = peerStatus;
					mBundle.mPeerList.remove(position);
					mBundle.mPeerList.add(0, peer);
				}
			}
		}

		deviceListAdapter.notifyDataSetChanged();
	}

	private int checkAvailablePeers(List<WifiP2pDevice> peers,
			WifiP2pDevice peer) {
		for (WifiP2pDevice tempPeer : peers) {
			if (tempPeer.deviceAddress.equals(peer.deviceAddress)) {
				return peers.indexOf(tempPeer);
			}
		}

		return -1;
	}

	private int checkPeerInMyPeerList(List<MyPeer> mPeerList,
			WifiP2pDevice peerDevice) {
		for (MyPeer peer : mPeerList) {
			if (peer.peerDevice.deviceAddress.equals(peerDevice.deviceAddress)) {
				return mPeerList.indexOf(peer);
			}
		}

		return -1;
	}

	private void checkConnectedPeer(List<WifiP2pDevice> peers) {
		for (WifiP2pDevice peer : peers) {
			if (checkPeerInMyPeerList(mBundle.mPeerList, peer) == -1) {
				if (peer.status == 0 || peer.status == 1) {
					MyPeer mPeer = new MyPeer();
					mPeer.peerDevice = peer;
					mBundle.mPeerList.add(0, mPeer);
				}
			}
		}
	}

	private void checkPeerInfoInPeerList(Info peerInfo) {
		MyPeer peer = mBundle.mPeerList.get(0);
		if (peer.peerInfo._status == 0) {
			peer.peerInfo = peerInfo;
			peer.peerInfo._status = 0;
			deviceListAdapter.notifyDataSetChanged();
		}
	}
	
	private void setAvatarToConnectPeer(Bitmap avatar){
		MyPeer peer = mBundle.mPeerList.get(0);
		if (peer.peerInfo._status == 0) {
			peer.peerAvatar = avatar;
			deviceListAdapter.notifyDataSetChanged();
		}
	}

	private void showChatView() {
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		startActivity(intent);
	}

	private void sendInfo() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(out);
			os.writeObject(mBundle.mInfo);
			byte[] bInfo = out.toByteArray();
			InputStream is = new ByteArrayInputStream(bInfo);
			mBundle.mBroadcast.sendMessage(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendAvatar() {
		InputStream is = null;
		if (!mBundle.mInfo._imagePath.equals("")) {
			File imgFile = new File(mBundle.mInfo._imagePath);
			if (imgFile.exists()) {
				Bitmap myBitmap = RealPathUtil.createBitmapWithPath(
						mBundle.mInfo._imagePath, RealPathUtil.WidthAvatar,
						RealPathUtil.HeightAvatar);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				myBitmap.compress(CompressFormat.PNG, 0, bos);
				byte[] bitmapdata = bos.toByteArray();
				is = new ByteArrayInputStream(bitmapdata);
			}
		} else {
			is = getActivity().getResources().openRawResource(
					R.drawable.applogo_256);
		}

		mBundle.mBroadcast.sendImage(is);
	}

	@Override
	public void onConnection() {
		// TODO Auto-generated method stub
		mBundle.mBroadcast.stopDiscoveryService();

		if (isActive == true) {
			sendInfo();
			Log.d("send", "send info");
		}

	}

	@Override
	public void onDisconnect() {
		if (!mBundle.isKilled) {
			if (mBundle.peerInfo != null) {
				if (alertDialog == null) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							getActivity());
					alertDialog = alertDialogBuilder.create();
					alertDialog.setTitle("DISCONNECTED!!!");
					alertDialog.setMessage("You are disconnected with "
							+ mBundle.peerInfo._name);
				}

				if (ChatActivity.isChat && !alertDialog.isShowing()) {
					alertDialog.show();
				}
			}

			ChatActivity.isChat = false;
			mBundle.mBroadcast.mP2PHandle.setReceiveDataListener(this);
			ChatArrayAdapter.Clear();

			mBundle.mPeerList.clear();
			deviceListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onReceiveMessageData(byte[] data) {
		// TODO Auto-generated method stub
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		try {
			ObjectInputStream is = new ObjectInputStream(in);
			mBundle.peerInfo = (Info) is.readObject();
			Handler hd = new Handler(getContext().getMainLooper());
			hd.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					checkPeerInfoInPeerList(mBundle.peerInfo);
				}
			});

		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onReceiveImageData(final byte[] data) {
		// TODO Auto-generated method stub
		Handler hd = new Handler(getActivity().getMainLooper());
		hd.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBundle.peerAvatar = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				setAvatarToConnectPeer(mBundle.peerAvatar);
			}
		});
		if (isActive == false) {
			sendInfo();
		} else {
			didSendAvatar = false;
			isActive = false;
			showChatView();
		}
	}

	@Override
	public void onCompleteSendData() {
		// TODO Auto-generated method stub
		if (!didSendAvatar) {
			sendAvatar();
			Log.d("send", "send avatar");
			didSendAvatar = true;
		} else {
			if (!isActive) {
				didSendAvatar = false;
				isActive = false;
				showChatView();
			}
		}
	}

	@Override
	public void onStateEnable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStateDisable() {
		// TODO Auto-generated method stu
	}
}
