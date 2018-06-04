package com.skipmorrow.powerclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class PowerConnectedReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(this, "Connected?");
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.KEY_AUTO_START_ON_USB, false)
				&& !ClockDisplayActivity.isRunning) {
			Intent i = new Intent(context, ClockDisplayActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(i);
			} catch (Exception e) {
				Log.e(this, "There was an error when plugging in the USB. " + e.getMessage());
			}
		}
	}
}
