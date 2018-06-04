package com.skipmorrow.powerclock;

//import java.text.SimpleDateFormat;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetBrightnessActivity extends Activity {

	BroadcastReceiver _broadcastReceiver;
	//private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("h:mm");
	//private final SimpleDateFormat _sdfWatchAmPm = new SimpleDateFormat("aa");
	//private final SimpleDateFormat _sdfNextAlarm = new SimpleDateFormat("E, h:mm a");
	TextView tvTime;
	TextView tvAmPm;
	TextView tvNextAlarm;
	LinearLayout llButtonRow;
	ViewGroup vg;
	//private final String DEFAULT_FONT_BRIGHTNESS = "ff";
	//private final String DEFAULT_FONT_COLOR = "ff0000"; //RED
	//private Integer x;
	private int width;
	private int height;
	private String daynight;
	SharedPreferences prefs;
    String hexPercent;
    String strFontColor;
	String strFontBrightness;
    String strColorPart;
	Integer fontBrightness;
	Integer displayBrightness; // a number from 0 to 1000. To use, must divide by 1000 to get a number between 0.0 and 1.0
	int contentColor;

@Override
	public void onStart() {
	    super.onStart();
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        Toast.makeText(getApplicationContext(), getString(R.string.swipe_left_right_brightness), Toast.LENGTH_LONG).show();
        Intent dayNightIntent = getIntent();
        if (dayNightIntent!= null) {
        	daynight = dayNightIntent.getStringExtra("daynight");
        	Log.d(this, "daynight is set to " + daynight);
        } else {
        	Log.e(this, "daynight was set to null. Setting to default of 'day'");
            daynight = "day";
        }
        strFontColor = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_FONT, "#ffff0000").substring(3);
        
        if (daynight.equals("day")) {
        	strFontColor = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_FONT_DAY, "#ff") + strFontColor;
        	displayBrightness = PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingsActivity.KEY_DISPLAY_DAY_BRIGHTNESS, 1000);
        } else {
        	strFontColor = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_FONT_NIGHT, "#ff") + strFontColor;
        	displayBrightness = PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingsActivity.KEY_DISPLAY_NIGHT_BRIGHTNESS, 1000);
        }
        
        strFontBrightness = strFontColor.substring(0, 3);
        strColorPart = strFontColor.substring(3);

        Log.d(this, "Font color for " + daynight + " loaded from sharedpreferences. strFontCOlor = " + strFontColor + "; displayBrightness = " + displayBrightness);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_clock_display);

        Typeface face;
        face = Typeface.createFromAsset(getAssets(), "Digital-7-improved.ttf");
        tvTime=(TextView)findViewById(R.id.clockdisplaytime);
        tvTime.setTypeface(face);
        tvTime.setText("12:00");
        final int fontSize = 
                PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SettingsActivity.KEY_CLOCK_FONT_SCALE,
                        256);
        tvTime.setTextSize(fontSize);
        tvAmPm=(TextView)findViewById(R.id.clockdisplayampm);
        tvAmPm.setTypeface(face);
        tvAmPm.setText("PM");
        tvNextAlarm = (TextView)findViewById(R.id.clocktoprow);
        llButtonRow = (LinearLayout) findViewById(R.id.buttonrow);
        
        Display display = getWindowManager().getDefaultDisplay(); 
        width = display.getWidth();  // deprecated
        height = display.getHeight();
        
        vg = (ViewGroup) findViewById(R.id.clock_layout);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        try {
        	fontBrightness = Integer.parseInt(strFontBrightness.substring(1), 16);
        } catch (Exception e) {
        	Log.e(this, "There was an error parsing the fontbrightness: " + strFontBrightness + "; the error was " + e.getMessage());
        	fontBrightness = 255;
        }
        SetScreenLevel(params, fontBrightness);
		//params.screenBrightness = ((float)displayBrightness) / 1000f;
		//Log.d(this, "Screen brightness has been set to " + ((float)displayBrightness) / 1000f);
		//getWindow().setAttributes(params);
        vg.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//String xpos;
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					//xpos = String.valueOf(Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255)) + "/" + String.valueOf(width);
					fontBrightness = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255);
					displayBrightness = Math.min(Math.max(Integer.valueOf(height - (int)event.getRawY() - 40), 0)  * 1000 / (height - 80), 1000);
					//Log.d(this, "ActionDown/Move; x = " + event.getRawX() + "; y = " + event.getRawY());
					//strFontBrightness = "#" + String.format("%02X", fontBrightness); 
			        SetScreenLevel(params, fontBrightness);
					break;
				case MotionEvent.ACTION_UP:
					//Integer percent = Math.min(((int)event.getRawX() + 80), width) * 255 / width;
					fontBrightness = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255);
					strFontBrightness = "#" + String.format("%02X", fontBrightness);
					Log.d(this, "Saving " + daynight + ": strFontBrightness = " + strFontBrightness + "; displayBrightness = " + displayBrightness);
					//SetAllFontsToColor(strFontBrightness + strFontColor.substring(3));
			        final SharedPreferences.Editor ed = prefs.edit();
					if (daynight.equals("day")) {
						ed.putString(SettingsActivity.KEY_FONT_DAY, strFontBrightness); // only storing the brightness part, such as #ff
						ed.putInt(SettingsActivity.KEY_DISPLAY_DAY_BRIGHTNESS, displayBrightness);
					} else {
						ed.putString(SettingsActivity.KEY_FONT_NIGHT, strFontBrightness); // only storing the brightness part, such as #81
						ed.putInt(SettingsActivity.KEY_DISPLAY_NIGHT_BRIGHTNESS, displayBrightness);
					}
					ed.commit();
					break;
				}
				return true;
		}} );
        DrawIcons();
        tvNextAlarm.setText("Slide left, right, up and down. Back button to save");
    }
	
	private float GetAlphaFromFontBrightness(Integer fontBrightness) {
        try {
        	return (float) Integer.parseInt(String.format("%02X", fontBrightness), 16) / 255f;
        } catch (NumberFormatException e) {
        	Log.d(this, "Could not parse " + String.format("%02X", fontBrightness));
        	return (float) Integer.parseInt("ff", 16) / 255f;
        }
	}
	
	private void SetScreenLevel(WindowManager.LayoutParams params, int fontBrightness) {
        // setAlpha deprecated as of API level 16
		float alpha = GetAlphaFromFontBrightness(fontBrightness);
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        vg.startAnimation(alphaUp);

		//SetAllFontsToColor(strFontBrightness + strFontColor.substring(3));
		params.screenBrightness = ((float)displayBrightness) / 1000f;
		getWindow().setAttributes(params);
		
		strFontBrightness = "#" + String.format("%02X", fontBrightness);
		strFontColor = strFontBrightness + strColorPart;
        try {
            contentColor = Color.parseColor(strFontColor);
        } catch (IllegalArgumentException e) {
        	Log.e(this, "Number format exception in parsing the color '" + strFontColor + "' in the SetBrightnessActivity");
        	strFontColor = "#ffff0000";
        	contentColor = Color.parseColor("#ffff0000");
        }

        tvTime.setTextColor(contentColor);
        tvAmPm.setTextColor(contentColor);
        tvNextAlarm.setTextColor(contentColor);
	}

    @Override
    public void onResume()
    {
        super.onResume();
        ClockDisplayActivity.isRunning = true;
    }
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
        //final SharedPreferences.Editor ed = prefs.edit();
		//ed.putString("font_brightness_" + daynight + "_setting", hexPercent);
		//ed.commit();
	}
	
	public void btnAlarmSettings_clicked(View v) {
	}

	public void btnSettings_clicked(View v) {
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
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }
    
	private void DrawIcons() {
		llButtonRow.removeAllViews();

        int iconColor;
        try {
        	iconColor = Color.parseColor("#" + strColorPart);
        } catch (IllegalArgumentException e) {
        	iconColor = Color.parseColor("#ff0000");
        }

        ImageButton ibAlarmSettings = new ImageButton(this);
        ibAlarmSettings.setBackgroundResource(R.drawable.alarm_clock_white_icon);
        ibAlarmSettings.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);

        ImageButton ibSettings = new ImageButton(this);
        ibSettings.setBackgroundResource(R.drawable.wrench_white_icon);
        ibSettings.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibSettings.setAdjustViewBounds(true);
        ibSettings.setMaxHeight(40);
        
        ImageButton ibSkipNext = new ImageButton(this);
        ibSkipNext.setBackgroundResource(R.drawable.skip_next_white_icon);
        ibSkipNext.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibSkipNext.setAdjustViewBounds(true);
        ibSkipNext.setMaxHeight(40);
        
        ImageButton ibDay = new ImageButton(this);
        ibDay.setBackgroundResource(R.drawable.sun_white_icon);
        ibDay.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibDay.setAdjustViewBounds(true);
        ibDay.setMaxHeight(40);

        ImageButton ibNight = new ImageButton(this);
        ibNight.setBackgroundResource(R.drawable.moon_white_icon);
        ibNight.getBackground().setColorFilter(iconColor, Mode.MULTIPLY);
        ibNight.setAdjustViewBounds(true);
        ibNight.setMaxHeight(40);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16, 0, 16, 0);
        
        llButtonRow.addView(ibAlarmSettings, lp);
        llButtonRow.addView(ibSettings, lp);
        llButtonRow.addView(ibSkipNext, lp);
        llButtonRow.addView(ibDay, lp);
        llButtonRow.addView(ibNight, lp);
        ibSettings.getLayoutParams().height = 60;
        ibSettings.getLayoutParams().width = 60;
        ibAlarmSettings.getLayoutParams().width = 60;
        ibDay.getLayoutParams().width = 60;
        ibNight.getLayoutParams().width = 60;
        
        Float alpha;
        try {
        	alpha = (float) Integer.parseInt(strFontBrightness.substring(1), 16) / 255f;
        } catch (NumberFormatException e) {
        	Log.d(this, "Could not parse " + strFontBrightness.substring(1));
        	alpha = (float) Integer.parseInt("ff", 16) / 255f;
        }
        
        // setAlpha deprecated as of API level 16
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        vg.startAnimation(alphaUp);
        Log.d(this, "Alpha = " + alpha);
	}
	
    @Override
    public void onPause()
    {
        super.onPause();
        ClockDisplayActivity.isRunning = false;
    }
}