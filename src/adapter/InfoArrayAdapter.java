package adapter;


import instance.MyBundle;

import java.io.IOException;
import java.util.List;

import com.example.ufriends.R;
import com.google.gson.JsonIOException;

import custom_view.MaterialRippleLayout;
import custom_view.RippleView.OnRippleCompleteListener;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class InfoArrayAdapter extends ArrayAdapter<String> {

	Context mContext;
	int mResource;
	List<String> mInfo;
	MyBundle mBundle;

	ImageButton btnModifyName;
	ImageButton btnModifyAge;
	ImageButton btnModifySex;

	EditText inputName;
	EditText inputAge;
	RadioButton inputMale;
	RadioButton inputFemale;

	// OnRippleCompleteListener rippleName;
	// OnRippleCompleteListener rippleAge;
	// OnRippleCompleteListener rippleSex;

	public InfoArrayAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);

		mContext = context;
		mResource = resource;
		mInfo = objects;

		mBundle = MyBundle.getInstance();

		// rippleName = new OnRippleCompleteListener() {
		//
		// @Override
		// public void onComplete(RippleView rippleView) {
		// // TODO Auto-generated method stub
		// showModifyNameDialog();
		// }
		// };
		//
		// rippleAge = new OnRippleCompleteListener() {
		//
		// @Override
		// public void onComplete(RippleView rippleView) {
		// // TODO Auto-generated method stub
		// showModifyAgeDialog();
		// }
		// };
		//
		// rippleSex = new OnRippleCompleteListener() {
		//
		// @Override
		// public void onComplete(RippleView rippleView) {
		// // TODO Auto-generated method stub
		// showModifySexDialog();
		// }
		// };
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View v = convertView;

		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		v = inflater.inflate(mResource, null);

		ImageView icon = (ImageView) v.findViewById(R.id.imvIcon);

		MaterialRippleLayout rippleModify = (MaterialRippleLayout) v
				.findViewById(R.id.rippleModify);

		TextView tv = (TextView) v.findViewById(R.id.tv);

		TextView tvInput = (TextView) v.findViewById(R.id.tvInput);

		tvInput.setText(mInfo.get(position));

		switch (position) {
		case 0:
			icon.setImageResource(R.drawable.ic_name);
			tv.setText("Name");
			btnModifyName = (ImageButton) v.findViewById(R.id.btnModify);
			rippleModify.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showModifyNameDialog();
				}
			});
			// rippleModify.setOnRippleCompleteListener(rippleName);
			break;

		case 1:
			icon.setImageResource(R.drawable.ic_age);
			tv.setText("Age");
			btnModifyAge = (ImageButton) v.findViewById(R.id.btnModify);
			rippleModify.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showModifyAgeDialog();
				}
			});
			// rippleModify.setOnRippleCompleteListener(rippleAge);
			break;

		case 2:
			icon.setImageResource(R.drawable.ic_sex);
			tv.setText("Sex");
			btnModifySex = (ImageButton) v.findViewById(R.id.btnModify);
			rippleModify.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showModifySexDialog();
				}
			});
			// rippleModify.setOnRippleCompleteListener(rippleSex);
			break;
		default:
			break;
		}

		return v;
	}

	private void showModifyNameDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("NAME");
		alertDialog.setMessage("Enter Your Name");

		inputName = new EditText(mContext);
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(25);	
		inputName.setFilters(filterArray);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		inputName.setLayoutParams(lp);
		alertDialog.setView(inputName);
		alertDialog.setIcon(R.drawable.ic_name);

		alertDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mInfo.set(0, inputName.getText().toString());
						notifyDataSetChanged();
						mBundle.mInfo._name = mInfo.get(0);
						try {
							mBundle.setInfoToJSONFile(mContext);
						} catch (JsonIOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		alertDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}

	private void showModifyAgeDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("Age");
		alertDialog.setMessage("Enter Your Age");

		inputAge = new EditText(mContext);
		inputAge.setInputType(InputType.TYPE_CLASS_NUMBER);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		inputAge.setLayoutParams(lp);
		alertDialog.setView(inputAge);
		alertDialog.setIcon(R.drawable.ic_age);

		alertDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							
							
							if (!inputAge.getText().toString().equals("")){
								mInfo.set(1, inputAge.getText().toString());
								mBundle.mInfo._age = Integer.parseInt(mInfo.get(1));
								notifyDataSetChanged();
								mBundle.setInfoToJSONFile(mContext);					
							}			
						} catch (JsonIOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NumberFormatException e){
							
						}
					}
				});

		alertDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}

	private void showModifySexDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("SEX");
		alertDialog.setMessage("Pick Your Sex");

		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		inputMale = new RadioButton(mContext);
		inputFemale = new RadioButton(mContext);
		inputMale.setText("Male");
		inputFemale.setText("Female");
		if (mInfo.get(2).equals("Male")) {
			inputMale.setChecked(true);
			inputFemale.setChecked(false);
		} else {
			inputMale.setChecked(false);
			inputFemale.setChecked(true);
		}

		inputMale.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					inputFemale.setChecked(false);
				}
			}
		});

		inputFemale.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					inputMale.setChecked(false);
				}
			}
		});

		layout.addView(inputMale);
		layout.addView(inputFemale);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(lp);
		alertDialog.setView(layout);
		alertDialog.setIcon(R.drawable.ic_sex);

		alertDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (inputMale.isChecked()) {
							mInfo.set(2, "Male");
							mBundle.mInfo._sex = true;
						} else {
							mInfo.set(2, "Female");
							mBundle.mInfo._sex = false;
						}

						notifyDataSetChanged();
						try {
							mBundle.setInfoToJSONFile(mContext);
						} catch (JsonIOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		alertDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}
}
