package group_1312141_1312269.ufriends;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Environment;
import android.provider.OpenableColumns;

public class MyBundle implements Serializable {

	public List<WifiP2pDevice> mPeerList;
	public WifiP2PBroadcast mBroadcast;
	public boolean isConnect;
	public Info mInfo;
	private static MyBundle mBundle = new MyBundle();

	public class Info implements Serializable {
		byte[] _image;
		String _name;

		int _age;
		boolean _sex;

		public Info() {
			_image = null;
			_name = "anonymous";
			_age = 0;
			_sex = false;
		}
		
		public void setInfo(byte[] image, String name, int age, boolean sex){
			_image = image;
			_name = name;
			_age = age;
			_sex = sex;
		}
	}

	private MyBundle() {
		// TODO Auto-generated constructor stub
		mPeerList = new ArrayList<WifiP2pDevice>();
		mInfo = new Info();

	}

	public static MyBundle getInstance() {
		return mBundle;
	}

	public void getInfoFromJSONFile(Context context) throws JsonSyntaxException,
			JsonIOException, IOException {
		Gson gson = new Gson();

		File file = new File("data/data/com.example.ufriends/test.json");
		
		InputStream inputStream = context.openFileInput("test.json");
		String jsonString = "";
		
        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            jsonString = stringBuilder.toString();
        }
		if (file.exists()) {
			// 1. JSON to Java object, read it from a file.
			mInfo = gson.fromJson(jsonString, Info.class);
		}
	}

	public void setInfoToJSONFile(Context context) throws JsonIOException,
			IOException {

		Gson gson = new Gson();

		// 1. Java object to JSON, and save into a file
		File external = Environment.getExternalStorageDirectory();
		File file = new File(external, "test.json");
		if (!file.exists()) {
			file.createNewFile();
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.fromFile(file)));
		}
		String jsonString = gson.toJson(mInfo);
		OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(
				"test.json", Context.MODE_PRIVATE));
		out.write(jsonString);
		out.close();
	}
}
