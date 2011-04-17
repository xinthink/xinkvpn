package xink.vpn.test;

import java.lang.reflect.Method;

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

    private static void assertMethodDefined(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method m = clazz.getMethod(methodName, parameterTypes);
        assertNotNull(methodName + " method not found", m);
    }
}
