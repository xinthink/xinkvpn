package xink.vpn;

import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;
import dalvik.system.PathClassLoader;

public class VpnManagerWrapper {

    private static final String ACTION_VPN_SERVICE = "android.net.vpn.SERVICE";
    private Context context;
    private PathClassLoader stubClassLoader;
    private Object vpnMgrStub;
    private Class<?> vpnServiceStubClass;
    private Class<?> vpnProfileType;
    private Object vpnProfileStub;

    public VpnManagerWrapper(final Context ctx) {
        this.context = ctx;
        init();
    }

    private void init() {
        try {
            initStubClassLoader();
            initVpnMgrStub();
            initVpnServiceStub();
            initVpnProfile();
        } catch (Throwable e) {
            throw new VpnManagerWrapperException("init VpnManager object failed", e);
        }
    }

    private void initStubClassLoader() throws Exception {
        ApplicationInfo vpnAppInfo = context.getPackageManager().getApplicationInfo("com.android.settings", 0);
        stubClassLoader = new dalvik.system.PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
    }

    private void initVpnMgrStub() throws Exception {
        Class<?> vpnMgrClass = Class.forName("android.net.vpn.VpnManager", true, stubClassLoader);
        Log.d("xink", "VpnManager class loaded: " + vpnMgrClass.getName());

        vpnMgrStub = vpnMgrClass.getConstructor(Context.class).newInstance(context);
    }

    private void initVpnServiceStub() throws Exception {
        vpnServiceStubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);
        Log.d("xink", "IVpnService class loaded: " + vpnServiceStubClass.getName());
    }

    private void initVpnProfile() throws Exception {
        vpnProfileType = Class.forName("android.net.vpn.VpnProfile", true, stubClassLoader);

        Class<?> profileClass = Class.forName("android.net.vpn.PptpProfile", true, stubClassLoader);
        Log.d("xink", "VpnProfile class loaded: " + profileClass.getName());

        vpnProfileStub = profileClass.newInstance();
        invokeStubMethod(vpnProfileStub, "setName", "blockcn");
        invokeStubMethod(vpnProfileStub, "setId", "blockcn");
        invokeStubMethod(vpnProfileStub, "setServerName", "v6.blockcn.com");
        invokeStubMethod(vpnProfileStub, "setDomainSuffices", "8.8.8.8");
        // invokeStubMethod(vpnProfileStub, "setRouteList", "");
    }

    public void connect() {
        startVpnService();

        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service) {
                try {
                    boolean success = doConnect(service, vpnProfileStub, "xinthink", "9jh74Nx$");

                    if (!success) {
                        Log.d("xink", "~~~~~~ connect() failed!");
                    } else {
                        Log.d("xink", "~~~~~~ connect() succeeded!");
                    }
                } catch (Throwable e) {
                    Log.e("xink", "connect()", e);
                    // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                Log.e("xink", "onServiceDisconnected");
                // checkStatus();
            }
        };

        if (!bindVpnService(c)) {
            Log.e("xink", "bind service failed");
            // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
        }
    }

    private boolean doConnect(final IBinder service, final Object profile, final String username, final String password) throws Exception {
        Method m = vpnServiceStubClass.getMethod("asInterface", IBinder.class);
        Object stub = m.invoke(null, service);

        m = stub.getClass().getMethod("connect", vpnProfileType, String.class, String.class);
        return (Boolean) m.invoke(stub, profile, username, password);
    }

    private void startVpnService() {
        invokeStubMethod(vpnMgrStub, "startVpnService");
    }

    private boolean bindVpnService(final ServiceConnection c) {
        if (!context.bindService(new Intent(ACTION_VPN_SERVICE), c, 0)) {
            Log.w("xink", "failed to connect to VPN service");
            return false;
        } else {
            Log.d("xink", "succeeded to connect to VPN service");
            return true;
        }
    }

    private Object invokeStubMethod(final Object stub, final String methodName, final Object... args) {
        try {
            Method method = findMethod(stub.getClass(), methodName, args);
            return method.invoke(stub, args);
        } catch (Throwable e) {
            throw new IllegalArgumentException("failed to invoke mehod '" + methodName + "' on stub", e);
        }
    }

    private Method findMethod(final Class<?> stubClass, final String methodName, final Object... args) throws NoSuchMethodException {
        Class<?>[] argTypes = new Class<?>[args.length];
        int i = 0;
        for (Object arg : args) {
            argTypes[i++] = arg.getClass();
        }

        Method method = stubClass.getMethod(methodName, argTypes);
        return method;
    }

    public void disconnect() {

    }
}