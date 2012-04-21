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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        VpnProfileRepository repo = VpnProfileRepository.getInstance(context);
        VpnProfile p = repo.getActiveProfile();

        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        if (p == null || profileName == null || !profileName.equals(p.getName())) {
            //Log.d(TAG, "ignores non-active profile event: " + profileName);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        VpnState newState = Utils.extractVpnState(intent);
        stateChanged(p, newState, prefs);
    }

    private void stateChanged(final VpnProfile p, final VpnState newState, final SharedPreferences prefs) {
        //Log.d(TAG, p + " state ==> " + newState);

        switch (newState) {
        case CONNECTED:
            startHeartbeat(p, prefs);
            break;
        case IDLE:
            stopHeartbeat();
            break;
        default:
            break;
        }
    }

    private static void startHeartbeat(final VpnProfile p, final SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean(PREF_ENABLED, false);
        if (!enabled || heartbeat != null)
            return;

        int period = getPeriodFromPrefs(prefs);
        Log.d(TAG, "start heartbeat every (ms)" + period);

        heartbeat = new Heartbeat(p);
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
        heartbeat = null;
        Log.d(TAG, "removed heartbeat timerTask: " + removed);
    }

    private static class Heartbeat extends TimerTask {

        private VpnProfile profile; // current hearbeat vpn profile

        protected Heartbeat(final VpnProfile p) {
            this.profile = p;
        }

        @Override
        public void run() {
            try {
                execPing();
            } catch (IOException e) {
                Log.e(TAG, "heartdbeat error", e);
            }
        }

        // ping the vpn server to keep connection alive
        private void execPing() throws IOException {
            Process process = null;

            try {
                process = new ProcessBuilder("sh").redirectErrorStream(true).start();

                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("ping -c 10 " + profile.getServerName() + "\n");
                os.writeBytes("exit\n");
                os.flush();

                dumpPingResults(process);

            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }

        private void dumpPingResults(final Process process) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);
            }
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
