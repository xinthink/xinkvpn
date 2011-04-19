package xink.vpn.wrapper;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.ServiceConnection;

public class VpnManager extends AbstractWrapper {

    public static final String METHOD_BIND_VPN_SERVICE = "bindVpnService";
    public static final String METHOD_STOP_VPN_SERVICE = "stopVpnService";
    public static final String METHOD_START_VPN_SERVICE = "startVpnService";

	public VpnManager(final Context ctx) {
        super(ctx, "android.net.vpn.VpnManager");
    }

    @Override
    protected Object createStubObject(final Class<?> clazz) throws Exception {
        return clazz.getConstructor(Context.class).newInstance(getContext());
    }

    /**
     * Starts the VPN service to establish VPN connection.
     */
    public void startVpnService() {
        invokeStubMethod(METHOD_START_VPN_SERVICE);
    }

    /**
     * Stops the VPN service.
     */
    public void stopVpnService() {
        invokeStubMethod(METHOD_STOP_VPN_SERVICE);
    }

    /**
     * Binds the specified ServiceConnection with the VPN service.
     */
    public boolean bindVpnService(final ServiceConnection c) {
        try {
            Method m = getStubClass().getMethod(METHOD_BIND_VPN_SERVICE, ServiceConnection.class);
            return (Boolean) m.invoke(getStub(), c);
        } catch (Throwable e) {
            throw new WrapperException("bindVpnService failed", e);
        }
    }
}