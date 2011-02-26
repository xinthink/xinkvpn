package xink.vpn;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import dalvik.system.PathClassLoader;

public class VpnSettingView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i("xink", "xinkvpn package code path = " + getPackageName());
        Log.i("xink", "xinkvpn package code path = " + getPackageCodePath());

        try {
            ApplicationInfo vpnAppInfo = getPackageManager().getApplicationInfo("com.android.settings", 0);
            Log.i("xink", vpnAppInfo.sourceDir);

            PathClassLoader myClassLoader = new dalvik.system.PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
            Class<?> handler = Class.forName("android.net.vpn.VpnManager", true, myClassLoader);
            Log.i("xink", handler.getName());
        } catch (Exception e) {
            Log.e("xink", e.getMessage());
        }
    }
}
