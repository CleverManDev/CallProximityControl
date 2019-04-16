package com.cmdev.callproximitycontrol;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import android.util.Log;



import java.util.Timer;
import java.util.TimerTask;

import static com.cmdev.callproximitycontrol.MainActivity.DEBUG;

//http://developer.alexanderklimov.ru/android/java/timer.php

public class WakeLockService extends Service {
	private static final String TAG = WakeLockService.class.getSimpleName();
	private static final String WAKELOCK_TAG = "cmdev:wakelock";
	private static final String CHANNEL_ID = "CallProximityControl";


	private static final String PERM_PRIVATE = "com.cmdev.callproximitycontrol.PRIVATE";

	private static final int NOTIFICATION_ID = 10;


	public static final String ACTION_SERVICE_STOP = "com.cmdev.callproximitycontrol.SERVICE_STOP";
	public static final String ACTION_SPEAKER_ON = "com.cmdev.callproximitycontrol.SPEAKER_ON";
	public static final String ACTION_SPEAKER_OFF = "com.cmdev.callproximitycontrol.SPEAKER_OFF";

	PowerManager.WakeLock mProximityWakeLock;
	NotificationManager mNotificationManager;
	Timer mTimer;
	Boolean mSpeakerOn;

	@Override
	public void onCreate() {
		super.onCreate();
		createNotificationChannel();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DEBUG) Log.d(TAG, "onStartCommand: ");

		startForeground(NOTIFICATION_ID, foregroundNotification());
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		if (pm != null) {
			mProximityWakeLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, WAKELOCK_TAG);
		}

		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (am != null) {
			mSpeakerOn = am.isSpeakerphoneOn();
		}
		if (mTimer != null) {
			if (DEBUG) Log.d(TAG, "onStart: Timer disable");
			mTimer.cancel();
			mTimer = null;
		}
		mTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (DEBUG) Log.d(TAG, "run: ");
				if (am != null && am.isSpeakerphoneOn() == !mSpeakerOn) {
					if (DEBUG) Log.d(TAG, "run: " + am.isSpeakerphoneOn());
					mSpeakerOn = am.isSpeakerphoneOn();
					Intent i = new Intent(WakeLockService.this, CallReceiver.class);
					if (mSpeakerOn) {
						i.setAction(ACTION_SPEAKER_ON);
						sendBroadcast(i, PERM_PRIVATE);
					} else {
						i.setAction(ACTION_SPEAKER_OFF);
						sendBroadcast(i, PERM_PRIVATE);
					}
				}
			}
		};

		mTimer.schedule(timerTask, 0,500);

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mProximityWakeLock.isHeld()) {
			mProximityWakeLock.release();
		}
		if (mTimer != null) {
			if (DEBUG) Log.d(TAG, "onDestroy: Timer disable");
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void createNotificationChannel() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					getString(R.string.default_channel_name),
					NotificationManager.IMPORTANCE_MIN);
			channel.setDescription(getString(R.string.default_channel_description));
			mNotificationManager.createNotificationChannel(channel);
		}
	}




	private Notification foregroundNotification() {
		Notification.Builder builder;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			builder = new Notification.Builder(this, CHANNEL_ID);

		} else {
			builder = new Notification.Builder(this)	.setPriority(Notification.PRIORITY_MIN);
		}

		Intent openActivity = new Intent(this, MainActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Intent closeAction = new Intent(this, CallReceiver.class);
		closeAction.setAction(ACTION_SERVICE_STOP);

		PendingIntent pendingActivity = PendingIntent
				.getActivity(this, 0, openActivity,PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingAction = PendingIntent.getBroadcast(this, 0, closeAction, 0);


		builder.setSmallIcon(R.drawable.ic_outline_mobile_friendly_24px)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_text))
				.setContentIntent(pendingActivity)
				.setVisibility(Notification.VISIBILITY_SECRET)
				.addAction(new Notification.Action.Builder(
						R.drawable.ic_outline_mobile_friendly_24px,
						getString(R.string.notification_action),
						pendingAction).build());

		return builder.build();
	}
}
