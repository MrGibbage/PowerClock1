/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skipmorrow.powerclock;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final int ALARM_STREAM_TYPE_BIT =
            1 << AudioManager.STREAM_ALARM;

    static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";
    static final String KEY_ALARM_SNOOZE =
            "snooze_duration";
    static final String KEY_VOLUME_BEHAVIOR =
            "volume_button_setting";
    static final String KEY_DEFAULT_RINGTONE =
            "default_ringtone";
    static final String KEY_FONT = "font"; //current font in the format #ffff0000
    static final String KEY_FONT_DAY = "font_day"; //preset day font brightness such as #ff
    static final String KEY_FONT_NIGHT = "font_night"; //preset night font brightness, such as #81
    
    // will hold an int, from 0 to 999 to indicate brightness. To use, must divide by 1000 because display brightness goes from 0.0 to 1.0
    static final String KEY_DISPLAY_BRIGHTNESS = "display_brightness"; // current display brightness which may not be the same as the day/night presets
    static final String KEY_DISPLAY_DAY_BRIGHTNESS = "display_day_brightness";
    static final String KEY_DISPLAY_NIGHT_BRIGHTNESS = "display_night_brightness";
    static final String KEY_ENABLE_BRIGHTNESS_QUICK_ADJUSTMENTS = "enable_quick_brightness_adjustments";
    
    static final String KEY_SHOW_NEXT_ALARM = "show_next_alarm";
    static final String KEY_SHOW_24HR_CLOCK = "show_24hr_clock";
    static final String KEY_SHOW_AMPM = "show_ampm";
    static final String KEY_SHOW_NEXT_ALARM_SMART = "show_next_alarm_only_if_less_than_18_hours";
    static final String KEY_CLOCK_FONT_SCALE = "clock_font_scale";
    static final String KEY_AUTO_START_ON_USB = "auto_start_when_usb_plugged_in";
    static final String KEY_POWER_NAG = "nag_when_power_not_connected";
    static final String KEY_USE_GENTLE_WAKE = "use_gentle_wake";
    static final String KEY_GENTLE_WAKE_DURATION = "gentle_wake_duration";
    static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    static final String KEY_ICON_SIZE = "icon_size";
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(this, "SettingsActivity");
        addPreferencesFromResource(R.xml.settings);

        final AlarmPreference ringtone =
                (AlarmPreference) findPreference(KEY_DEFAULT_RINGTONE);
        Uri alert = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_ALARM);
        if (alert != null) {
            ringtone.setAlert(alert);
        }
        ringtone.setChangeDefault();
        
        Preference showNextAlarmPreference = findPreference(KEY_SHOW_NEXT_ALARM);
        showNextAlarmPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Preference smartShowNextAlarmPreference = findPreference(KEY_SHOW_NEXT_ALARM_SMART);
				smartShowNextAlarmPreference.setEnabled((Boolean)newValue);
				return true;
			}
		});
        Preference useGentleWakePreference = findPreference(KEY_USE_GENTLE_WAKE);
        useGentleWakePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Preference gentleWakeDurationPreference = findPreference(KEY_GENTLE_WAKE_DURATION);
				gentleWakeDurationPreference.setEnabled((Boolean)newValue);
				return true;
			}
		});
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        ClockDisplayActivity.isRunning = true;
		Preference smartShowNextAlarmPreference = findPreference(KEY_SHOW_NEXT_ALARM_SMART);
		smartShowNextAlarmPreference.setEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_SHOW_NEXT_ALARM, false));
		Preference useGentleWakeDurationPreference = findPreference(KEY_GENTLE_WAKE_DURATION);
		useGentleWakeDurationPreference.setEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_USE_GENTLE_WAKE, false));
        refresh();
    }

    @SuppressWarnings("deprecation")
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            int ringerModeStreamTypes = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

            if (pref.isChecked()) {
                ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
            } else {
                ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                    ringerModeStreamTypes);

            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
    	//Log.d(this, "Preference changed. " + pref.getKey() + " is now set to " + newValue.toString());
        final ListPreference listPref = (ListPreference) pref;
        final int idx = listPref.findIndexOfValue((String) newValue);
        listPref.setSummary(listPref.getEntries()[idx]);
        return true;
    }
    
    @SuppressWarnings("deprecation")
	private void refresh() {
        final CheckBoxPreference alarmInSilentModePref =
                (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        final int silentModeStreams =
                Settings.System.getInt(getContentResolver(),
                        Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        alarmInSilentModePref.setChecked(
                (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

        final ListPreference snooze =
                (ListPreference) findPreference(KEY_ALARM_SNOOZE);
        snooze.setSummary(snooze.getEntry());
        snooze.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        ClockDisplayActivity.isRunning = false;
    }

}
