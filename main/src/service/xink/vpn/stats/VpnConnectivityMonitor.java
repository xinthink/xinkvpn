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
package xink.vpn.stats;

import static xink.vpn.Constants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xink.vpn.Utils;
import xink.vpn.VpnProfileRepository;
import xink.vpn.wrapper.VpnState;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * VPN connectivity monitor. Collects stats data, sending error report...
 * 
 * @author ywu
 * 
 */
public class VpnConnectivityMonitor extends BroadcastReceiver {

    private static Logger LOG = LoggerFactory.getLogger("xink.ConnMonitor");

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (ACTION_VPN_CONNECTIVITY.equals(intent.getAction())) {
            onConnChanged(intent, context);
        }
    }

    private void onConnChanged(final Intent data, final Context ctx) {
        String profileName = data.getStringExtra(BROADCAST_PROFILE_NAME);
        VpnState newState = Utils.extractVpnState(data);
        int err = data.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);

        LOG.info(String.format("vpn %1$s -> %2$s, err=%3$d", profileName, newState, err));

        VpnConnectivityStats stats = VpnProfileRepository.getInstance(ctx).getConnectivityStats();
        stats.onConnectivityChanged(profileName, newState, err);
    }

}
