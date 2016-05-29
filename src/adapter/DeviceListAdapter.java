package adapter;

import instance.Info;
import instance.MyPeer;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ufriends.R;

public class DeviceListAdapter extends ArrayAdapter<MyPeer> {

	Context mContext;
	int mResource;
	// List <WifiP2pDevice> mList;
	List<MyPeer> mInfoList;

	public DeviceListAdapter(Context context, int resource,
			List<MyPeer> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub

		mContext = context;
		mResource = resource;
		// mList = objects;
		mInfoList = objects;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View v = convertView;

		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(mResource, null);

		TextView tvDeviceName = (TextView) v.findViewById(R.id.tvLabel);
		TextView tvSubInfo = (TextView) v.findViewById(R.id.tvSubInfo);
		TextView tvStatus = (TextView)v.findViewById(R.id.tvStatus);
		
		MyPeer peer = mInfoList.get(position);
		if (peer.peerAvatar != null){
			ImageView imvAvatar = (ImageView)v.findViewById(R.id.imvAvatar);
			imvAvatar.setImageBitmap(peer.peerAvatar);
		}

		Info info = peer.peerInfo;
		tvDeviceName.setText(info._name);
		
		if (info._age != 0){
			String subInfo;

			if (info._sex) {
				subInfo = "Sex: Male; " + "Age: " + String.valueOf(info._age);
			} else {
				subInfo = "Sex: Female; " + "Age: " + String.valueOf(info._age);
			}
			tvSubInfo.setText(subInfo);
		}else {
			tvSubInfo.setVisibility(View.GONE);
		}
		
		
		if (info._status == 2){
			tvStatus.setText("Available");
			tvStatus.setTextColor(Color.GREEN);
		}else if (info._status == 1){
			tvStatus.setText("Connecting");
			tvStatus.setTextColor(Color.BLUE);
		}else {
			tvStatus.setText("Connected");
			tvStatus.setTextColor(Color.RED);
		}

		return v;
	}

}
