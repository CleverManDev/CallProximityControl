package com.cmdev.callproximitycontrol;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements Runnable,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener,
		ActivityCompat.OnRequestPermissionsResultCallback{

	public static final boolean DEBUG = true;

	private static final String TAG = "MainActivity";
	private static final String WAKELOCK_TAG = "callproximitycontrol:" + TAG;
	private static final int REQUEST_INITIAL= 3845;
	private static final int REQUEST_PHONE_PERMISSION = REQUEST_INITIAL + 1;
	private static final int REQUEST_APP_INFO_SCREEN =  REQUEST_INITIAL + 2;


	Switch mServiceState, mBtDevices, mHeadsets, mSpeaker;
	PowerManager.WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialUi();
		setListeners();
		debugButtons();

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
	 	mWakeLock = pm != null
				? pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, WAKELOCK_TAG)
				: null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!canReadPhoneState()) {
			AppSettings.setServiceState(this, false);
		} else {
			AppSettings.setDialogCanceledState(this, false);
		}
		updateUi();
	}

	@Override
	public void run() {
		if (!mWakeLock.isHeld())
			mWakeLock.acquire();
		if (DEBUG) Log.d(TAG, "run: ");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_acquire : run();
				break;
			case R.id.button_release : mWakeLock.release();
				break;
			case R.id.button_start : if (!isMyServiceRunning(WakeLockService.class))
				startService(new Intent(this, WakeLockService.class));
				break;
			case R.id.button_stop : stopService(new Intent(this, WakeLockService.class));
				break;
			case R.id.button_check_speaker :
				AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
				if (am != null) {
					Toast.makeText(this, Boolean.toString(am.isSpeakerphoneOn()), Toast.LENGTH_SHORT).show();
					am.isSpeakerphoneOn();
				}
				break;
			case R.id.button_check_service :

				isMyServiceRunning(WakeLockService.class);
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.d(TAG, "onCheckedChanged: ");
		switch (buttonView.getId()) {
			
			case R.id.switch_service :
				if (isChecked & canReadPhoneState())
					AppSettings.setServiceState(this, true);
				else if (isChecked & !canReadPhoneState()) {
					AppSettings.setServiceState(this, false);
					mServiceState.setChecked(false);
					askReadPhoneStatePermission();
				} else
					AppSettings.setServiceState(this, false);
				break;
			case R.id.switch_bt_device : AppSettings.setBtDevicesState(this, isChecked);
				break;
			case R.id.switch_headset : AppSettings.setHeadsetsState(this, isChecked);
				break;
			case R.id.switch_speaker : AppSettings.setSpeakerState(this, isChecked);
				break;
		}
	}


//    @TargetApi(Build.VERSION_CODES.M)
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (DEBUG) {
			Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
			int i = 1;
			for (String str : permissions) Log.d(TAG, "permission_" + i++ + " " + str);
			i = 1;
			for (int res :	grantResults) Log.d(TAG, "results_" + i++ + " " + res);
		}
		switch (requestCode) {
			case REQUEST_PHONE_PERMISSION :
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					AppSettings.setServiceState(this, true);
					AppSettings.setDialogCanceledState(this, false);
				} else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
					Log.d(TAG, "onRequestPermissionsResult: denied");
					AppSettings.setServiceState(this, false);
					AppSettings.setDialogCanceledState(this, true);
					giveExplanation();
				}
				break;
		}
		updateUi();
	}

	private boolean canReadPhoneState() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
				PackageManager.PERMISSION_GRANTED;
	}

	private void askReadPhoneStatePermission() {
		if (!AppSettings.isDialogCanceled(this)) {
			makeSnackbar(R.string.permission_explanation)
					.setAction(R.string.ok, v -> ActivityCompat.requestPermissions(MainActivity.this,
							new String[]{Manifest.permission.READ_PHONE_STATE},
							REQUEST_PHONE_PERMISSION))
					.show();
		} else {
			getAppInfoScreen(getPackageName());
		}
	}

	private void giveExplanation() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.READ_PHONE_STATE)) {
			AppSettings.setDialogCanceledState(this, false);
			makeSnackbar(R.string.permission_not_granted).show();
		} else {
			AppSettings.setDialogCanceledState(this, true);
			getAppInfoScreen(getPackageName());
		}
	}

	private void getAppInfoScreen(String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		makeSnackbar(R.string.permission_dialog_canceled).setAction(R.string.settings, v ->
				startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))).show();
	}

	private Snackbar makeSnackbar(@StringRes int resId) {
		return Snackbar.make(findViewById(R.id.activity_main_root), resId, Snackbar.LENGTH_LONG);
	}

	private void initialUi() {
		mServiceState = findViewById(R.id.switch_service);
		mBtDevices = findViewById(R.id.switch_bt_device);
		mHeadsets = findViewById(R.id.switch_headset);
		mSpeaker = findViewById(R.id.switch_speaker);
	}

	private void setListeners() {
		mServiceState.setOnCheckedChangeListener(this);
		mServiceState.setOnCheckedChangeListener(this);
		mBtDevices.setOnCheckedChangeListener(this);
		mHeadsets.setOnCheckedChangeListener(this);
		mSpeaker.setOnCheckedChangeListener(this);
	}

	private void updateUi() {
		mServiceState.setChecked(AppSettings.getServiceState(this));
		mBtDevices.setChecked(AppSettings.btDevicesState(this));
		mHeadsets.setChecked(AppSettings.headsetsState(this));
		mSpeaker.setChecked(AppSettings.speakerState(this));
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (am != null) {
			for (ActivityManager.RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(service.service.getClassName())) {
					if (DEBUG) Log.d(TAG, "isMyServiceRunning: true");
					return true;
				}
			}
		}
		if (DEBUG) Log.d(TAG, "isMyServiceRunning: false");
		return false;
	}

	/*
	 * DEBUG SECTION
	 */

	private void debugButtons() {
		if (DEBUG) {
			setDebugVisibility(
					findViewById(R.id.button_acquire),
					findViewById(R.id.button_release),
					findViewById(R.id.button_start),
					findViewById(R.id.button_stop),
					findViewById(R.id.button_check_speaker),
					findViewById(R.id.button_check_service)
			);
		}
	}

	private void setDebugVisibility(View... views) {
		for (View view : views) {
			view.setVisibility(View.VISIBLE);
			view.setOnClickListener(this);
		}
	}

	/*
	 * END DEBUG SECTION
	 */
}
