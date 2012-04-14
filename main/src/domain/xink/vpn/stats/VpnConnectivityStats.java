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

import xink.vpn.R;
import xink.vpn.Utils;
import xink.vpn.wrapper.VpnState;
import android.content.Context;

/**
 * Keep vpn connectivity stats result.
 * 
 * @author ywu
 * 
 */
public class VpnConnectivityStats {

    private Context ctx;
    private int connErr;

    public VpnConnectivityStats(final Context context) {
        super();
        this.ctx = context;
    }

    /**
     * Receive vpn connectivity event.
     */
    public void onConnectivityChanged(final String profileName, final VpnState newState, final int result) {
        if (!newState.isStable())
            return;

        updateConnStats(result);
    }

    private void updateConnStats(final int result) {
        boolean alert = false;

        synchronized (this) {
            connErr = (result == VPN_ERROR_NO_ERROR) ? 0 : connErr + 1;

            int threshold = getConnErrThreshold();
            if (connErr >= threshold) {
                alert = true;
                connErr = 0; // reset the counter
            }
        }

        if (alert) {
            reportErr(); // send a error report when conn erros reach threshold
        }
    }

    private int getConnErrThreshold() {
        int threshold = Utils.getPrefInt(R.string.pref_crash_conn_broken_threshold_key,
                R.integer.pref_crash_conn_broken_threshold_default, ctx);
        return threshold;
    }

    // send an error report
    private void reportErr() {
        if (!isErrReportEnabled())
            return;

        ACRA.getErrorReporter().handleException(null, false);
    }

    private boolean isErrReportEnabled() {
        boolean reportErr = Utils.getPrefBool(R.string.pref_crash_report_enabled_key,
                R.bool.pref_crash_report_enabled_default, ctx);
        boolean reportConnErr = Utils.getPrefBool(R.string.pref_crash_conn_broken_key,
                R.bool.pref_crash_conn_broken_default, ctx);
        return reportErr && reportConnErr;
    }

}
