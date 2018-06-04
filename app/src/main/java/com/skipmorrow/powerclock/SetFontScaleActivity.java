package com.skipmorrow.powerclock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SetFontScaleActivity extends Activity {

	BroadcastReceiver _broadcastReceiver;
	//private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("h:mm");
	//private final SimpleDateFormat _sdfWatchAmPm = new SimpleDateFormat("aa");
	//private final SimpleDateFormat _sdfNextAlarm = new SimpleDateFormat("E, h:mm a");
	TextView tvTime;
	TextView tvAmPm;
	TextView tvNextAlarm;
	TextView tvSunInfo;
	Button btnAlarmSettings;
	Button btnSkipNextAlarm;
	Button btnSettings;
	//private final String DEFAULT_FONT_BRIGHTNESS = "ff";
	private final String DEFAULT_FONT_COLOR = "ff0000"; //RED
	//private Integer x;
	private int width;
	private int height;
	//private String daynight;
	SharedPreferences prefs;
    int fontSize;

@Override
	public void onStart() {
	    super.onStart();
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_display);
        tvNextAlarm = (TextView)findViewById(R.id.clocktoprow);
        tvAmPm = (TextView) findViewById(R.id.clockdisplayampm);
        tvTime = (TextView) findViewById(R.id.clockdisplaytime);
        Typeface face;
        face = Typeface.createFromAsset(getAssets(), "Digital-7-improved.ttf");
        //tvTime=(TextView)findViewById(R.id.clockdisplaytime);
        tvTime.setTypeface(face);
        tvTime.setText("12:00");
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fontSize = prefs.getInt(SettingsActivity.KEY_CLOCK_FONT_SCALE, 256);
        final String fontColor =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_FONT,
                        DEFAULT_FONT_COLOR);
        //Log.d(this, "Font color: " + fontColor);
        tvTime.setTextSize(fontSize);
        try {
        	tvTime.setTextColor(Color.parseColor("#ff" + fontColor));
        } catch (IllegalArgumentException e) {
        	tvTime.setTextColor(Color.parseColor("#ffff0000"));
        }
        //tvAmPm=(TextView)findViewById(R.id.clockdisplayampm);
        tvAmPm.setTypeface(face);
        try {
        	tvAmPm.setTextColor(Color.parseColor("#ff" + fontColor));
        } catch (IllegalArgumentException e) {
        	tvAmPm.setTextColor(Color.parseColor("#ffff0000"));
        }

/*        tvNextAlarm = (TextView)findViewById(R.id.clocktoprow);
        btnAlarmSettings = (Button) findViewById(R.id.btn_alarm_settings);
        try {
        	btnAlarmSettings.setTextColor(Color.parseColor("#ff" + fontColor));
	    } catch (NumberFormatException e) {
	    	tvTime.setTextColor(Color.parseColor("#ffff0000"));
	    }
        
        btnSkipNextAlarm = (Button) findViewById(R.id.btn_skip_next_alarm);
        try {
        	btnSkipNextAlarm.setTextColor(Color.parseColor("#ff" + fontColor));
		} catch (NumberFormatException e) {
			tvTime.setTextColor(Color.parseColor("#ffff0000"));
		}
        btnSettings= (Button) findViewById(R.id.btn_settings);
        try {
        	btnSettings.setTextColor(Color.parseColor("#ff" + fontColor));
	    } catch (NumberFormatException e) {
	    	tvTime.setTextColor(Color.parseColor("#ffff0000"));
	    }
        
        tvSunInfo.setTextColor(Color.parseColor("#ff" + fontColor));
        tvSunInfo.setText("Sunrise/Sunset info");
        */
        LinearLayout llButtonRow = (LinearLayout)findViewById(R.id.buttonrow);
        
        Display display = getWindowManager().getDefaultDisplay(); 
        width = display.getWidth();  // deprecated
        height = display.getHeight();
        
        tvTime.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					//xpos = String.valueOf(Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255)) + "/" + String.valueOf(width);
					fontSize = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height / (width - 160), height);
					tvTime.setTextSize(fontSize);
					tvNextAlarm.setText("Current font size: " + String.valueOf(fontSize));
					break;
				case MotionEvent.ACTION_UP:
					//Integer percent = Math.min(((int)event.getRawX() + 80), width) * 255 / width;
					fontSize = Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height / (width - 160), height);
					tvTime.setTextSize(fontSize);
					tvNextAlarm.setText("Current font size: " + String.valueOf(fontSize));
					break;
					
				}
				return true;
			}
		});
        
        llButtonRow.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					//xpos = String.valueOf(Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255)) + "/" + String.valueOf(width);
					fontSize = (int) Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height * .5 / (width * .5 - 160), height * .5);
					btnAlarmSettings.setTextSize(fontSize);
					btnSettings.setTextSize(fontSize);
					btnSkipNextAlarm.setTextSize(fontSize);
					tvNextAlarm.setText(String.valueOf(fontSize));
					break;
				case MotionEvent.ACTION_UP:
					//Integer percent = Math.min(((int)event.getRawX() + 80), width) * 255 / width;
					fontSize = (int) Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height * .5/ (width * .5 - 160), height * .5);
					btnAlarmSettings.setTextSize(fontSize);
					btnSettings.setTextSize(fontSize);
					btnSkipNextAlarm.setTextSize(fontSize);
					tvNextAlarm.setText(String.valueOf(fontSize));
					break;
					
				}
				return true;
			}
		}); 
        
        ViewGroup vg = (ViewGroup) findViewById(R.id.clock_layout);
        vg.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					//xpos = String.valueOf(Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * 255 / (width - 160), 255)) + "/" + String.valueOf(width);
					fontSize = (int) Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height * .5 / (width * .5 - 160), height * .5);
					btnAlarmSettings.setTextSize(fontSize);
					btnSettings.setTextSize(fontSize);
					btnSkipNextAlarm.setTextSize(fontSize);
					tvNextAlarm.setText(String.valueOf(fontSize));
					break;
				case MotionEvent.ACTION_UP:
					//Integer percent = Math.min(((int)event.getRawX() + 80), width) * 255 / width;
					fontSize = (int) Math.min(Math.max(Integer.valueOf((int)event.getRawX() - 80), 0)  * height * .5/ (width * .5 - 160), height * .5);
					btnAlarmSettings.setTextSize(fontSize);
					btnSettings.setTextSize(fontSize);
					btnSkipNextAlarm.setTextSize(fontSize);
					tvNextAlarm.setText(String.valueOf(fontSize));
					break;
					
				}
				return true;
			}
		});
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
        final SharedPreferences.Editor ed = prefs.edit();
		ed.putInt(SettingsActivity.KEY_CLOCK_FONT_SCALE, fontSize);
		ed.commit();
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

    @Override
    public void onPause()
    {
        super.onPause();
        ClockDisplayActivity.isRunning = false;
    }
}
