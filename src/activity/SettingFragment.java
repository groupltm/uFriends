package activity;

import instance.MyBundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import utility_class.RealPathUtil;

import com.example.ufriends.R;
import com.google.gson.JsonIOException;

import adapter.InfoArrayAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

public class SettingFragment extends Fragment {

	MyBundle myBundle;
	List<String> mInfo;

	ImageView mAvatar;
	ListView lvMyInfo;

	View mView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		myBundle = MyBundle.getInstance();
		mInfo = myBundle.mInfo.convertInfoToStringList();
		mInfo.remove(0);
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_setting, container, false);

		mAvatar = (ImageView) mView.findViewById(R.id.myAvatar);
		lvMyInfo = (ListView) mView.findViewById(R.id.lvInfo);
		lvMyInfo.setAdapter(new InfoArrayAdapter(getActivity(),
				R.layout.list_info_item, mInfo));

		setAvatar();

		mAvatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openGalery();
			}
		});

		return mView;
	}

	private void setAvatar() {
		if (myBundle.mInfo._imagePath.equals("")) {
			mAvatar.setImageResource(R.drawable.applogo_256);
		} else {
			File imgFile = new File(myBundle.mInfo._imagePath);

			if (imgFile.exists()) {

				Bitmap myBitmap = RealPathUtil.createBitmapWithPath(
						myBundle.mInfo._imagePath,
						RealPathUtil.WidthAvatar,
						RealPathUtil.HeightAvatar);
				myBundle.myAvatar = myBitmap;

				mAvatar.setImageBitmap(myBitmap);
			}
		}
	}

	private void openGalery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 100);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		// Uri uri = data.getData();
		// Bitmap bitmap;
		// try {
		// bitmap =
		// MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
		// uri);
		// mAvatar.setImageBitmap(bitmap);
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

//		 String realPath;
//		 // SDK < API11
//		 if (Build.VERSION.SDK_INT < 11)
//		 realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getContext(),
//		 data.getData());
//		
//		 // SDK >= 11 && SDK < 19
//		 else if (Build.VERSION.SDK_INT < 19)
//		 realPath = RealPathUtil.getRealPathFromURI_API11to18(getContext(),
//		 data.getData());
//		
//		 // SDK > 19 (Android 4.4)
//		 else
//		 realPath = RealPathUtil.getRealPathFromURI_API19(getContext(),
//		 data.getData());
//		 // Log.d(TAG, String.valueOf(bitmap));
//		
//		 myBundle.mInfo._imagePath = realPath;
//		 try {
//		 myBundle.setInfoToJSONFile(getContext());
//		 } catch (JsonIOException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 } catch (IOException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 }
//		 setAvatar();
		if (resultCode == Activity.RESULT_OK){
			Uri uri = data.getData();
			String realPath;
			if (Build.VERSION.SDK_INT < 11){
				realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity().getApplicationContext(), uri);
			}else if (Build.VERSION.SDK_INT < 20){
				realPath = RealPathUtil.getRealPathFromURI_API11to19(getActivity().getApplicationContext(), uri);
			}else {
				realPath = RealPathUtil.getRealPathFromURI_API20(getActivity().getApplicationContext(), uri);
			}
			myBundle.mInfo._imagePath = realPath;
			try {
				myBundle.setInfoToJSONFile(getContext());
			} catch (JsonIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setAvatar();
		}
	}

	public String getAbsolutePath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null,
				null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			String realPath = cursor.getString(column_index);
			return realPath;
		} else{
			return null;
		}
			
	}
}
