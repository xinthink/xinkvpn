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

import org.acra.ACRA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            onConnChanged(intent);
        }
    }

    private void onConnChanged(final Intent data) {
        int err = data.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);

        // handle connection errors
        if (err != VPN_ERROR_NO_ERROR) {
            handleConnErr(err, data);
        }
    }

    // send an error report
    private void handleConnErr(final int err, final Intent data) {
        LOG.info("connectivity error occurs, err={}", err);

        // ErrorReporter.getInstance().handleSilentException(
        // new RuntimeException("connection error occurs, err_code=" + err));
        ACRA.getErrorReporter().handleException(null, false);
    }

}
