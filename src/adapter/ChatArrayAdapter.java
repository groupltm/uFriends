package adapter;

import instance.ChatMessage;
import instance.MyBundle;
import instance.MyPeer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import utility_class.RealPathUtil;

import com.example.ufriends.R;

import activity.ImageActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

	private TextView chatText;
	private TextView timeText;
	private static List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
	private Context context;
	private MyBundle mBundle;

	private ImageView avatar;

	@Override
	public void add(ChatMessage object) {
		chatMessageList.add(object);
		super.add(object);
	}

	public void clear() {
		chatMessageList.clear();
	}
	
	public static void Clear(){
		chatMessageList.clear();
	}

	public ChatArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
		mBundle = MyBundle.getInstance();
	}

	public int getCount() {
		return this.chatMessageList.size();
	}

	public ChatMessage getItem(int index) {
		return this.chatMessageList.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ChatMessage chatMessageObj = getItem(position);
		View row = convertView;
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		

		if (!chatMessageObj.isImage) {
			if (chatMessageObj.left) {
				row = inflater.inflate(R.layout.list_chat_left_item, null);
				avatar = (ImageView) row.findViewById(R.id.imvAvatar);
				mBundle.peerAvatar = Bitmap.createScaledBitmap(
						mBundle.peerAvatar, 64, 64, false);
				avatar.setImageBitmap(mBundle.peerAvatar);
			} else {
				row = inflater.inflate(R.layout.list_chat_right_item, null);
				avatar = (ImageView) row.findViewById(R.id.imvAvatar);

				mBundle.myAvatar = Bitmap.createScaledBitmap(mBundle.myAvatar,
						64, 64, false);

				avatar.setImageBitmap(mBundle.myAvatar);
			}
			chatText = (TextView) row.findViewById(R.id.txtMsg);
			chatText.setText(chatMessageObj.message);

		} else {
			if (chatMessageObj.left) {
				row = inflater.inflate(R.layout.list_image_left_item, null);
				avatar = (ImageView) row.findViewById(R.id.imvAvatar);
				mBundle.peerAvatar = Bitmap.createScaledBitmap(
						mBundle.peerAvatar, 64, 64, false);
				avatar.setImageBitmap(mBundle.peerAvatar);
			} else {
				row = inflater.inflate(R.layout.list_image_right_item, null);
				avatar = (ImageView) row.findViewById(R.id.imvAvatar);

				mBundle.myAvatar = Bitmap.createScaledBitmap(mBundle.myAvatar,
						64, 64, false);

				avatar.setImageBitmap(mBundle.myAvatar);
			}

			ImageView imvShareImage = (ImageView) row
					.findViewById(R.id.imvShareImage);
			
			imvShareImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int position = mBundle.receivedImagePath.indexOf(chatMessageObj.message);
					showImageActivity(position);
				}
			});
			
			File imgFile = new File(chatMessageObj.message);

			if (imgFile.exists()) {

				Bitmap myBitmap = RealPathUtil.createBitmapWithPath(
						chatMessageObj.message,100,100);

				imvShareImage.setImageBitmap(myBitmap);
			}
		}
		
		timeText = (TextView)row.findViewById(R.id.txtSeenDay);
		long date = System.currentTimeMillis(); 

		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		String dateString = sdf.format(date);  
		timeText.setText(dateString);

		return row;
	}
	
	private void showImageActivity(int position){
		Intent intent = new Intent(context, ImageActivity.class);
		intent.putExtra("position", position);
		context.startActivity(intent);
	}
}
