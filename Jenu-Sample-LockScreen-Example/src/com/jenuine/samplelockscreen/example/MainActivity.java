package com.jenuine.samplelockscreen.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jenuine.samplelockscreen.LockScreen;
import com.jenuine.samplelockscreen.LockScreen.OnTriggerListener;

public class MainActivity extends Activity implements OnTriggerListener {

	private LockScreen mLockScreen;

	KeyguardManager.KeyguardLock k1;
	boolean inDragMode;
	int selectedImageViewX;
	int selectedImageViewY;
	int windowwidth;
	int windowheight;
	ImageView droid, phone, home;
	// int phone_x,phone_y;
	int home_x, home_y;
	int[] droidpos;
	private LayoutParams layoutParams;
	private LinearLayout bg;
	private TextView time;
	private Typeface typeface;
	private TextView textViewDate;
	private String wifi;
	protected Handler mClockHandler;
	private View bottomLayout;
	private View buttonWallpaper;

	/**********************/

	public static final String TAG = "ImageCropper";
	public static final int REQUEST_CODE_GALLERY = 0x1;
	public static final int REQUEST_CODE_CROP_IMAGE = 0x3;

	private File mFileTemp;

	private Bitmap bitmap;

	public static String TEMP_FILE_NAME = "wallpaper.jpg";

	/*
	 * @Override public void onAttachedToWindow() { // TODO Auto-generated
	 * method stub
	 * this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG
	 * |WindowManager.LayoutParams.FLAG_FULLSCREEN);
	 * 
	 * super.onAttachedToWindow(); }
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(Window.FEATURE_NO_TITLE|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		mLockScreen = (LockScreen) findViewById(R.id.glow_pad_view);
		bg = (LinearLayout) findViewById(R.id.Layout);
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_FILE_NAME);
		} else {
			mFileTemp = new File(getFilesDir(), TEMP_FILE_NAME);
		}
		if (mFileTemp.exists()) {
			bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
			Drawable d = new BitmapDrawable(getResources(), bitmap);
			bg.setBackground(d);
		}
		time = (TextView) findViewById(R.id.textViewtime);
		textViewDate = (TextView) findViewById(R.id.textViewDate);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh-mm-ss");
		SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
		// String fileName = dateFormat.format(new Date()) + ".jpg"
		System.out.println(dateFormat.format(new Date()));
		System.out.println(displayFormat.format(new Date()));
		System.out.println(timeFormat.format(new Date()));
		typeface = Typeface.createFromAsset(
				getApplicationContext().getAssets(), "Ubuntu-L.ttf");
		time.setTypeface(typeface);
		time.setText(displayFormat.format(new Date()));

		bottomLayout = findViewById(R.id.BottomLayout);

		Display display_for_width_height = getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display_for_width_height.getSize(size);
		int width = size.x;
		int height = size.y;

//		bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(width,
//				height / 2));

		mLockScreen.setOnTriggerListener(this);

		// uncomment this to make sure the glowpad doesn't vibrate on touch
		// mGlowPadView.setVibrateEnabled(false);

		// uncomment this to hide targets
		mLockScreen.setShowTargetsOnIdle(true);
		startService(new Intent(this, MyService.class));
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		int orientation = display.getOrientation();
		// setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		/*
		 * wifi = getCurrentSsid(getApplicationContext()); if (wifi != null) {
		 * textViewDate.setText("Connected to " + wifi);
		 * textViewDate.setTypeface(typeface); }
		 */
		mClockHandler = new Handler();
		new Thread(new Runnable() {
			public void run() {
				setTime();
				mClockHandler.postDelayed(this, 1000);
			}
		}).start();

		String weekDay;
		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
		Calendar calendar = Calendar.getInstance();
		weekDay = dayFormat.format(calendar.getTime());
		textViewDate.setText(weekDay + "," + dateFormat.format(new Date()));
		textViewDate.setTypeface(typeface);

		buttonWallpaper = findViewById(R.id.buttonWallpaper);
		buttonWallpaper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// openGallery();

				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	protected void setTime() {
		// TODO Auto-generated method stub
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh-mm-ss");
		SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
		// String fileName = dateFormat.format(new Date()) + ".jpg"
		// System.out.println(dateFormat.format(new Date()));
		// System.out.println(displayFormat.format(new Date()));
		// System.out.println(timeFormat.format(new Date()));

		time.setTypeface(typeface);
		time.setText(displayFormat.format(new Date()));
	}

	public static String getCurrentSsid(Context context) {
		String ssid = null;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null
					&& !TextUtils.isEmpty(connectionInfo.getSSID())) {
				ssid = connectionInfo.getSSID();
			}
		}
		return ssid;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
			bg.setBackgroundResource(R.drawable.bg_land);

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
			bg.setBackgroundResource(R.drawable.bg);
		}
	}

	@Override
	public void onGrabbed(View v, int handle) {
		// TODO Auto-generated method stub
		// ...
	}

	@Override
	public void onReleased(View v, int handle) {
		mLockScreen.ping();

	}

	@Override
	public void onTrigger(View v, int target) {
		final int resId = mLockScreen.getResourceIdForTarget(target);
		switch (resId) {
		case R.drawable.ic_item_camera:
			Toast.makeText(this, "Camera selected", Toast.LENGTH_SHORT).show();

			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			startActivityForResult(intent, 0);

			break;

		case R.drawable.ic_item_google:
			// Toast.makeText(this, "Google selected",
			// Toast.LENGTH_SHORT).show();
			finish();
			break;
		default:
			// Code should never reach here.
		}

	}

	@Override
	public void onGrabbedStateChange(View v, int handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinishFinalAnimation() {
		// TODO Auto-generated method stub

	}
@Override
public void onBackPressed() {
	// TODO Auto-generated method stub
		//	super.onBackPressed();
}
}
