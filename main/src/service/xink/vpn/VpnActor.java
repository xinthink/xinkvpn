/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.vpn;

import static xink.vpn.Constants.*;
import xink.sys.Mtpd;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class VpnActor {
    private static final int ONE_SEC = 1000;
    private static final String TAG = "xink";

    private VpnProfileRepository repository;
    private Context context;

    public VpnActor(final Context ctx) {
        super();
        context = ctx;
    }

    public void connect() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null)
            throw new NoActiveVpnException("connect failed, no active vpn");

        connect(p);
    }

    public void connect(final VpnProfile p) {
        Log.i(TAG, "connect to: " + p);

        p.preConnect();
        final VpnProfile cp = p.clone4Connect(); // connect using a clone, so the secret key can be replace

        Mtpd.startPptp(cp.server, cp.username, cp.password, ((PptpProfile) cp).encrypted);
    }

    public void disconnect() {
        Log.i(TAG, "disconnect active vpn");

        Mtpd.stop();
    }

    public void checkStatus() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null)
            return;

        checkStatus(p);
    }

    private void checkStatus(final VpnProfile p) {
        Log.i(TAG, "check status of vpn: " + p);


    }

    public void checkAllStatus() {
        VpnProfileRepository repo = getRepository();

        synchronized (repo) {
            for (VpnProfile p : repo.getAllVpnProfiles()) {
                checkStatus(p);
            }
        }
    }

    private VpnProfileRepository getRepository() {
        if (repository == null) {
            repository = VpnProfileRepository.i();
        }

        return repository;
    }

    public void broadcastConnectivity(final String profileName, final VpnState s, final int error) {
        Intent intent = new Intent(ACTION_VPN_CONNECTIVITY);
        intent.putExtra(BROADCAST_PROFILE_NAME, profileName);
        intent.putExtra(BROADCAST_CONNECTION_STATE, s);
        if (error != VPN_ERROR_NO_ERROR) {
            intent.putExtra(BROADCAST_ERROR_CODE, error);
        }
        context.sendBroadcast(intent);
    }
}
