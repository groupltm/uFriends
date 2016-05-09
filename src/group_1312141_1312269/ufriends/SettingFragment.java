package group_1312141_1312269.ufriends;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.ufriends.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class SettingFragment extends Fragment{

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
		mInfo = convertInfoToStringList(myBundle.mInfo);
	}
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		mView = inflater.inflate(R.layout.fragment_setting, container, false);		
		
		mAvatar = (ImageView)mView.findViewById(R.id.myAvatar);
		lvMyInfo = (ListView)mView.findViewById(R.id.lvInfo);
		lvMyInfo.setAdapter(new InfoArrayAdapter(getActivity(), R.layout.list_info_item, mInfo));
		
		if (myBundle.mInfo._imagePath.equals("")){
			mAvatar.setImageResource(R.drawable.ic_launcher);
		}else{
			File imgFile = new  File(myBundle.mInfo._imagePath);

			if(imgFile.exists()){

			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

			    mAvatar.setImageBitmap(myBitmap);
			}
		}
		
		return mView;
	}
	
	private List<String> convertInfoToStringList(Info info){
		List<String> infoList = new ArrayList<>();
		
		infoList.add(info._name);
		infoList.add(String.valueOf(info._age));
		if (info._sex){
			infoList.add("Male");
		}else{
			infoList.add("Female");
		}
		
		return infoList;
	}
}
