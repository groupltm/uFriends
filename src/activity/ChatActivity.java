package activity;

import instance.ChatMessage;
import instance.MyBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Text;

import utility_class.RealPathUtil;

import mywifip2pkit.ReceiveSocketAsync.OnStreamListener;
import mywifip2pkit.WifiP2PBroadcast;
import mywifip2pkit.ReceiveSocketAsync.SocketReceiverDataListener;
import mywifip2pkit.WifiP2PBroadcast.WifiP2PBroadcastListener;

import adapter.ChatArrayAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ufriends.R;

public class ChatActivity extends AppCompatActivity implements
		SocketReceiverDataListener, WifiP2PBroadcastListener, OnStreamListener {

	public static int NOTIFICATION_ID = 2303;
	
	WifiP2PBroadcast mBroadcast;
	MyBundle mBundle;

	ChatArrayAdapter mChatAdapter;

	ListView lvChat;
	EditText edtChatIn;
	Button btnSend;
	Button btnCamera;
	Button btnFile;
	Button btnStream;

	boolean side = false;
	boolean isReceiveRequestStream = false;

	Toolbar mToolbar;

	Uri mImageUri;
	String mImagePath;
	
	AlertDialog alertDialog;
	NotificationManager notiManager;
	
	Handler mhd;
	
	boolean isActive;
	
	public static boolean isChat = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);

		mBundle = MyBundle.getInstance();
		isChat = true;

		mToolbar = (Toolbar) findViewById(R.id.chatToolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(mBundle.peerInfo._name);
		final Drawable upArrow = getResources().getDrawable(
				R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		upArrow.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(upArrow);

		lvChat = (ListView) findViewById(R.id.lvChat);
		edtChatIn = (EditText) findViewById(R.id.edtChatIn);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnCamera = (Button) findViewById(R.id.btnCamera);
		btnFile = (Button) findViewById(R.id.btnFile);
		btnStream = (Button) findViewById(R.id.btnStream);

		mBroadcast = mBundle.mBroadcast;
		mBroadcast.mListener = this;

		mChatAdapter = new ChatArrayAdapter(this, R.layout.list_chat_left_item);

		edtChatIn.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					return sendChatMessage();
				}
				return false;
			}
		});

		btnSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				sendChatMessage();
			}
		});

		btnCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendImageCapture();
			}
		});

		btnFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendImageInGallery();
			}
		});
		
		btnStream.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBundle.mBroadcast.requestStream();
			}
		});

		lvChat.setAdapter(mChatAdapter);

		mChatAdapter.registerDataSetObserver(new DataSetObserver() {

			@Override
			public void onChanged() {
				// TODO Auto-generated method stub
				super.onChanged();
				lvChat.setSelection(mChatAdapter.getCount() - 1);
			}
		});
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialog = alertDialogBuilder.create();
		alertDialog.setTitle("DISCONNECTED!!!");
		alertDialog.setMessage("You are disconnected with "
				+ mBundle.peerInfo._name);
		
		notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		mhd = new Handler(getMainLooper());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isActive = true;
		mBundle.mBroadcast.registerWithContext(this);
		mBroadcast.mP2PHandle.setReceiveDataListener(this);
		mBroadcast.mP2PHandle.setStreamListener(this);
		
		if (isReceiveRequestStream){
			isReceiveRequestStream = false;
			showAlertRequestStream();
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActive = false;
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStorageDirectory();
		File f = Environment.getRootDirectory();
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.fromFile(image)));
		mImageUri = Uri.fromFile(image);
		mImagePath = image.getAbsolutePath();
		return image;
	}

	private InputStream convertStringToIs(String s) {
		InputStream stream = new ByteArrayInputStream(
				s.getBytes(StandardCharsets.UTF_8));
		return stream;
	}
	
	private String createImageWithData(byte[] data) {

		ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
		os.write(data, 0, data.length);

		try {
			final File f = new File(Environment.getExternalStorageDirectory()
					+ "/" + getApplicationContext().getPackageName()
					+ "/wifip2pImageShare-" + System.currentTimeMillis()
					+ ".jpg");

			File dirs = new File(f.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			f.createNewFile();
			os.writeTo(new FileOutputStream(f));
			getApplicationContext().sendBroadcast(
					new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
							.fromFile(f)));
			return f.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void createNotification(String msg){
		Intent mIntent = new Intent(this, ChatActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(ChatActivity.class);
		stackBuilder.addNextIntent(mIntent);

		//mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder notiBuilder = new Notification.Builder(this);
		notiBuilder.setContentTitle(mBundle.peerInfo._name);
		notiBuilder.setContentText(msg);	
		
		Bitmap newBm = RealPathUtil.createBitmapWithBitmap(mBundle.peerAvatar, RealPathUtil.WidthAvatar, RealPathUtil.HeightAvatar);
		notiBuilder.setLargeIcon(newBm);
		notiBuilder.setContentIntent(pIntent);
		notiBuilder.setSmallIcon(R.drawable.applogo);
		notiBuilder.setVibrate(new long[]{100, 100, 100, 100, 100});
		notiBuilder.setLights(Color.RED, 3000, 3000);
		
		notiBuilder.setSound(Uri.parse("android.resource://"
	            + this.getPackageName() + "/" + R.raw.capisci));
		
		Notification noti = new Notification.InboxStyle(notiBuilder).addLine("OK OK").build();
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notiManager.notify(NOTIFICATION_ID, noti);
	}
	
	private void createToast(String msg){
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.custom_toast_chatmessage, null);
		ImageView imvAvatar = (ImageView)layout.findViewById(R.id.imvAvatar);
		TextView tvPeerName = (TextView)layout.findViewById(R.id.tvPeerName);
		TextView tvMessage = (TextView)layout.findViewById(R.id.tvMessage);
		TextView tvTime = (TextView)layout.findViewById(R.id.tvTime);
		
		Bitmap newBm = RealPathUtil.createBitmapWithBitmap(mBundle.peerAvatar, RealPathUtil.WidthAvatar, RealPathUtil.HeightAvatar);
		imvAvatar.setImageBitmap(newBm);
		tvPeerName.setText(mBundle.peerInfo._name);
		tvTime.setText(msg);
		
		long date = System.currentTimeMillis(); 

		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		String dateString = sdf.format(date);  
		tvMessage.setText(dateString);
		
		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	private void showAlertRequestStream(){
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Request Stream");
		alertDialogBuilder.setMessage(mBundle.peerInfo._name + " want to share  his camera with you!!!");
		alertDialogBuilder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mBundle.mBroadcast.allowStream();
			}
		}); 
		
		alertDialogBuilder.setNegativeButton("Disallow", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mBundle.mBroadcast.disallowStream();
			}
		});
		alertDialogBuilder.show();
	}
	
	private void showStreamerView(){
		Intent intent = new Intent(this, StreamerActivity.class);
		startActivity(intent);
	}
	
	private void showPlayerView(String streamerIP){
		Intent intent = new Intent(this, PlayerActivity.class);
		intent.putExtra("IPSTREAMER", streamerIP);
		startActivity(intent);
	}

	private boolean sendChatMessage() {
		String msg = edtChatIn.getText().toString();
		if (!msg.equals("")) {
			side = false;
			mChatAdapter.add(new ChatMessage(side, edtChatIn.getText()
					.toString(), false));
			edtChatIn.setText("");

			InputStream is = convertStringToIs(msg);
			mBroadcast.sendMessage(is);
		}

		return true;
	}

	private void sendImageInGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 100);
	}

	private void sendImageCapture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				startActivityForResult(takePictureIntent, 120);
			}
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Uri uri = null;
			if (requestCode == 100) {
				uri = data.getData();
				String realPath;
				if (Build.VERSION.SDK_INT < 11) {
					realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(
							getApplicationContext(), uri);
				} else if (Build.VERSION.SDK_INT < 20) {
					realPath = RealPathUtil.getRealPathFromURI_API11to19(
							getApplicationContext(), uri);
				} else {
					realPath = RealPathUtil.getRealPathFromURI_API20(
							getApplicationContext(), uri);
				}
				mChatAdapter.add(new ChatMessage(false, realPath, true));
				mBundle.receivedImagePath.add(realPath);
			} else if(requestCode == 120){
				uri = mImageUri;
				mChatAdapter.add(new ChatMessage(false, mImagePath, true));
				mBundle.receivedImagePath.add(mImagePath.toString());
			}

			ContentResolver cr = getContentResolver();
			final InputStream is;

			try {
				is = cr.openInputStream(uri);
				Thread send_thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBroadcast.sendImage(is);
					}
				});
				send_thread.start();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onReceiveMessageData(byte[] data) {
		// TODO Auto-generated method stub
		final String msg = new String(data, StandardCharsets.UTF_8);
		side = true;

		Handler hd = new Handler(getMainLooper());

		hd.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				mChatAdapter.add(new ChatMessage(side, msg, false));
				if (!isActive){
					createNotification(msg);
					createToast(msg);
				}
			}
		});

	}

	@Override
	public void onCompleteSendData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveImageData(byte[] data) {
		// TODO Auto-generated method stub
		final String imagePath = createImageWithData(data);
		mBundle.receivedImagePath.add(imagePath);
		Handler hd = new Handler(getMainLooper());
		hd.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mChatAdapter.add(new ChatMessage(true, imagePath, true));
				if (!isActive){
					String msg = mBundle.peerInfo._name + " have just sended an image for you!!!"; 
					createNotification(msg);
					createToast(msg);
				}
			}
		});

	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect() {	
		// TODO Auto-generated method stub
		if (!mBundle.isKilled){
			if (mBundle.peerInfo != null) {
				if (isActive){
					if (!alertDialog.isShowing()){
						alertDialog.show();
					}
				}else{
					if (isChat){
						String msg = "You disconnected with " + mBundle.peerInfo._name;
						createNotification(msg);
						createToast(msg);
					}			
				}
			}

			isChat = false;
		}		
	}
	
	@Override
	public void onReceiveRequestStream() {
		// TODO Auto-generated method stub
		mhd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String msg = mBundle.peerInfo._name + " want to share live stream with you!!!";
				if (!isChat){
					createNotification(msg);
					createToast(msg);
					isReceiveRequestStream = true;
				}			
				showAlertRequestStream();
			}
		});
	}

	@Override
	public void onReceiveAllowStream() {
		// TODO Auto-generated method stub
		
		mhd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				showStreamerView();
			}
		});
		
	}

	@Override
	public void onReceiveDisallowStream() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAlreadyStream(final String streamerIP) {
		// TODO Auto-generated method stub
		Log.e("streamip", streamerIP);
		mhd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				showPlayerView(streamerIP);
			}
		});
		
	}

	@Override
	public void onStopStream() {
		// TODO Auto-generated method stub
		
	}
}
