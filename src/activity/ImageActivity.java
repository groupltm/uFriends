package activity;

import java.io.File;

import instance.MyBundle;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.example.ufriends.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class ImageActivity extends Activity{

	private SliderLayout mDemoSlider;
	private MyBundle mBundle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_image);
		
		WindowManager.LayoutParams windowManager = getWindow().getAttributes();
	    windowManager.dimAmount = 0.75f;
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		mDemoSlider = (SliderLayout)findViewById(R.id.slider);
		mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Foreground2Background);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        
		mBundle = MyBundle.getInstance();
		addImageFileToSlider();
		
		Intent intent = getIntent();
		int position = intent.getIntExtra("position", 0);
		mDemoSlider.setCurrentPosition(position);
	}
	
	private void addImageFileToSlider(){
		for (int i = 0; i < mBundle.receivedImagePath.size(); i++){
			File imageFile = new File(mBundle.receivedImagePath.get(i));
			if (imageFile.exists()){
				TextSliderView textSliderView = new TextSliderView(this);
	            textSliderView.image(imageFile);
	            textSliderView.setScaleType(BaseSliderView.ScaleType.CenterInside);
	            mDemoSlider.addSlider(textSliderView);
	            
	            
			}
		}
	}
}
