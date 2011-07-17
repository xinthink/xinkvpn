/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xink.vpn;

import static xink.vpn.Constants.*;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import xink.vpn.wrapper.VpnState;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Keep VPN connection alive, by accessing internet resource periodically.
 */
public final class KeepAlive extends BroadcastReceiver {

    public static final String PREF_HEARTBEAT_PERIOD = "xink.vpn.pref.keepAlive.period";

    public static final String PREF_ENABLED = "xink.vpn.pref.keepAlive";

    private static final String TAG = KeepAlive.class.getName();

    private static Timer timer = new Timer("xink.vpn.HeartbeatTimer", true);

    private static Heartbeat heartbeat;

    /**
     * Get informed about VPN connectivity, to start/stop the heartbeat session.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (!ACTION_VPN_CONNECTIVITY.equals(action))
            return;

        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        if (!profileName.equals(Utils.getActvieProfileName(context))) {
            Log.d(TAG, "ignores non-active profile event: " + profileName);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        VpnState newState = Utils.extractVpnState(intent);
        stateChanged(profileName, newState, prefs);
    }

    private void stateChanged(final String profileName, final VpnState newState, final SharedPreferences prefs) {
        Log.d(TAG, profileName + " state ==> " + newState);

        switch (newState) {
        case CONNECTED:
            startHeartbeat(prefs);
            break;
        case IDLE:
            stopHeartbeat();
            break;
        default:
            break;
        }
    }

    private static void startHeartbeat(final SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean(PREF_ENABLED, true);
        if (!enabled || heartbeat != null)
            return;

        int period = getPeriodFromPrefs(prefs);
        Log.d(TAG, "start heartbeat every (ms)" + period);

        heartbeat = new Heartbeat();
        timer.schedule(heartbeat, period, period);
    }

    private static int getPeriodFromPrefs(final SharedPreferences prefs) {
        String periodStr = prefs.getString(PREF_HEARTBEAT_PERIOD, Period.TEN_MIN.toString());
        Period p = Period.valueOf(periodStr);
        return p.value;
    }

    private static synchronized void stopHeartbeat() {
        if (heartbeat == null)
            return;

        heartbeat.cancel();
        int removed = timer.purge();
        Log.d(TAG, "removed heartbeat timerTask: " + removed);
    }

    private static class Heartbeat extends TimerTask {

        private static final String[] TARGETS = { "http://www.google.com", "http://www.android.com",
                "http://www.bing.com", "http://code.google.com/p/xinkvpn/wiki/DonatePlusOne", "http://www.yahoo.com" };

        private static int index;

        @Override
        public void run() {
            String url = nextUrl();
            Log.i(TAG, "start heartbeat, target=" + url);

            AndroidHttpClient client = AndroidHttpClient.newInstance("XinkVpn");
            try {
                HttpResponse resp = client.execute(new HttpHead(url));
                Log.i(TAG, "heartbeat resp: " + resp.getStatusLine());
            } catch (IOException e) {
                Log.e(TAG, "heartdbeat error", e);
            } finally {
                client.close();
            }
        }

        private static String nextUrl() {
            return TARGETS[nextIndex()];
        }

        private static synchronized int nextIndex() {
            index = index == TARGETS.length ? 0 : index;
            return index++;
        }
    }

    private static enum Period {
        FIVE_MIN(300000), TEN_MIN(600000), FIFTEEN_MIN(900000), THIRTY_MIN(1800000), TEST_5_SEC(5000);

        private int value; // in miliseconds

        private Period(final int v) {
            value = v;
        }
    }
}
