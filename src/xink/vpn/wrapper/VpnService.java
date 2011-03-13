package xink.vpn.wrapper;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.IBinder;

public class VpnService extends AbstractWrapper {

    public VpnService(final Context ctx) {
        super(ctx, "android.net.vpn.IVpnService$Stub");
    }

    @Override
    protected Object createStubObject(final Class<?> clazz) {
        return null;
    }

    public boolean connect(final IBinder service, final VpnProfile profile) throws Exception {
        asInterface(service);

        Method m = getStubClass().getMethod("connect", profile.getGenericProfileClass(), String.class, String.class);
        return (Boolean) m.invoke(getStub(), profile.getStub(), profile.getUsername(), profile.getPassword());
    }

    public void disconnect(final IBinder service) throws Exception {
        asInterface(service);
        invokeStubMethod("disconnect");
    }

    public void checkStatus(final IBinder service, final VpnProfile p) throws Exception {
        asInterface(service);
        Method m = getStubClass().getMethod("checkStatus", p.getGenericProfileClass());
        m.invoke(getStub(), p.getStub());
    }

    private void asInterface(final IBinder service) throws Exception {
        Method method = getStubClass().getMethod("asInterface", IBinder.class);
        setStub(method.invoke(null, service));
    }
}
