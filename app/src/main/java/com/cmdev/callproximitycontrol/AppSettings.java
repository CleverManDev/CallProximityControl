package com.cmdev.callproximitycontrol;

import android.content.Context;
import android.preference.PreferenceManager;


public class AppSettings {

	private static final String PREF_DIALOG_CANCELED = "dialogCanceled";
	private static final String PREF_SERVICE_STATE = "serviceState";
	private static final String PREF_BT_DEVICES = "btDevices";
	private static final String PREF_HEADSETS = "headsets";
	private static final String PREF_SPEAKER = "speaker";

	public static boolean isDialogCanceled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_DIALOG_CANCELED, false);
	}

	public static void setDialogCanceledState(Context context, boolean state) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(PREF_DIALOG_CANCELED, state).apply();
	}


	public static boolean getServiceState(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_SERVICE_STATE, false);
	}

	public static void setServiceState(Context context, boolean state) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(PREF_SERVICE_STATE, state)
				.apply();
	}

	public static boolean btDevicesState(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_BT_DEVICES, false);
	}

	public static void setBtDevicesState(Context context, boolean state) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(PREF_BT_DEVICES, state)
				.apply();
	}

	public static boolean headsetsState(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_HEADSETS, false);
	}

	public static void setHeadsetsState(Context context, boolean state) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(PREF_HEADSETS, state)
				.apply();
	}

	public static boolean speakerState(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_SPEAKER, false);
	}

	public static void setSpeakerState(Context context, boolean state) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(PREF_SPEAKER, state)
				.apply();
	}


}
