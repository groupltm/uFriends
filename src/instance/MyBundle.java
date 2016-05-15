package instance;

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

import mywifip2pkit.WifiP2PBroadcast;

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

	public List<MyPeer> mPeerList;
	//public List<Info> mPeerInfoList;
	public List<Info> mPeerInfoConnectList;
	public WifiP2PBroadcast mBroadcast;
	public boolean isConnect;
	public Info mInfo;
	private static MyBundle mBundle = new MyBundle();

	private MyBundle() {
		// TODO Auto-generated constructor stub
		mPeerList = new ArrayList<MyPeer>();
		//mPeerInfoList = new ArrayList<>();
		
		mPeerInfoConnectList = new ArrayList<>();
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
		File file = new File("data/data/com.example.ufriends/test.json");
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
