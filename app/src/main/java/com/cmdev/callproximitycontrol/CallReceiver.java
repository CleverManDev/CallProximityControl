package com.cmdev.callproximitycontrol;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import static com.cmdev.callproximitycontrol.MainActivity.DEBUG;

public class CallReceiver extends BroadcastReceiver {

	private static final String TAG = "CallReceiver";
	private static final String EXTRA_STATE_RINGING = "RINGING";
	private static final String EXTRA_STATE_OFFHOOK = "OFFHOOK";
	private static final String EXTRA_STATE_IDLE = "IDLE";


	TelephonyManager mTelephonyManager;

	PowerManager.WakeLock mProximityWakelock;


	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		switch (action) {
			case TelephonyManager.ACTION_PHONE_STATE_CHANGED :
				if (DEBUG) Log.d(TAG, "onReceive: " + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
				break;
			case WakeLockService.ACTION_SERVICE_STOP :
				if (DEBUG) Log.d(TAG, "onReceive: " + WakeLockService.ACTION_SERVICE_STOP);
				context.stopService(new Intent(context, WakeLockService.class));
				break;
			case WakeLockService.ACTION_SPEAKER_ON :
				if (DEBUG) Log.d(TAG, "onReceive: " + WakeLockService.ACTION_SPEAKER_ON);
				break;
			case WakeLockService.ACTION_SPEAKER_OFF :
				if (DEBUG) Log.d(TAG, "onReceive: " + WakeLockService.ACTION_SPEAKER_OFF);
				break;
			case Intent.ACTION_HEADSET_PLUG :
				if (DEBUG) Log.d(TAG, "onReceive: " + action + "\n");
				break;
		}
	}

	@SuppressLint("WakelockTimeout")
	private void acquireWakelock() {
		if (!mProximityWakelock.isHeld()) {
			mProximityWakelock.acquire();
		}
	}

	private void releaseWakelock() {
		if (mProximityWakelock.isHeld()) {
			mProximityWakelock.release();
		}

	}
}
