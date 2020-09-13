package com.skipmorrow.powerclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClockDisplayActivity extends Activity {

	BroadcastReceiver _broadcastReceiver;
	private final String UPDATE_CLOCK_DISPLAY_RECEIVER="UPDATE_CLOCK_DISPLAY_RECEIVER";

	private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("h:mm", Locale.getDefault());
	private final SimpleDateFormat _sdfWatchTime24 = new SimpleDateFormat("H:mm", Locale.getDefault());
	private final SimpleDateFormat _sdfWatchAmPm = new SimpleDateFormat("aa", Locale.getDefault());
	private final SimpleDateFormat _sdfNextAlarm = new SimpleDateFormat("E, d MMM h:mm a", Locale.getDefault());

	TextView tvTime;
	TextView tvAmPm;
	TextView tvNextAlarm;
	LinearLayout llButtonRow;
	ViewGroup vg;

	public static boolean isRunning;
	boolean skipNextButtonEnabled;
	boolean skipNextAlarm;

	private int height;
	private int width;

	private final String DEFAULT_FONT_COLOR = "#ffff0000"; //RED

	SharedPreferences prefs;
	private SharedPreferences.Editor ed;

	//Integer displayBrightness; // a number from 0 to 1000. To use, must divide by 1000 to get a number between 0.0 and 1.0
	String strFontColor; // such as #ffff0000

	@Override
	public void onStart() {
	    super.onStart();
	}

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ed =  prefs.edit();

		skipNextAlarm = prefs.getBoolean(SettingsActivity.KEY_SKIP_NEXT_ALARM, false);
        setContentView(R.layout.activity_clock_display);
        tvTime = (TextView) findViewById(R.id.clockdisplaytime);
        tvAmPm = (TextView) findViewById(R.id.clockdisplayampm);
        tvNextAlarm = (TextView)findViewById(R.id.clocktoprow);
        llButtonRow = (LinearLayout) findViewById(R.id.buttonrow);


        vg = (ViewGroup) findViewById(R.id.clock_layout);

        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();  // deprecated
        height = display.getHeight();

        //final WindowManager.LayoutParams params = getWindow().getAttributes();
    }

	private void UpdateEntireDisplay() {
		DrawTime();
		DrawNextAlarmStatus();
		DrawIcons();
		SetBrightness();
	}

	private void SetBrightness() {
		Float alpha;
		Log.d(this, "SetBrightness strFontColor = " + strFontColor);
		String strFontBrightness;
		strFontBrightness = strFontColor.substring(0, 3);
		try {
			alpha = (float) Integer.parseInt(strFontBrightness.substring(1), 16) / 255f;
			Log.d(this, "alpha value set to = " + alpha);
		} catch (NumberFormatException e) {
			Log.d(this, "Could not parse " + strFontBrightness.substring(1));
			alpha = (float) Integer.parseInt("ff", 16) / 255f;
		}

		//setAlpha deprecated as of API level 16
		AlphaAnimation alphaUp = new AlphaAnimation(1.0f, alpha);
		alphaUp.setFillAfter(true);
		vg.startAnimation(alphaUp);

		WindowManager.LayoutParams layout = getWindow().getAttributes();
		//layout.screenBrightness = alpha < 0.9f ? 0.0f : 1.0f;
		layout.screenBrightness = 0.0f;
		getWindow().setAttributes(layout);
	}

	private void DrawTime() {
		if (prefs.getBoolean(SettingsActivity.KEY_SHOW_24HR_CLOCK, false)) {
			tvTime.setText(_sdfWatchTime24.format(new Date()));
		} else {
			tvTime.setText(_sdfWatchTime.format(new Date()));
		}
        if (prefs.getBoolean(SettingsActivity.KEY_SHOW_AMPM, true)) {
        	tvAmPm.setText(_sdfWatchAmPm.format(new Date()));
        } else {
        	tvAmPm.setText("");
        }
		if (prefs.getBoolean(SettingsActivity.KEY_POWER_NAG, false)) {
			if (!isConnected(this)) {
				Toast.makeText(this, "Power not connected", Toast.LENGTH_LONG).show();
			}
		}
	}


	public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

	private void DrawNextAlarmStatus() {
		if (prefs.getBoolean(SettingsActivity.KEY_SHOW_NEXT_ALARM, true)) {
	        Alarm nextAlarm = Alarms.getNextAlertingAlarm(this); // could be an alert that will be skipped
	        //Alarm nextUnskippedAlarm = Alarms.calculateNextUnskippedAlert(this);
	        if (nextAlarm == null) {
	        	tvNextAlarm.setText("There are no alarms set");
	        	return;
	        }
			if (prefs.getBoolean(SettingsActivity.KEY_SHOW_NEXT_ALARM_SMART, false)) {
				//Log.d(this, "Using the smart alarm notification");
		        long nowTime = new Date().getTime();
		        long nowTime18 = nowTime + 18 * 60 * 60 * 1000;
		        //Log.d(this, "Next Alarm Time: " + nextAlarm.time + "; Now: " + nowTime + "; nowtime18 = " + nowTime18);
		        if (nextAlarm.getNextAlarmTimeInMillis(this) < nowTime18) {
		        	//Log.d(this, "nextAlarm.time is less than nowtime18. Show the info");
		        	if (!skipNextAlarm) {
			        	tvNextAlarm.setText("Next alarm is set for " + _sdfNextAlarm.format(nextAlarm.getNextAlarmTimeInMillis(this)));
			        } else {
			        	Log.d(this, "Alarm # " + Alarms.getIdOfNextAlertingAlarm(this) + " is the next unskipped alarm. Getting the time/date now");
			        	//Calendar nextAlert =  nextUnskippedAlarm.getNextUnskippedAlert();
			        	Calendar nextAlert = Calendar.getInstance();
			        	nextAlert.setTimeInMillis(Alarms.getNextNextAlertingAlarmTimeInMillis(this));
			        	tvNextAlarm.setText( _sdfNextAlarm.format(nextAlarm.getNextAlarmTimeInMillis(this)) + ": skipped. The next alarm will sound " + _sdfNextAlarm.format(nextAlert.getTime()));
			        }
		        } else {
		        	tvNextAlarm.setText("");
		        }
			} else {
				//Log.d(this, "Not using the smart alarm notification");
		        if (!skipNextAlarm) {
		        	tvNextAlarm.setText("Next alarm is set for " + _sdfNextAlarm.format(nextAlarm.getNextAlarmTimeInMillis(this)));
		        } else {
		        	//Log.d(this, "UpdateTimeText() in ClockDisplayActivity. myAlarm and nextUnskippedAlarm do not have the same time");
					Calendar nextAlert = Calendar.getInstance();
					nextAlert.setTimeInMillis(Alarms.getNextNextAlertingAlarmTimeInMillis(this));
		        	tvNextAlarm.setText( _sdfNextAlarm.format(nextAlarm.getNextAlarmTimeInMillis(this)) + ": skipped. The next alarm will sound " + _sdfNextAlarm.format(nextAlert.getTime()));
		        }
			}
		} else {
        	tvNextAlarm.setText("");
		}
        //tvNextAlarm.setText("lat: " + latitude + "; long: " + longitude + "; offset: " + (tz.getRawOffset() + tz.getDSTSavings()) / 1000 / 60 / 60 + "; sunset: " + src.getSunset());
	}

	private void DrawIcons() {
		Log.d(this, "Drawing icons");
		llButtonRow.removeAllViews();
		String iconScale =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_ICON_SIZE, "Normal");

		String strColorPart = strFontColor.substring(3);
		String strFontBrightness = strFontColor.substring(0, 3);

        int iconColor;
        try {
        	iconColor = Color.parseColor("#" + strColorPart);
			Log.d(this, "Color:" + strColorPart);
        } catch (IllegalArgumentException e) {
        	Log.e(this, "Could not parse the color:" + strColorPart);
        	iconColor = Color.parseColor("#ff0000");
        }

        ImageButton ibAlarmSettings = new ImageButton(this);
        ibAlarmSettings.setBackgroundResource(R.drawable.alarm_clock_white_icon);
        ibAlarmSettings.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibAlarmSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnAlarmSettings_clicked(v);
			}
		});


        ImageButton ibSettings = new ImageButton(this);
        ibSettings.setBackgroundResource(R.drawable.wrench_white_icon);
        ibSettings.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibSettings.setAdjustViewBounds(true);
        ibSettings.setMaxHeight(40);
        ibSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnSettings_clicked(v);
			}
		});

        ImageButton ibSkipNext = new ImageButton(this);
        ibSkipNext.setBackgroundResource(R.drawable.skip_next_white_icon);
        ibSkipNext.setAdjustViewBounds(true);
        ibSkipNext.setMaxHeight(40);
        Alarm nextAlarm = Alarms.getNextAlertingAlarm(this);
		Log.d(this, "Next alerting alarm has been selected.");
        if (nextAlarm != null) {
			Log.d(this, "Label: " + nextAlarm.label);
			Log.d(this, "time: " + String.valueOf(nextAlarm.time));
			Log.d(this, "nextAlarmTime: " + Alarms.formatTime(this, nextAlarm.getNextAlarmTimeInMillis(this)));
			Log.d(this, "id: " + String.valueOf(nextAlarm.id));
		} else {
			Log.d(this, "nextAlarm is null");
		}

        // see if anything changed with the skipping alarms
        if (nextAlarm == null) {
            Log.d(this, "nextAlarm is null");
        	skipNextButtonEnabled = false;
        	skipNextAlarm = false;
        	ibSkipNext.getBackground().setColorFilter(Color.WHITE, Mode.DST);
        } else {
			if (nextAlarm.time > 0) {
				// this is a single shot alarm, which can't be skipped
				Log.d(this, "The next alarm is a single shot alarm, so skipping is disabled");
				ibSkipNext.getBackground().setColorFilter(Color.WHITE, Mode.DST);
				//ibSkipNext.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
				skipNextAlarm = false;
				skipNextButtonEnabled = false;
			} else {
				Log.d(this, "The next alarm is a repeating alarm");
				prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				skipNextAlarm = prefs.getBoolean(SettingsActivity.KEY_SKIP_NEXT_ALARM, false);
				if (skipNextAlarm) {
					Log.d(this, "skipNext is true, so reverse the button colors");
					ibSkipNext.getBackground().setColorFilter(iconColor, Mode.XOR);
					Alarms.disableAlert(this);
					Alarms.enableAlarm(this, nextAlarm.id, true);
				} else {
					Log.d(this, "skipNext is false, and the button is enabled so it can be skipped");
					ibSkipNext.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
				}
				skipNextButtonEnabled = true;
			}
		}
        ibSkipNext.setAdjustViewBounds(true);
        ibSkipNext.setMaxHeight(40);
        final Context ctx = getApplicationContext();
        //Log.d(this, "Creating onclicklistener");
        ibSkipNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.d(this, "SkipNext button was clicked; skipNextButtonEnabled = " + skipNextButtonEnabled);
				if (skipNextButtonEnabled){
					prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					ed =  prefs.edit();
					skipNextAlarm = !skipNextAlarm;
					ed.putBoolean(SettingsActivity.KEY_SKIP_NEXT_ALARM, skipNextAlarm);
					ed.apply();
					DrawNextAlarmStatus();
					DrawIcons();
					if (skipNextAlarm) {
						Log.d(getApplicationInfo(), "skipNextAlarm is true. Disabling the next alert");
						Alarms.disableAlert(getApplicationContext());
					}
					Alarms.enableAlert(getApplicationContext(),
							Alarms.getNextAlertingAlarm(getApplicationContext()),
							Alarms.getNextNextAlertingAlarmTimeInMillis(getApplicationContext()));
				}
			}
		});
        
        ImageButton ibDay = new ImageButton(this);
        ibDay.setBackgroundResource(R.drawable.sun_white_icon);
        ibDay.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibDay.setAdjustViewBounds(true);
        ibDay.setMaxHeight(40);
        ibDay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SetDisplayBrightnessPreset("day");
			}
		});

        ImageButton ibNight = new ImageButton(this);
        ibNight.setBackgroundResource(R.drawable.moon_white_icon);
        ibNight.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibNight.setAdjustViewBounds(true);
        ibNight.setMaxHeight(40);
        ibNight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SetDisplayBrightnessPreset("night");
			}
		});

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16, 0, 16, 0);
        
        llButtonRow.addView(ibAlarmSettings, lp);
        llButtonRow.addView(ibSettings, lp);
        llButtonRow.addView(ibSkipNext, lp);
        llButtonRow.addView(ibDay, lp);
        llButtonRow.addView(ibNight, lp);

        int iconSize = 60;
		if (iconScale.equals("Large")) iconSize = 90; 
		if (iconScale.equals("Very Large")) iconSize = 120; 
		
		ibSettings.getLayoutParams().height = iconSize;
        ibSettings.getLayoutParams().width = iconSize;
        ibAlarmSettings.getLayoutParams().width = iconSize;
        ibDay.getLayoutParams().width = iconSize;
        ibNight.getLayoutParams().width = iconSize;

        //Log.d(this, "Alpha = " + alpha);
	}
	
	private void SetDisplayBrightnessPreset(String dayNight) {
		String strFontBrightness;
		if (dayNight.equals("day")) {
			
	        strFontBrightness =
	                PreferenceManager.getDefaultSharedPreferences(this)
	                .getString(SettingsActivity.KEY_FONT_DAY,
	                        DEFAULT_FONT_COLOR).substring(0, 3); // just the first three characters, such as #ff
	        //displayBrightness =
	        //        PreferenceManager.getDefaultSharedPreferences(this)
	        //        .getInt(SettingsActivity.KEY_DISPLAY_DAY_BRIGHTNESS,
	        //                1000);
		} else {
	        strFontBrightness=
	                PreferenceManager.getDefaultSharedPreferences(this)
	                .getString(SettingsActivity.KEY_FONT_NIGHT,
	                        DEFAULT_FONT_COLOR).substring(0, 3);
	        //displayBrightness =
	        //        PreferenceManager.getDefaultSharedPreferences(this)
	        //        .getInt(SettingsActivity.KEY_DISPLAY_NIGHT_BRIGHTNESS,
	        //                1000);
		}
		//Log.d(this, dayNight + " preset selected Font brightness = " + strFontBrightness + "; font color currently = " + strFontColor);
		strFontColor = strFontBrightness + strFontColor.substring(3);
		//String strFontBrightness = "#" + String.format("%02X", fontBrightness); 
		//Log.d(this, "Font color has been reset to " + strFontColor + "; display brightness = " + displayBrightness);
        final SharedPreferences.Editor ed = prefs.edit();
		ed.putString(SettingsActivity.KEY_FONT, strFontColor);
		//ed.putInt(SettingsActivity.KEY_DISPLAY_BRIGHTNESS, displayBrightness);
		ed.commit();
		Log.d(this, "SetDisplayBrightnessPreset# Calling SetAllFontsToColor(" + strFontColor + ")");
		//SetAllFontsToColor(strFontColor);
		//UpdateEntireDisplay();
		SetBrightness();
        /*
		final WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = ((float)displayBrightness) / 1000f;
		getWindow().setAttributes(params);
		*/
	}
	
	// arg0 fontColor in the form of #ffff0000
	private void SetAllFontsToColor(String fontcolor) {
		int iFontColor;
		try {
			iFontColor = Color.parseColor(fontcolor);
		} catch (IllegalArgumentException e) {
			Log.e(this, "Cannot parse fontcolor: " + fontcolor);
			iFontColor = Color.parseColor(DEFAULT_FONT_COLOR);
		}
		tvAmPm.setTextColor(iFontColor);
		tvNextAlarm.setTextColor(iFontColor);
		tvTime.setTextColor(iFontColor);
		//DrawIcons();
	}


	@Override
	public void onResume() {
		super.onResume();
        isRunning = true;

	    _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
				Log.d(this, "Intent was received in onResume: " + intent.getAction());
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
					Log.d(this, "ACTION_TIME_TICK intent was received");
					//DrawTime();
                	//DrawNextAlarmStatus();
					UpdateEntireDisplay();
                }
                if (intent.getAction().compareTo(UPDATE_CLOCK_DISPLAY_RECEIVER) == 0) {
                	Log.d(this, "UPDATE_CLOCK_DISPLAY_RECEIVER intent was received");
                	//DrawNextAlarmStatus();
                	//DrawIcons();
					UpdateEntireDisplay();
                }
            }
    	};

	    registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	    registerReceiver(_broadcastReceiver, new IntentFilter(UPDATE_CLOCK_DISPLAY_RECEIVER));

	    Typeface face;
        face = Typeface.createFromAsset(getAssets(), "Digital-7-improved.ttf");
        //tvTime=(TextView)findViewById(R.id.clockdisplaytime);
        tvTime.setTypeface(face);
        final int fontSize = 
                PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SettingsActivity.KEY_CLOCK_FONT_SCALE,
                        82);
        strFontColor =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_FONT,
                        DEFAULT_FONT_COLOR);
		Log.d(this, "onResume fontcolor: " + strFontColor);

        //displayBrightness =
        //        PreferenceManager.getDefaultSharedPreferences(this)
        //        .getInt(SettingsActivity.KEY_DISPLAY_BRIGHTNESS,
        //                1000);

        //final WindowManager.LayoutParams params = getWindow().getAttributes();
		//params.screenBrightness = ((float)displayBrightness) / 1000f;
		//getWindow().setAttributes(params);
		
		try {
			Color.parseColor(strFontColor);
		} catch (IllegalArgumentException e) {
			Log.e(this, "onResume could not parse fontcolor " + strFontColor + ". Setting strFontColor to default of #ffff0000");
			strFontColor = DEFAULT_FONT_COLOR;
		}

        vg.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (prefs.getBoolean(SettingsActivity.KEY_ENABLE_BRIGHTNESS_QUICK_ADJUSTMENTS, true)) {
					String strFontBrightness;
					switch(event.getAction()){
					case MotionEvent.ACTION_MOVE:
						int fontBrightness = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255);
						//displayBrightness = Math.min(Math.max(Integer.valueOf(height - (int)event.getRawY() - 40), 0)  * 1000 / (height - 80), 1000);
						strFontBrightness = "#" + String.format("%02X", fontBrightness);
						Log.d(this, "ACTION_DOWN# Calling SetAllFontsToColor(" + strFontBrightness + strFontColor.substring(3) + ")");
						SetAllFontsToColor(strFontBrightness + strFontColor.substring(3));
						//params.screenBrightness = ((float)displayBrightness) / 1000f;
						//getWindow().setAttributes(params);
						break;
					case MotionEvent.ACTION_DOWN:
						break;
					case MotionEvent.ACTION_UP:
						fontBrightness = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255);
						strFontBrightness = "#" + String.format("%02X", fontBrightness);
						Log.d(this, "ACTION_UP# Calling SetAllFontsToColor(" + strFontBrightness + strFontColor.substring(3) + ")");
						SetAllFontsToColor(strFontBrightness + strFontColor.substring(3));
						ed.putString(SettingsActivity.KEY_FONT, strFontBrightness + strFontColor.substring(3));
						//ed.putInt(SettingsActivity.KEY_DISPLAY_BRIGHTNESS, displayBrightness);
						ed.commit();
						break;
					}
				}
				return true;
		}} );
	
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_KEEP_SCREEN_ON, true)) {
        	getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //Log.d(this, "onResume() Font color: " + strFontColor);
        tvTime.setTextSize(fontSize);
        tvAmPm.setTypeface(face);
		Log.d(this, "OnResume# Calling SetAllFontsToColor(" + strFontColor + ")");
		SetAllFontsToColor(strFontColor);
		UpdateEntireDisplay();
	}
	
	public void btnAlarmSettings_clicked(View v) {
		Intent i = new Intent(this, AlarmClock.class);
		startActivity(i);
	}

	public void btnSettings_clicked(View v) {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_clock_display, menu);
        return true;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
		Log.d(this, "Stopped");
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
		Log.d(this, "Paused...");
    	if (_broadcastReceiver!=null) {
    		unregisterReceiver(_broadcastReceiver);
    		_broadcastReceiver = null;
    		isRunning = false;
    	}
    }
    
    @Override
    public void onBackPressed()
    {
    	finish();
    }
}
