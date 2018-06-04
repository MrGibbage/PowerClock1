package com.skipmorrow.powerclock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity_layout);
        TextView t = (TextView) findViewById(R.id.help_text);
        
        String helpText = "PowerClock ver 1.0\n" +
        "\n" +
        "Email the developer at skip@pelorus.org\n" +
        "\n" +
        "Alarm Settings help\n" +
        "Alarm in silent mode: Attempts to play the alarm sound even if " +
        "the phone is in silent mode. May not work for all phones. " +
        "Email the developer if your phone does not work with this setting.\n" +
        "\n" +
        "Alarm volume: Sets the maximum volume for the alarm\n" +
        "\n" +
        "Use Gentle Wake: The alarm sound will start at 0 and work it's way up to maximum over a period of time.\n" +
        "\n" +
        "Gentle Wake duration: How long (in seconds) will the volume ramp up to maximum.\n" +
        "\n" +
        "Side button behavior: Do you want to use the side button for snooze, disable, or nothing. " +
        "May not work with all devices. Email the developer if yours does not work.\n" +
        "\n" +
        "Snooze duration: How long between alerts when snoozing.\n" +
        "\n" +
        "Set default ringtone: Sets the default ringone for alarms. Individual alarms can also have unique ringtones.\n" +
        "\n" +
        "Display Settings\n" +
        "Select font color: Red, Green, or Blue\n" +
        "\n" +
        "Set font size: Select the font size by swiping left and right. Try to choose the largest font your " +
        "display can take without losing any digits.\n" +
        "\n" +
        "Adjust screen daytime brightness: You can set the day and night preset brightness " +
        "for quick selection from the main screen. " +
        "Swipe left and right to adjust the font brightness " +
        "and up and down to adjust the screen brightness. Behaves a little differently among " +
        "different devices (AMOLED vs. LCD for instance) but should work on all devices.\n" +
        "\n" +
        "Adjust screen nighttime brightness: Same as daytime.\n" +
        "Show next alarm: Display the summary at the top such as \"No alarms set\", \"Next alarm is set for Mon, 6:30 AM\", etc.\n" +
        "\n" +
        "Smarter show next alarm: Only displays the next alarm if it is set to go off within the next 18 hours.\n" +
        "\n" +
        "Show 24 hour clock: shows time like \"16:00\" rather than \"4:00\".\n" +
        "\n" +
        "Show AM/PM Indicator: select to show the AM/PM indicator on the main screen.\n" +
        "\n" +
        "Show reminders when power is not connected: This app is a BATTERY HOG! It is not meant to run on the " +
        "battery. Instead, the phone should be plugged in when using the clock. If this setting is enabled (default), " +
        "there will be a warning every minute if the phone is not plugged in when the app is running.\n" +
        "\n" +
        "Adjust screen brightness from the main screen: If enabled, you will be abled to adjust the screen brightness " +
        "by swiping on the main screen. If you only want to use presets, then disable this option. It also helps to " +
        "disable this option if you have a tendency to miss the icons and accidentally adjust the screen brightness.\n" +
        "\n" +
        "Other Settings\n" +
        "Auto-start when USB is connected: Enable this option if you want PowerClock to auto-start when the USB is plugged in. " +
        "May not work on all devices. Email the developer if this option does not work for you.\n" +
        "\n" +
        "Keep screen on: Since this is a clock application, it seems pointless if the screen keeps going to sleep. But if that " +
        "is what you want, disable this option.\n" +
        "\n" +
        "Alarm Preferences\n" +
        "Time: What time do you want the alarm to go off\n" +
        "\n" +
        "Ringtone: Ringtones may be set individually for each alarm here.\n" +
        "\n" +
        "Vibrate: Do you want the device to vibrate when the alarm goes off? May not work with every device.\n" +
        "\n" +
        "Repeat: Select the days of the week that you want the alarm to go off.\n" +
        "\n" +
        "Skip next repeating alarm: Enable if you have a repeating alarm and you want the next occurance to be skipped. \n" +
        "There is a shortcut to this setting on the main screen. This feature allows you to disable an upcoming alarm. For " +
        "instance, if you wake up a few minuted before your alarm goes off, you can tap the shortcut to disable the upcoming " +
        "alarm and still get up, knowing that the alarm is still scheduled for tomorrow.\n" +
        "\n" +
        "Label: A short name describing the alarm.";
        
        t.setText(helpText);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
    @Override
    public void onPause()
    {
        super.onPause();
        ClockDisplayActivity.isRunning = false;
    }
	
    @Override
    public void onResume()
    {
        super.onResume();
        ClockDisplayActivity.isRunning = true;
    }
	
}
