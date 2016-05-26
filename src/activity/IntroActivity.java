package activity;

import instance.MyBundle;

import java.io.File;
import java.io.IOException;

import com.example.ufriends.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class IntroActivity extends Activity {
	
	public static boolean FLAG_ALIVE = false;
	MyBundle mBundle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (!isTaskRoot())
        {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction(); 
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;       
            }
        }
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		if (!appInstalledOrNot()) {
			addShortcutIcon();
		}
		
		Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/IntimaScriptOne.otf");
	    TextView myTextView = (TextView)findViewById(R.id.tvAppName);
	    myTextView.setTypeface(myTypeface);
		
		mBundle = MyBundle.getInstance();

		Handler hd = new Handler();
		hd.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivityForResult(intent, 100);
			}
		}, 3000);
		
		FLAG_ALIVE = true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		FLAG_ALIVE = false;
//		Handler hd = new Handler();
//		hd.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				if (!FLAG_ALIVE)
//					mBundle.mBroadcast.disconnectFromPeer();
//			}
//		}, 2000);
	}

	private boolean appInstalledOrNot() {
		File file = new File("data/data/com.example.ufriends/InstallFile");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			return false;
		}
		return true;
	}

	private void addShortcutIcon() {
		// shorcutIntent object
		Intent shortcutIntent = new Intent(getApplicationContext(),
				IntroActivity.class);

		//shortcutIntent.setAction(Intent.ACTION_MAIN);

		// shortcutIntent is added with addIntent
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "uFriends");
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(
						getApplicationContext(), R.drawable.applogo));

		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// finally broadcast the new Intent
		getApplicationContext().sendBroadcast(addIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
