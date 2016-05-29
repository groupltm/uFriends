package activity;


import instance.MyBundle;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import service.MyService;

import mywifip2pkit.WifiP2PBroadcast;
import mywifip2pkit.ReceiveSocketAsync.SocketReceiverDataListener;
import mywifip2pkit.WifiP2PBroadcast.WifiP2PBroadcastListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.sip.SipRegistrationListener;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ufriends.R;
import com.google.gson.JsonIOException;

public class MainActivity extends AppCompatActivity {

	// DeviceListAdapter deviceListAdapter;
	// ListView lvDevice;
	//
	// IntentFilter filter = new IntentFilter();
	//
	// public static MyBundle mBundle = new MyBundle();
	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// // TODO Auto-generated method stub
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.activity_main);
	//
	// lvDevice = (ListView)findViewById(R.id.lvDevice);
	//
	// deviceListAdapter = new DeviceListAdapter(this,
	// R.layout.list_device_item, mBundle.mPeerList);
	// lvDevice.setAdapter(deviceListAdapter);
	// lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// // TODO Auto-generated method stub
	// connectToPeer(position);
	// }
	// });
	//
	// setBroadcast();
	// }
	//
	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// // TODO Auto-generated method stub
	// super.onSaveInstanceState(outState);
	//
	//
	// }
	//
	// private void setBroadcast() {
	//
	// mBundle.mBroadcast = new WifiP2PBroadcast(this);
	// mBundle.mBroadcast.setManager();
	//
	// mBundle.mBroadcast.mListener = this;
	//
	// filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	// filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	// filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	// filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	// filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
	//
	// mBundle.mBroadcast.register(filter);
	// mBundle.mBroadcast.advertiseWifiP2P();
	// }
	//
	//
	// public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {
	//
	// Context mContext;
	// int mResource;
	// List <WifiP2pDevice> mList;
	//
	// public DeviceListAdapter(Context context, int resource,
	// List<WifiP2pDevice> objects) {
	// super(context, resource, objects);
	// // TODO Auto-generated constructor stub
	//
	// mContext = context;
	// mResource = resource;
	// mList = objects;
	// }
	//
	// @SuppressLint("ViewHolder") @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// // TODO Auto-generated method stub
	//
	// View v = convertView;
	//
	// LayoutInflater inflater = getLayoutInflater();
	// v = inflater.inflate(mResource, null);
	//
	// TextView tvDeviceName = (TextView)v.findViewById(R.id.tvLabel);
	// tvDeviceName.setText(mList.get(position).deviceName);
	//
	// return v;
	// }
	//
	// }
	//
	// private void connectToPeer(int position){
	// WifiP2pDevice device = mBundle.mPeerList.get(position);
	// final WifiP2pConfig config = new WifiP2pConfig();
	// config.deviceAddress = device.deviceAddress;
	// config.wps.setup = WpsInfo.PBC;
	// config.groupOwnerIntent = 15;
	//
	// mBundle.mBroadcast.connectPeer(config);
	// }
	//
	// @Override
	// public void onPeers(WifiP2pDeviceList peers) {
	// // TODO Auto-generated method stub
	// mBundle.mPeerList.clear();
	// mBundle.mPeerList.addAll(peers.getDeviceList());
	// deviceListAdapter.notifyDataSetChanged();
	// }
	//
	// @Override
	// public void onConnection() {
	// // TODO Auto-generated method stub
	//Intent intent = new Intent(this, ChatActivity.class);
	// startActivity(intent);
	// }
	//
	// @Override
	// public void onDisconnect() {
	// // TODO Auto-generated method stub
	// mBundle.mBroadcast.advertiseWifiP2P();
	// }

	private Toolbar toolbar;
	private TabLayout tabLayout;
	public static ViewPager viewPager;
	MyBundle mBundle;
	IntentFilter filter = new IntentFilter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//addShortcutIcon();
		
		mBundle = MyBundle.getInstance();

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("");

		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		
		setupTabIcon();

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				if (arg0 == 1) {
//					if (mBundle.isConnect == true){
//						ChatFragment chatfm = (ChatFragment) ((ViewPagerAdapter) viewPager
//								.getAdapter()).getItem(1);
//						mBundle.mBroadcast.mP2PHandle.setReceiveDataListener((SocketReceiverDataListener)chatfm);
//					}			
				}else if (arg0 == 0){
					BrowseFragment browserfm = (BrowseFragment) ((ViewPagerAdapter) viewPager
							.getAdapter()).getItem(0);
					//browserfm.restartDiscovery();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		
		if (mBundle.mBroadcast == null){
			mBundle.setBroadcast(getApplicationContext());
			
		}
		
		if (mBundle.myAvatar == null){
			mBundle.setMyAvatar(this);
		}
		
		try {
			mBundle.getInfoFromJSONFile(getApplicationContext());
			//mBundle.setInfoToJSONFile(getApplicationContext());
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startMyService();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mBundle.mBroadcast.registerWithContext(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mBundle.mBroadcast.unregister();
	}
	
//	private void addShortcutIcon() {
//		// shorcutIntent object
//		Intent shortcutIntent = new Intent(getApplicationContext(),
//				MainActivity.class);
//
//		//shortcutIntent.setAction(Intent.ACTION_MAIN);
//
//		// shortcutIntent is added with addIntent
//		Intent addIntent = new Intent();
//		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "uFriends");
//		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//				Intent.ShortcutIconResource.fromContext(
//						getApplicationContext(), R.drawable.applogo));
//
//		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
//		// finally broadcast the new Intent
//		getApplicationContext().sendBroadcast(addIntent);
//	}
	
	private void startMyService(){
		Intent intent = new Intent(getApplicationContext(), MyService.class);
		startService(intent);
	}
	
	public void setCurrentTab(int position) {
		viewPager.setCurrentItem(position, true);
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(
				getSupportFragmentManager());
		adapter.addFragment(new BrowseFragment(), "Browse");
		//adapter.addFragment(new ChatFragment(), "Chat");
		adapter.addFragment(new SettingFragment(), "Setting");
		viewPager.setAdapter(adapter);

	}
	
	private void setupTabIcon(){
		tabLayout.getTabAt(0).setIcon(R.drawable.ic_search_white_24dp);
		tabLayout.getTabAt(1).setIcon(R.drawable.ic_settings_white_24dp);
	}

	class ViewPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();

		public ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}

		@Override
		public int getCount() {
			return mFragmentList.size();
		}

		public void addFragment(Fragment fragment, String title) {
			mFragmentList.add(fragment);
			mFragmentTitleList.add(title);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			//return mFragmentTitleList.get(position);
			
			return null;
		}
	}
}
