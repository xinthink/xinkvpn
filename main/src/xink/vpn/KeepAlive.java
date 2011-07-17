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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import xink.vpn.wrapper.VpnState;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * Keep VPN connection alive, by accessing internet resource periodically.
 */
public final class KeepAlive extends BroadcastReceiver {

    private static final String TAG = KeepAlive.class.getName();

    private static final int HEARTBEAT_PERIOD = 300000;

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

        VpnState newState = Utils.extractVpnState(intent);
        stateChanged(profileName, newState);
    }

    private void stateChanged(final String profileName, final VpnState newState) {
        Log.d(TAG, profileName + " state ==> " + newState);

        switch (newState) {
        case CONNECTED:
            startHeartbeat();
            break;
        case IDLE:
            stopHeartbeat();
            break;
        default:
            break;
        }
    }

    private static void startHeartbeat() {
        if (heartbeat != null)
            return;

        heartbeat = new Heartbeat();
        timer.schedule(heartbeat, HEARTBEAT_PERIOD, HEARTBEAT_PERIOD);
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
                "http://www.bing.com", "http://code.google.com/p/xinkvpn/wiki/DonatePlusOne" };

        private static final Random RANDOM = new Random();

        @Override
        public void run() {
            Log.i(TAG, "start heartbeat");

            AndroidHttpClient client = AndroidHttpClient.newInstance("XinkVpn");
            try {
                HttpResponse resp = client.execute(new HttpHead(nextUrl()));
                Log.i(TAG, "heartbeat resp: " + resp.getStatusLine());
            } catch (IOException e) {
                Log.e(TAG, "heartdbeat error", e);
            } finally {
                client.close();
            }
        }

        private static String nextUrl() {
            String url = TARGETS[RANDOM.nextInt(TARGETS.length)];
            Log.d(TAG, "next target: " + url);
            return url;
        }
    }
}
