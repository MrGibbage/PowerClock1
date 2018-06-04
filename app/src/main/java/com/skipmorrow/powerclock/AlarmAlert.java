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

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

/**
 * Full screen alarm alert: pops visible indicator and plays alarm tone. This
 * activity shows the alert as a dialog.
 */
public class AlarmAlert extends AlarmAlertFullScreen {

    // If we try to check the keyguard more than 5 times, just launch the full
    // screen activity.
    private int mKeyguardRetryCount;
    private final int MAX_KEYGUARD_CHECKS = 5;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(this, "handleScreenOff");
            handleScreenOff((KeyguardManager) msg.obj);
        }
    };

    private final BroadcastReceiver mScreenOffReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    KeyguardManager km =
                            (KeyguardManager) context.getSystemService(
                            Context.KEYGUARD_SERVICE);
                    handleScreenOff(km);
                }
            };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Listen for the screen turning off so that when the screen comes back
        // on, the user does not need to unlock the phone to dismiss the alarm.
        registerReceiver(mScreenOffReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
        Log.d(this, "Creating an AlarmAlert instance");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenOffReceiver);
        // Remove any of the keyguard messages just in case
        mHandler.removeMessages(0);
        Log.d(this, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        finish();
        Log.d(this, "onBackPressed");
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	Rect diagonalBounds = new Rect();
    	getWindow().getDecorView().getHitRect(diagonalBounds);
    	if (diagonalBounds.contains((int) ev.getX(), (int) ev.getY())) {
    		return super.dispatchTouchEvent(ev);
    	} else {
    		return false;
    	}
    }

    private boolean checkRetryCount() {
        if (mKeyguardRetryCount++ >= MAX_KEYGUARD_CHECKS) {
            Log.e(this, "Tried to read keyguard status too many times, bailing...");
            return false;
        }
        Log.d(this, "checkRetryCount");
        return true;
    }

    private void handleScreenOff(final KeyguardManager km) {
        Log.d(this, "handleScreenOff");
        if (!km.inKeyguardRestrictedInputMode() && checkRetryCount()) {
            if (checkRetryCount()) {
                Log.d(this, "sendMessageDelayed");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0, km), 500);
            }
        } else {
            // Launch the full screen activity but do not turn the screen on.
            Log.d(this, "launch the full screen activity instead");
            Intent i = new Intent(this, AlarmAlertFullScreen.class);
            i.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
            i.putExtra(SCREEN_OFF, true);
            startActivity(i);
            finish();
        }
    }
}
