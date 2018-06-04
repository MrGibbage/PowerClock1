/*
 * Copyright (C) 2008 The Android Open Source Project
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

/**
 * package-level logging flag
 */

package com.skipmorrow.powerclock;

import android.util.Config;

@SuppressWarnings("deprecation")
class Log {
    public final static String LOGTAG = "PowerClock_";

	static final boolean LOGV = AlarmClock.DEBUG ? Config.LOGD : Config.LOGV;

    static void v(Object o, String logMe) {
        android.util.Log.v(LOGTAG + o.getClass().getSimpleName(), logMe);
    }

    static void e(Object o, String logMe) {
        android.util.Log.e(LOGTAG + o.getClass().getSimpleName(), logMe);
    }

    static void d(Object o, String logMe) {
        android.util.Log.d(LOGTAG + o.getClass().getSimpleName(), logMe);
    }

    static void e(Object o, String logMe, Exception ex) {
        android.util.Log.e(LOGTAG + o.getClass().getSimpleName(), logMe, ex);
    }
    
    public static void v(boolean b, String classname, String logMe) {
        android.util.Log.v(LOGTAG + classname, logMe);
    }

    public static void e(boolean b, String classname, String logMe) {
        android.util.Log.e(LOGTAG + classname, logMe);
    }

    public static void d(boolean b, String classname, String logMe) {
        android.util.Log.d(LOGTAG + classname, logMe);
    }

    public static void e(boolean b, String classname, String logMe, Exception ex) {
        android.util.Log.e(LOGTAG + classname, logMe, ex);
    }
}
