package xink.vpn;

import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ToggleVpn extends Activity {
    private static final String TAG = "xink.ToggleVpn";

    private VpnProfileRepository repository;
    private KeyStore keyStore;
    private Runnable resumeAction;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyStore = new KeyStore(getApplicationContext());
        toggleVpn(getIntent());
    }

    private VpnProfileRepository getRepository() {
        if (repository == null) {
            repository = VpnProfileRepository.getInstance(getApplicationContext());
        }
        return repository;
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
        VpnState state = (VpnState) data.getSerializableExtra(Constants.KEY_VPN_STATE);

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
        Log.e(TAG, "connect ...");

        VpnProfile p = getRepository().getActiveProfile();
        if (p == null) {
            Toast.makeText(this, getString(R.string.err_no_active_vpn), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "connect failed, no active vpn");
            return;
        }

        connect(p);
    }

    private void connect(final VpnProfile p) {
        if (unlockKeyStoreIfNeeded(p)) {
            sendToggleRequest();
            finish();
        }
    }

    private boolean unlockKeyStoreIfNeeded(final VpnProfile p) {
        if (!p.needKeyStoreToConnect() || keyStore.isUnlocked()) {
            return true;
        }

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
        Log.e(TAG, "disconnect ...");
        sendToggleRequest();
        finish();
    }

    private void sendToggleRequest() {
        Intent intent = new Intent(Constants.ACT_TOGGLE_VPN_CONN);
        sendBroadcast(intent);
    }
}
