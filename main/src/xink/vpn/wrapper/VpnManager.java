package xink.vpn.wrapper;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.ServiceConnection;

public class VpnManager extends AbstractWrapper {

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
        invokeStubMethod("startVpnService");
    }

    /**
     * Stops the VPN service.
     */
    public void stopVpnService() {
        invokeStubMethod("stopVpnService");
    }

    /**
     * Binds the specified ServiceConnection with the VPN service.
     */
    public boolean bindVpnService(final ServiceConnection c) {
        try {
            Method m = getStubClass().getMethod("bindVpnService", ServiceConnection.class);
            return (Boolean) m.invoke(getStub(), c);
        } catch (Throwable e) {
            throw new WrapperException("bindVpnService failed", e);
        }
    }
}