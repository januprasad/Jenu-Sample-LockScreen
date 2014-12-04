package com.jenuine.samplelockscreen.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import eu.janmuller.android.simplecropimage.CropImage;

public class SettingsActivity extends PreferenceActivity {

	/**********************/

	public static final String TAG = "ImageCropper";
	public static final int REQUEST_CODE_GALLERY = 0x1;
	public static final int REQUEST_CODE_CROP_IMAGE = 0x3;

	private File mFileTemp;
	private int WIDTH;
	private int HEIGHT;
	public static String TEMP_FILE_NAME = "wallpaper.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(Window.FEATURE_NO_TITLE|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN);

		/** Setting Preferences resource to the PreferenceActivity */
		addPreferencesFromResource(R.xml.preferences);
		System.out.println("open gallery");
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_FILE_NAME);
		} else {
			mFileTemp = new File(getFilesDir(), TEMP_FILE_NAME);
		}

		Display display_for_width_height = getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display_for_width_height.getSize(size);
		WIDTH = size.x;
		HEIGHT = size.y;

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		Preference button = findPreference("setwallpaper");
		button.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub

				openGallery();
				return false;
			}
		});
		Preference button1 = findPreference("more");
		button1.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub

				showAbout();
				return false;
			}
		});

	}

	protected void showAbout() {
		// TODO Auto-generated method stub
		
		
		
	}

	public void openGallery() {

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
	}

	private void startCropImage() {

		Intent intent = new Intent(this, CropImage.class);
		intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
		intent.putExtra(CropImage.SCALE, true);
		intent.putExtra(CropImage.ASPECT_X, 2);
		intent.putExtra(CropImage.ASPECT_Y, 3);
		System.out.println(WIDTH + " " + HEIGHT);
		intent.putExtra(CropImage.OUTPUT_X, WIDTH*2);
		intent.putExtra(CropImage.OUTPUT_Y, HEIGHT*2);
		startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {

			return;
		}

		Bitmap bitmap;

		switch (requestCode) {

		case REQUEST_CODE_GALLERY:
			System.out.println("reached");
			try {
				System.out.println(mFileTemp.getAbsolutePath());
				InputStream inputStream = getContentResolver().openInputStream(
						data.getData());
				FileOutputStream fileOutputStream = new FileOutputStream(
						mFileTemp);
				copyStream(inputStream, fileOutputStream);
				fileOutputStream.close();
				inputStream.close();

				startCropImage();

			} catch (Exception e) {

				Log.e(TAG, "Error while creating temp file", e);
			}

			break;

		case REQUEST_CODE_CROP_IMAGE:

//			Toast.makeText(getApplicationContext(), "cropping sucess",
//					Toast.LENGTH_SHORT).show();
			String path = data.getStringExtra(CropImage.IMAGE_PATH);
			if (path == null) {

				return;
			}

//			Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT)
//					.show();
			// buttonRecord.setVisibility(View.VISIBLE);
			System.out.println(mFileTemp.getAbsolutePath());
			bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());

			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			startActivity(intent);

			// mFileTemp.getAbsolutePath(), bitmap);

			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
