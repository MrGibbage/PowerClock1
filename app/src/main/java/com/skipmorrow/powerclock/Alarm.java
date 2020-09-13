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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;


public final class Alarm implements Parcelable {

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<Alarm> CREATOR
            = new Parcelable.Creator<Alarm>() {
                public Alarm createFromParcel(Parcel p) {
                    return new Alarm(p);
                }

                public Alarm[] newArray(int size) {
                    return new Alarm[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeParcelable(alert, flags);
        p.writeInt(silent ? 1 : 0);
        //p.writeLong(nextTime);
        //p.writeInt(skipNextRepeatingAlarm ? 1 : 0);
    }
    //////////////////////////////
    // end Parcelable apis
    //////////////////////////////
    
/*    public Calendar getNextUnskippedAlert() {
    	// get the next actual alert for this alarm. If skipnext is not true, the next alarm is simply the next alarm.
    	// if skipnext is true, then the next alarm is actually the alarm after the next alarm.
    	Calendar c = Calendar.getInstance();
    	int daystogo = 0;
    	// This might be a skipped alarm
    	if (this.skipNextRepeatingAlarm) {
    		daystogo = daysOfWeek.getAlarmAfterNext(c);
    	} else {
        	daystogo = daysOfWeek.getNextAlarm(c);
    	}
    	//Log.d(this, "getNextUnskippedAlert in the Alarm class. daystogo = " + daystogo);
    	c.add(Calendar.DATE, daystogo);
    	c.set(Calendar.HOUR_OF_DAY, hour);
    	c.set(Calendar.MINUTE, minutes);
    	return c;
    }*/

    //////////////////////////////
    // Column definitions
    //////////////////////////////
    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.skipmorrow.powerclock/alarm");

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>Type: INTEGER</P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>Type: INTEGER</P>
         */
        public static final String ALARM_TIME = "alarmtime";

        /**
         * True if alarm is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";

        /**
         * True if alarm should vibrate
         * <P>Type: BOOLEAN</P>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Message to show when alarm triggers
         * Note: not currently used
         * <P>Type: STRING</P>
         */
        public static final String MESSAGE = "message";

        /**
         * Audio alert to play when alarm triggers
         * <P>Type: STRING</P>
         */
        public static final String ALERT = "alert";

        /**
         * Next alarm time in UTC milliseconds from the epoch.
         * Holds when this alarm will sound. For single-shot alarms,
         * this will just be the time the alarm is set for. (The same
         * as alarmtime.)
         * For repeated alarms, it will hold the time when the alarm is
         * scheduled to sound again, regardless of whether the alarm is
         * skipped or not.
         * <P>Type: INTEGER</P>
         */
        public static final String NEXT_TIME = "nexttime";

        /**
         * True if the next repeating alarm will be skipped
         * <P>Type: BOOLEAN</P>
         */
        //public static final String SKIP_NEXT_REPEATING_ALARM = "skipnext";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        static final String[] ALARM_QUERY_COLUMNS = {
            _ID, HOUR, MINUTES, DAYS_OF_WEEK, ALARM_TIME,
            ENABLED, VIBRATE, MESSAGE, ALERT};

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int ALARM_ID_INDEX = 0;
        public static final int ALARM_HOUR_INDEX = 1;
        public static final int ALARM_MINUTES_INDEX = 2;
        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
        public static final int ALARM_TIME_INDEX = 4;
        public static final int ALARM_ENABLED_INDEX = 5;
        public static final int ALARM_VIBRATE_INDEX = 6;
        public static final int ALARM_MESSAGE_INDEX = 7;
        public static final int ALARM_ALERT_INDEX = 8;
        //public static final int ALARM_SKIP_NEXT_INDEX = 9;
        //public static final int NEXT_TIME_INDEX = 9;

    }
    //////////////////////////////
    // End column definitions
    //////////////////////////////

