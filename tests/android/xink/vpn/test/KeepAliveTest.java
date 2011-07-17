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
package xink.vpn.test;

import static xink.vpn.Constants.*;
import static xink.vpn.KeepAlive.*;

import java.util.ArrayList;
import java.util.List;

import xink.vpn.VpnProfileRepository;
import xink.vpn.test.helper.RepositoryHelper;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

public class KeepAliveTest extends AndroidTestCase {

    private RepositoryHelper helper;
    private VpnProfileRepository repository;
    private String activeVpnName = "pptp";
    private VpnProfile activeProfile;
    private SharedPreferences prefs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = new RepositoryHelper(mContext);
        helper.clearRepository();
        helper.populateRepository();

        repository = VpnProfileRepository.getInstance(mContext);
        activeProfile = repository.getProfileByName(activeVpnName);
        assertNotNull(activeProfile);
        repository.setActiveProfile(activeProfile);
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected void tearDown() throws Exception {
        helper.clearRepository();
        super.tearDown();
    }

    public void testVpnConnectted() throws Exception {
        prefs.edit().putBoolean(PREF_ENABLED, true).putString(PREF_HEARTBEAT_PERIOD, "TEST_5_SEC").commit();

        sendConnBroadcast(VpnState.CONNECTED);
        Thread.sleep(60000); // wait at least 5 heartbeat periods

        List<Thread> threads = findThreads(".*HeartbeatTimer.*");
        Log.i("xink.test", threads.toString());
        assertEquals("has only one timer", 1, threads.size());
    }

    public void testKeepAliveDisabled() throws Exception {
        prefs.edit().putBoolean(PREF_ENABLED, false).putString(PREF_HEARTBEAT_PERIOD, "TEST_5_SEC").commit();

        sendConnBroadcast(VpnState.CONNECTED);
        Thread.sleep(11000);

        List<Thread> threads = findThreads(".*HeartbeatTimer.*");
        Log.i("xink.test", threads.toString());
        assertEquals("has only one timer", 1, threads.size());
    }

    public void testVpnConnBroken() throws Exception {
        sendConnBroadcast(VpnState.IDLE);
        Thread.sleep(1000);

        List<Thread> threads = findThreads(".*HeartbeatTimer.*");
        Log.i("xink.test", threads.toString());
        assertEquals("has only one timer", 1, threads.size());
    }

    private void sendConnBroadcast(final VpnState state) {
        Intent intent = new Intent(ACTION_VPN_CONNECTIVITY);
        intent.putExtra(BROADCAST_PROFILE_NAME, activeVpnName);
        intent.putExtra(BROADCAST_CONNECTION_STATE, state);
        mContext.sendBroadcast(intent);
    }

    private static List<Thread> findThreads(final String pattern) {
        Thread[] allThreads = getAllThreads();
        ArrayList<Thread> result = new ArrayList<Thread>(allThreads.length);

        for (Thread thread : allThreads) {
            if (thread == null) {
                continue;
            }

            String name = thread.getName();
            if (name.matches(pattern)) {
                result.add(thread);
            }
        }

        return result;
    }

    private static Thread[] getAllThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }

        Thread[] threads = new Thread[rootGroup.activeCount()];
        while (rootGroup.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        return threads;
    }
}
