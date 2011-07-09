package xink.vpn.test;

import java.lang.reflect.Method;

import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnManager;
import xink.vpn.wrapper.VpnService;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.test.AndroidTestCase;

public class CompatibleTest extends AndroidTestCase {

    public void testVpnManager() throws Exception {
        VpnManager vpnMgr = new VpnManager(getContext());
        Class<?> stubClass = vpnMgr.getStubClass();

        assertMethodDefined(stubClass, VpnManager.METHOD_START_VPN_SERVICE);
        assertMethodDefined(stubClass, VpnManager.METHOD_STOP_VPN_SERVICE);
        assertMethodDefined(stubClass, VpnManager.METHOD_BIND_VPN_SERVICE, ServiceConnection.class);
    }

    public void testVpnService() throws Exception {
        VpnService vpnSrv = new VpnService(getContext());
        Class<?> stubClass = vpnSrv.getStubClass();

        assertMethodDefined(stubClass, "asInterface", IBinder.class);

        Class<?> profileClass = new PptpProfile(getContext()).getGenericProfileClass();
        assertMethodDefined(stubClass, "connect", profileClass, String.class, String.class);
        assertMethodDefined(stubClass, "disconnect");
        assertMethodDefined(stubClass, "checkStatus", profileClass);
    }

    public void testKeyStore() throws Exception {
        KeyStore ks = new KeyStore(getContext());
        Class<?> stubClass = ks.getStubClass();

        assertMethodDefined(stubClass, "getInstance");
        assertMethodDefined(stubClass, "test");
        assertMethodDefined(stubClass, "getLastError");
        assertMethodDefined(stubClass, "execute", int.class, byte[][].class);
        assertMethodDefined(stubClass, "put", String.class, String.class);
        assertMethodDefined(stubClass, "contains", String.class);
        assertMethodDefined(stubClass, "delete", String.class);
    }

    private static void assertMethodDefined(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        Method m = getMethod(clazz, methodName, false, parameterTypes);

        if (m == null) {
            // try class's declared methods
            m = getMethod(clazz, methodName, true, parameterTypes);
        }

        assertNotNull(methodName + " method not found", m);
    }

    private static Method getMethod(final Class<?> clazz, final String methodName, final boolean declaredOnly, final Class<?>... parameterTypes) {
        Method m = null;

        try {
            m = declaredOnly ? clazz.getDeclaredMethod(methodName, parameterTypes) : clazz.getMethod(methodName, parameterTypes);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return m;
    }
}