    // Public fields
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes;
    public DaysOfWeek daysOfWeek;
    public long       time;
    public boolean    vibrate;
    public String     label;
    public Uri        alert;
    public boolean    silent;
    SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    //public long       nextTime;
    //public boolean    skipNextRepeatingAlarm;

    public Alarm(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        //nextTime = c.getLong(Columns.NEXT_TIME_INDEX);
        //skipNextRepeatingAlarm = c.getInt(Columns.ALARM_SKIP_NEXT_INDEX) == 1;
        String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
        
        if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
            if (Log.LOGV) {
                //Log.v(this, "Alarm is marked as silent");
            }
            silent = true;
        } else {
            if (alertString != null && alertString.length() != 0) {
                alert = Uri.parse(alertString);
            }

            // If the database alert is null or it failed to parse, use the
            // default alert.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM);
            }
        }
    }

    public Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();
        alert = (Uri) p.readParcelable(null);
        silent = p.readInt() == 1;
        //nextTime = p.readLong();
        //skipNextRepeatingAlarm = p.readInt() == 1;
    }

    public Alarm (Context context, int hour, int minute, DaysOfWeek dow) {
        enabled = true;
        this.hour = hour;
        this.minutes = minute;
        daysOfWeek = dow;
        if (!dow.isRepeatSet()) {
            time = getNextUnskippedAlarmTimeInMillis(context);
            //time = Alarms.calculateAlarm(context, hour, minutes, daysOfWeek).getTimeInMillis();
        }
    }

    public Alarm (Context context, int hour, int minute, DaysOfWeek dow, int id) {
        enabled = true;
        this.hour = hour;
        this.minutes = minute;
        this.daysOfWeek = dow;
        this.id = id;
        if (!dow.isRepeatSet()) {
            time = getNextUnskippedAlarmTimeInMillis(context);
            //time = Alarms.calculateAlarm(context, hour, minutes, daysOfWeek).getTimeInMillis();
        }
    }

    public String getLabelOrDefault(Context context) {
        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
        }
        return label;
    }

    public ArrayList<Long> getAllAlarmTimesInMillis(Context context) {
        //Log.d(this, "getAllAlarmTimesInMillis() called");
        //Log.d(this, "Hour = " + hour + ", minute = " + minutes + "time = " + time);
        ArrayList<Long> alarmTimes = new ArrayList<Long>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM h:mm a", Locale.getDefault());

        // first handle the single-shot alarm
        if (!daysOfWeek.isRepeatSet()) {
            Log.d(this, "Single shot");
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minutes);
            time = c.getTimeInMillis();
            time = time / 60000 * 60000;
            // it is possible that the time computed above is behind the current time. If it is,
            // add 24 hours.
            if (time < System.currentTimeMillis()) {
                Log.d(this, "the time is behind us. Add 24 hours");
                time += 24 * 60 * 60 * 1000;
            }
            //Log.d(this, "Single shot alarm set for " + sdf.format(time));
            alarmTimes.add(time);
            Log.d(this, Alarms.formatTime(context, time));
            return alarmTimes;
        }

        for (int i = 0; i < 7; i++) {
            int dayCheck = (i + c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            if (daysOfWeek.isSet(dayCheck)) {
                Calendar r = Calendar.getInstance();
                r.set(Calendar.YEAR, c.get(Calendar.YEAR));
                r.set(Calendar.MONTH, c.get(Calendar.MONTH));
                r.set(Calendar.DATE, c.get(Calendar.DATE));
                r.set(Calendar.HOUR_OF_DAY, hour);
                r.set(Calendar.MINUTE, minutes);
                r.set(Calendar.SECOND, 0);
                r.set(Calendar.MILLISECOND, 0);


                // we have a match, but is the match for today? If so, the alarm
                // could have been earlier in the day. Or it could still be ahead.
                if (i == 0) {
                    Calendar t = Calendar.getInstance();

                    int minuteOfDay = t.get(Calendar.HOUR_OF_DAY) * 60 + t.get(Calendar.MINUTE);
                    int alarmMinuteOfDay = hour * 60 + minutes;
                    //Log.d(this, "i = 0. alarmMinuteOfDay = " + alarmMinuteOfDay + "; minuteOfDay = " + minuteOfDay);

                    if (alarmMinuteOfDay < minuteOfDay) {
                        r.add(Calendar.DATE, 7);
                    }
                } else {
                    // some other day, but not today
                    //Log.d(this, "Returning the next alarm time as " + r.toString());
                    r.add(Calendar.DATE, i);
                }
                //Log.d(this, "adding " + sdf.format(r.getTimeInMillis()));
                alarmTimes.add(r.getTimeInMillis());
            }
        }
        Collections.sort(alarmTimes);
        return alarmTimes;
    }

    /**
     * returns the time (in millis) of the next scheduled time
     * for this alarm. If it is a single-shot alarm, this will
     * simply be the time it is scheduled. If it is a repeating
     * alarm, then calculate when it is scheduled next.
     * The alarm time returned could be skipped.
     */
    public long getNextAlarmTimeInMillis(Context context) {
        try {
            return getAllAlarmTimesInMillis(context).get(0);
        }
        catch (Exception e) {
            Log.e(this, "Got an error trying to retrieve the nextAlarmTimeInMillis (Alarm class): " + e.getMessage());
            Log.e(this, "Alarm name: " + this.label);
            Log.e(this, "Alarm ID: " + this.id);
            Log.e(this, "Alarm time: " + this.time);
            Log.e(this, "dayOfWeek isRepeatSet?: " + daysOfWeek.isRepeatSet());
        }
        return 0;
    }

    public long getSecondAlarmTimeInMillis(Context context) {
        try {
            ArrayList<Long> al = getAllAlarmTimesInMillis(context);
            if (al != null && al.size() > 0) {
                return getAllAlarmTimesInMillis(context).get(1);
            } else {
                return -1;
            }
        }
        catch (Exception e) {
            Log.e(this, "Got an error trying to retrieve the secondAlarmTimeInMillis (Alarm class): " + e.getMessage());
        }
        return -1;
    }

    /**
     * get the actual time that this alarm will sound, taking the possibility
     * that skip_next may be set.
     * @return
     */
    public long getNextUnskippedAlarmTimeInMillis(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean skipNextAlarm = prefs.getBoolean(SettingsActivity.KEY_SKIP_NEXT_ALARM, false);
        //Log.d(this, "Calling getNextUnskippedAlarmTimeInMillis. skipNextAlarm = " + skipNextAlarm);
        if (skipNextAlarm) {
            return getSecondAlarmTimeInMillis(context);
        } else {
            return getNextAlarmTimeInMillis(context);
        }
    }

    public int getDaysUntilNextAlarm(Context context) {
        Calendar c = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        c.setTimeInMillis(getNextAlarmTimeInMillis(context));
        return (int) (c.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60 * 60 * 24);
    }

    public static int getDaysUntilNextAlarm(int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {
        if (daysOfWeek.isRepeatSet()) {
            // checking i = 0 and i = 7 are almost the same. Indeed, the math is the same as far
            // as the (i+5)%7, but we check 0 first, and only if the alarm is still ahead of us
            // (at a later time, but still today), and if that is the case, we return 0. But
            // if we get all the way back to 7 without any other days being set (an alarm that is
            // set to repeat once a week, and the time is behind us), then we can return 7
            for (int i = 0; i <= 7; i++) {
                int dayCheck = (i + 5) % 7;
                if (daysOfWeek.isSet(dayCheck)) {
                    if (i == 0) {
                        // the match is today when i == 0. BUt it could be earlier in the day
                        // or still ahead of us.
                        Calendar t = Calendar.getInstance();

                        int minuteOfDay = t.get(Calendar.HOUR_OF_DAY) * 60 + t.get(Calendar.MINUTE);
                        int alarmMinuteOfDay = hour * 60 + minute;

                        if (alarmMinuteOfDay > minuteOfDay) {
                            return 0;
                        }
                    } else return i;
                }
            }
        } else {
            Calendar t = Calendar.getInstance();
            int minuteOfDay = t.get(Calendar.HOUR_OF_DAY) * 60 + t.get(Calendar.MINUTE);
            int alarmMinuteOfDay = hour * 60 + minute;
            if (alarmMinuteOfDay < minuteOfDay) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        };

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ?
                        context.getText(R.string.never).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0) ret.append(
                            context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }
        
        public boolean isSet(int day) {
        	boolean b = ((mDays & (1 << day)) > 0);
        	//Log.d(this, "Checking " + DAY_MAP[day] + "; isset = " + b + "; day = " + day);
            return b;
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
        
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }
        
        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
/*
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            //Log.d(this, "getNextAlarm() starting with " + c.get(Calendar.DATE));
            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            //Log.d(this, "today = " + today);

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                	//Log.d(this, day + " is set. Returning " + dayCount);
                    break;
                }
            }
            return dayCount;
        }
*/

/*
		public int getAlarmAfterNext(Calendar c) {
            //Log.d(this, "getAlarmAfterNext() starting with " + c.get(Calendar.DATE));
            if (mDays == 0) {
                return -1;
            }
            //Log.d(this, "getNextAlarm() starting with " + c.get(Calendar.DATE));
            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            //Log.d(this, "today = " + today);

            Calendar t = Calendar.getInstance();
            int nowHour = t.get(Calendar.HOUR_OF_DAY);
            int nowMinute = t.get(Calendar.MINUTE);

            // if alarm is behind current time, advance one day
            boolean advanced = false;
            if (c.get(Calendar.HOUR) < nowHour  ||
            		c.get(Calendar.HOUR) == nowHour && c.get(Calendar.MINUTE) <= nowMinute) {
            	Log.d(true, "Alarm", "Advancing one day because the alarm has already sounded today.");
                today = (today + 1) % 7;
                advanced = true;
            }

            int day = 0;
            int dayCount = 0;
            boolean firstAlarmFound = false;
            for (; dayCount < 14; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                	if (firstAlarmFound) {
                		Log.d(this, day + " is set. Returning " + dayCount);
                		break;
                	} else {
                		firstAlarmFound = true;
                	}
                }
            }
            return dayCount + (advanced ? 1 : 0);
		}
*/

        /**
         * returns number of days from today until next unskipped alarm,
         * which will be not the first valid occurance following the
         * next alarm.
         * @param c must be set to today
         */
/*
        public int getNextUnskippedAlarmx(Calendar c, int hour, int minute) {
        	Log.d(true, "Alarm", "Calculating next unskipped alarm");
        	int origNextAlarmDayCount = getNextAlarm(c);
        	Log.d(true, "Alarm", "Original next alarm day count = " + origNextAlarmDayCount);
        	Calendar nextCal = Calendar.getInstance();
        	nextCal.add(Calendar.DATE, origNextAlarmDayCount);
        	nextCal.set(Calendar.HOUR_OF_DAY, hour);
        	nextCal.set(Calendar.MINUTE, minute);
        	boolean hasPassed = nextCal.before(Calendar.getInstance());
        	if (hasPassed) {
        		Log.d(this, "nextCal has already passed.");
        		nextCal.add(Calendar.DATE, 1);
        	} else {
        		Log.d(this, "nextCal has not passed.");
        	}
        	nextCal.add(Calendar.DATE, 1);
        	int t1 = getNextAlarm(nextCal);
        	int t2 = origNextAlarmDayCount;
        	int t3 = (hasPassed ? 1 : 0);
        	int r = t1 + t2 + t3;
        	Log.d(true, "Alarm", "Returning " + t1 + " + " + t2 + " + " + t3 + " = " + r);
        	return r;
        }
*/
    }
}
