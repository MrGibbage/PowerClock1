/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class Alarms {

    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String ALARM_ALERT_ACTION = "com.skipmorrow.powerclock.ALARM_ALERT";

    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This intent is sent from the notification when the user cancels the
    // snooze alert.
    public static final String CANCEL_SNOOZE = "cancel_snooze";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    // This string is used to identify the alarm id passed to SetAlarm from the
    // list of alarms.
    public static final String ALARM_ID = "alarm_id";

    final static String PREF_SNOOZE_ID = "snooze_id";
    final static String PREF_SNOOZE_TIME = "snooze_time";

    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E k:mm";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";
    
    /**
     * Creates a new Alarm.
     */
    public static Uri addAlarm(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(Alarm.Columns.HOUR, 8);
        return contentResolver.insert(Alarm.Columns.CONTENT_URI, values);
    }

    /**
     * Removes an existing Alarm.  If this alarm is snoozing, disables
     * snooze.  Sets next alert.
     */
    public static void deleteAlarm(
            Context context, int alarmId) {

        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);

        setNextAlert(context);
    }

    /**
     * Queries all alarms
     * @return cursor over all alarms
     */
    public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(
                Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, Alarm.Columns.DEFAULT_SORT_ORDER);
    }

    // Private method to get a more limited set of alarms from the database.
    private static Cursor getFilteredAlarmsCursor(
            ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
                null, null);
    }

    /**
     * Return an Alarm object representing the alarm id in the database.
     * Returns null if no alarm exists.
     */
    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
                Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }
    
    public static long setAlarm(Context context, Alarm a) {
    	return setAlarm(context, a.id, a.enabled, a.hour, a.minutes, a.daysOfWeek, a.vibrate, a.getLabelOrDefault(context), a.alert.toString(), a.skipNextRepeatingAlarm);
    }


    /**
     * A convenience method to set an alarm in the Alarms
     * content provider.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     * @param hour           corresponds to the HOUR column
     * @param minutes        corresponds to the MINUTES column
     * @param daysOfWeek     corresponds to the DAYS_OF_WEEK column
     * @param time           corresponds to the ALARM_TIME column
     * @param vibrate        corresponds to the VIBRATE column
     * @param message        corresponds to the MESSAGE column
     * @param alert          corresponds to the ALERT column
     * @param skipnext       corresponds to the SKIPNEXT column
     * @return Time when the alarm will fire.
     */
    public static long setAlarm(
            Context context, int id, boolean enabled, int hour, int minutes,
            Alarm.DaysOfWeek daysOfWeek, boolean vibrate, String message,
            String alert, boolean skipnext) {

        ContentValues values = new ContentValues(9);
        ContentResolver resolver = context.getContentResolver();
        // Set the alarm_time value if this alarm does not repeat. This will be
        // used later to disable expired alarms.
        long time = 0;
        if (!daysOfWeek.isRepeatSet()) {
            time = calculateAlarm(context, hour, minutes, daysOfWeek).getTimeInMillis();
        }

        //Log.d(true, "Alarms", "Alarms", "**  setAlarm * idx " + id + " hour " + hour + " minutes " +
        //        minutes + " enabled " + enabled + " time " + time + " skipnext " + skipnext);

        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);
        values.put(Alarm.Columns.HOUR, hour);
        values.put(Alarm.Columns.MINUTES, minutes);
        values.put(Alarm.Columns.ALARM_TIME, time);
        values.put(Alarm.Columns.DAYS_OF_WEEK, daysOfWeek.getCoded());
        values.put(Alarm.Columns.VIBRATE, vibrate);
        values.put(Alarm.Columns.MESSAGE, message);
        values.put(Alarm.Columns.ALERT, alert);
        values.put(Alarm.Columns.SKIP_NEXT_REPEATING_ALARM, skipnext ? 1 : 0);
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
                        values, null, null);

        long timeInMillis =
                calculateAlarm(context, hour, minutes, daysOfWeek, skipnext).getTimeInMillis();

        if (enabled) {
            // If this alarm fires before the next snooze, clear the snooze to
            // enable this alarm.
            SharedPreferences prefs = context.getSharedPreferences(
                    AlarmClock.PREFERENCES, 0);
            long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
            if (timeInMillis < snoozeTime) {
                clearSnoozePreference(context, prefs);
            }
        }

        setNextAlert(context);

        return timeInMillis;
    }

    /**
     * A convenience method to enable or disable an alarm.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     */

    public static void enableAlarm(
            final Context context, final int id, boolean enabled) {
        enableAlarmInternal(context, id, enabled);
        setNextAlert(context);
    }

    private static void enableAlarmInternal(final Context context,
            final int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id), enabled);
    }

    private static void enableAlarmInternal(final Context context,
            final Alarm alarm, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

        // If we are enabling the alarm, calculate alarm time since the time
        // value in Alarm may be old.
        if (enabled) {
            long time = 0;
            if (!alarm.daysOfWeek.isRepeatSet()) {
                time = calculateAlarm(context, alarm.hour, alarm.minutes,
                        alarm.daysOfWeek).getTimeInMillis();
            }
            values.put(Alarm.Columns.ALARM_TIME, time);
        }

        resolver.update(ContentUris.withAppendedId(
                Alarm.Columns.CONTENT_URI, alarm.id), values, null, null);
    }

    public static Alarm calculateNextAlert(final Context context) {
        Alarm alarm = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
                    // A time of 0 indicates this is a repeating alarm, so
                    // calculate the time to get the next alert.
                    if (a.time == 0) {
                        a.time = calculateAlarm(context, a.hour, a.minutes, a.daysOfWeek)
                                .getTimeInMillis();
                    } else if (a.time < now) {
                        // Expired alarm, disable it and move along.
                        enableAlarmInternal(context, a, false);
                        continue;
                    }
                    if (a.time < minTime) {
                        minTime = a.time;
                        alarm = a;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return alarm;
    }
    
    public static boolean alarmsHaveSameIdAndNextAlertDate(Alarm a1, Alarm a2) {
    	if (a1 != null && a2!=null) {
    		return (a1.id == a2.id && a1.time == a2.time && a1.getNextUnskippedAlert() == a2.getNextUnskippedAlert());
    	} else return false;
    }
    
    public static void setAllAlarmsSkippedNextToFalse(final Context context) {
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
                    a.skipNextRepeatingAlarm = false;
                    Alarms.setAlarm(context, a);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public static Alarm calculateNextUnskippedAlert(final Context context) {
        Alarm alarm = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        long curNextAlarmTime = Long.MAX_VALUE;
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        SimpleDateFormat _sdfNextAlarm = new SimpleDateFormat("E, d MMM h:mm a", Locale.getDefault());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
                    Calendar t = Calendar.getInstance();
                    // A time of 0 indicates this is a repeating alarm, so
                    // calculate the time to get the next alert.
                    Log.d(true, "Alarms", "Checking alarm #" + a.id +"; time = " + a.hour + ":" + a.minutes + "; a.time = " + a.time);
                    if (a.time == 0 && a.skipNextRepeatingAlarm == false) {
                        Log.d(true, "Alarms", "Test 1 is true");
                        curNextAlarmTime = calculateAlarm(context, a.hour, a.minutes, a.daysOfWeek).getTimeInMillis();
                        int d = (int) ((curNextAlarmTime - t.getTimeInMillis()) / 1000 / 60);
                        t.add(Calendar.MINUTE, d);
                        Log.d(true, "Alarms", "curNextAlarmTime = " + curNextAlarmTime + "; This alarm is set to go off in " + d + " minutes at " + _sdfNextAlarm.format(t.getTime()));
                    } else if (a.time == 0 && a.skipNextRepeatingAlarm == true) {
                        Log.d(true, "Alarms", "Test 2 is true");
                        curNextAlarmTime = calculateAlarm(context, a.hour, a.minutes, a.daysOfWeek, true).getTimeInMillis();
                        int d = (int) ((curNextAlarmTime - t.getTimeInMillis()) / 1000 / 60);
                        t.add(Calendar.MINUTE, d);
                        Log.d(true, "Alarms", "curNextAlarmTime = " + curNextAlarmTime + "; This alarm is set to go off in " + d + " minutes at " + _sdfNextAlarm.format(t.getTime()));
                    } else if (a.time < now) {
                        Log.d(true, "Alarms", "Test 3 is true");
                        // Expired alarm, disable it and move along.
                        enableAlarmInternal(context, a, false);
                        continue;
                    }
                    if (curNextAlarmTime < minTime) {
                    	Log.d(true, "Alarms", "Alarm #" + a.id + " is now the next unskipped alarm");
                    	Log.d(true, "Alarms", "Is this a skipped alarm? " + a.skipNextRepeatingAlarm);
                    	Calendar c = Calendar.getInstance();
                    	Date d = new Date(curNextAlarmTime);
                    	c.setTime(d);
                    	DateFormatSymbols symbols = new DateFormatSymbols(new Locale("us"));
                    	// for the current Locale :
                    	//   DateFormatSymbols symbols = new DateFormatSymbols(); 
                    	String[] dayNames = symbols.getShortWeekdays();
                    	Log.d(true, "Alarms", "This alarm is scheduled to go off at " + dayNames[c.get(Calendar.DAY_OF_WEEK)] + ", " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                        minTime = curNextAlarmTime; 
                        alarm = a;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Log.d(true, "Alarms", "Returning the next unskipped alarm id is " + alarm.id);
        return alarm;
    }

    /**
     * Disables non-repeating alarms that have passed.  Called at
     * boot.
     */
    public static void disableExpiredAlarms(final Context context) {
        Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        if (cur.moveToFirst()) {
            do {
                Alarm alarm = new Alarm(cur);
                // A time of 0 means this alarm repeats. If the time is
                // non-zero, check if the time is before now.
                if (alarm.time != 0 && alarm.time < now) {
                    if (Log.LOGV) {
                        Log.v(true, "Alarms", "** DISABLE " + alarm.id + " now " + now +" set "
                                + alarm.time);
                    }
                    enableAlarmInternal(context, alarm, false);
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }

    /**
     * Called at system startup, on time/timezone change, and whenever
     * the user changes alarm settings.  Activates snooze if set,
     * otherwise loads all alarms, activates next alert.
     */
    public static void setNextAlert(final Context context) {
        if (!enableSnoozeAlert(context)) {
            Alarm alarm = calculateNextAlert(context);
            if (alarm != null) {
                enableAlert(context, alarm, alarm.time);
            } else {
                disableAlert(context);
            }
        }
    }


    /**
     * Sets alert in AlarmManger and StatusBar.  This is what will
     * actually launch the alert when the alarm triggers.
     *
     * @param alarm Alarm.
     * @param atTimeInMillis milliseconds since epoch
     */
    private static void enableAlert(Context context, final Alarm alarm,
            final long atTimeInMillis) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (Log.LOGV) {
            Log.v(true, "Alarms", "** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

        setStatusBarIcon(context, true);

        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(atTimeInMillis));
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
        // added the next line because of a compiler warning that recycle() should be called.
        out.recycle();
    }

    /**
     * Disables alert in AlarmManger and StatusBar.
     *
     * @param id Alarm ID.
     */
    static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(ALARM_ALERT_ACTION),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
        setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }

    static void saveSnoozeAlert(final Context context, final int id,
            final long time) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);
        if (id == -1) {
            clearSnoozePreference(context, prefs);
        } else {
            SharedPreferences.Editor ed = prefs.edit();
            ed.putInt(PREF_SNOOZE_ID, id);
            ed.putLong(PREF_SNOOZE_TIME, time);
            ed.commit();
        }
        // Set the next alert after updating the snooze.
        setNextAlert(context);
    }

    /**
     * Disable the snooze alert if the given id matches the snooze id.
     */
    static void disableSnoozeAlert(final Context context, final int id) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);
        int snoozeId = prefs.getInt(PREF_SNOOZE_ID, -1);
        if (snoozeId == -1) {
            // No snooze set, do nothing.
            return;
        } else if (snoozeId == id) {
            // This is the same id so clear the shared prefs.
            clearSnoozePreference(context, prefs);
        }
    }

    // Helper to remove the snooze preference. Do not use clear because that
    // will erase the clock preferences. Also clear the snooze notification in
    // the window shade.
    private static void clearSnoozePreference(final Context context,
            final SharedPreferences prefs) {
        final int alarmId = prefs.getInt(PREF_SNOOZE_ID, -1);
        if (alarmId != -1) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(alarmId);
        }

        final SharedPreferences.Editor ed = prefs.edit();
        ed.remove(PREF_SNOOZE_ID);
        ed.remove(PREF_SNOOZE_TIME);
        ed.commit();
    };

    /**
     * If there is a snooze set, enable it in AlarmManager
     * @return true if snooze is set
     */
    private static boolean enableSnoozeAlert(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                AlarmClock.PREFERENCES, 0);

        int id = prefs.getInt(PREF_SNOOZE_ID, -1);
        if (id == -1) {
            return false;
        }
        long time = prefs.getLong(PREF_SNOOZE_TIME, -1);

        // Get the alarm from the db.
        final Alarm alarm = getAlarm(context.getContentResolver(), id);
        // The time in the database is either 0 (repeating) or a specific time
        // for a non-repeating alarm. Update this value so the AlarmReceiver
        // has the right time to compare.
        alarm.time = time;

        enableAlert(context, alarm, time);
        return true;
    }

    /**
     * Tells the StatusBar whether the alarm is enabled or disabled
     */
    private static void setStatusBarIcon(Context context, boolean enabled) {
        //Intent alarmChanged = new Intent(Intent.ACTION_ALARM_CHANGED);
    	Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet", enabled);
        context.sendBroadcast(alarmChanged);
    }

    static Calendar calculateAlarm(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {
    	return calculateAlarm(context, hour, minute, daysOfWeek, false);
    }
    /**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     * @param hour Always in 24 hour 0-23
     * @param minute 0-59
     * @param daysOfWeek 0-59
     */
    static Calendar calculateAlarm(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek, boolean skippedNext) {

        //Log.d(true, "Alarms", "Calculating alarm; hour = " + hour + ", minute = " + minute + ", days = " + daysOfWeek.toString(context, true));
    	// start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
        	//Log.d(true, "Alarms", "Advancing one day because the alarm has already sounded today.");
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int addDays = daysOfWeek.getNextAlarm(c);
        if (skippedNext) {
        	//Log.d(true, "Alarms", "skippedNext is set. The next alarm (which will be skipped) is set for " + addDays + " day(s) from now");
        	addDays = daysOfWeek.getAlarmAfterNext(c);
        	//Log.d(true, "Alarms", "The alarm after next is set for " + addDays + " day(s) from now.");
        }
        	
        /* Log.v("** TIMES * " + c.getTimeInMillis() + " hour " + hour +
           " minute " + minute + " dow " + c.get(Calendar.DAY_OF_WEEK) + " from now " +
           addDays); */
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        //Log.d(true, "Alarms", "Returning " + addDays);
        return c;
    }

    static String formatTime(final Context context, int hour, int minute,
                             Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(context, hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * Shows day and time -- used for lock screen
     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * Save time of the next alarm, as a formatted string, into the system
     * settings so those who care can make use of it.
     */
    static void saveNextAlarm(final Context context, String timeString) {
        Settings.System.putString(context.getContentResolver(),
                                  Settings.System.NEXT_ALARM_FORMATTED,
                                  timeString);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
}
