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

import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Help desktop widgets to unlock keystore (must be invoked in an activity)
 */
public class ToggleVpn extends Activity {
    private static final String TAG = "xink.ToggleVpn";

    private VpnProfileRepository repository;
    private VpnActor vpnActor;
    private KeyStore keyStore;
    private Runnable resumeAction;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyStore = new KeyStore(getApplicationContext());
        vpnActor = new VpnActor(getApplicationContext());

        toggleVpn(getIntent());
    }

    private VpnProfileRepository getRepository() {
        if (repository == null) {
            repository = VpnProfileRepository.getInstance(getApplicationContext());
        }
        return repository;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(final Intent intent) {
        toggleVpn(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume, check and run resume action");
        if (resumeAction != null) {
            Runnable action = resumeAction;
            resumeAction = null;
            runOnUiThread(action);
        }
    }

    private void toggleVpn(final Intent data) {
        VpnState state = getRepository().getActiveVpnState();

        switch (state) {
        case IDLE:
            connect();
            break;
        case CONNECTED:
            disconnect();
            break;
        default:
            Log.i(TAG, "intent not handled, currentState=" + state);
            finish();
            break;
        }
    }

    private void connect() {
        Log.d(TAG, "connect ...");

        VpnProfile p = getRepository().getActiveProfile();
        if (p == null) {
            Utils.showToast(getApplicationContext(), R.string.err_no_active_vpn);
            Log.e(TAG, "connect failed, no active vpn");
            finish();
            return;
        }

        connect(p);
    }

    private void connect(final VpnProfile p) {
        if (unlockKeyStoreIfNeeded(p)) {
            try {
                vpnActor.connect(p);
            } finally {
                finish();
            }
        }
    }

    private boolean unlockKeyStoreIfNeeded(final VpnProfile p) {
        if (!p.needKeyStoreToConnect() || keyStore.isUnlocked())
            return true;

        Log.i(TAG, "keystore is locked, unlock it now and reconnect later.");
        resumeAction = new Runnable() {
            @Override
            public void run() {
                // redo this after unlock activity return
                connect(p);
            }
        };

        keyStore.unlock(this);
        return false;
    }

    private void disconnect() {
        Log.d(TAG, "disconnect ...");
        try {
            vpnActor.disconnect();
        } finally {
            finish();
        }
    }
}
