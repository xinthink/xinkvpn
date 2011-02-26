package xink.vpn;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class VpnSettingView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            VpnManagerWrapper vpnMgrWrapper = new VpnManagerWrapper(this);
            vpnMgrWrapper.connect();
        } catch (Throwable e) {
            Log.e("xink", "vpnMgrWrapper failed", e);
        }
    }
}
