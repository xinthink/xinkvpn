package xink.vpn.wrapper;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeyStore extends AbstractWrapper {

    public static final String UNLOCK_ACTION = "android.credentials.UNLOCK";

    public static final int NO_ERROR = 1;
    public static final int LOCKED = 2;
    public static final int UNINITIALIZED = 3;
    public static final int SYSTEM_ERROR = 4;
    public static final int PROTOCOL_ERROR = 5;
    public static final int PERMISSION_DENIED = 6;
    public static final int KEY_NOT_FOUND = 7;
    public static final int VALUE_CORRUPTED = 8;
    public static final int UNDEFINED_ACTION = 9;
    public static final int WRONG_PASSWORD = 10;

    public KeyStore(final Context ctx) {
        super(ctx, "android.security.KeyStore");
    }

    @Override
    protected Object createStubObject(final Class<?> clazz) throws Exception {
        return getStubInstance();
    }

    private Object getStubInstance() throws Exception {
        Method method = getStubClass().getMethod("getInstance");
        return method.invoke(null);
    }

    public boolean put(final String key, final String value) {
        return invokeStubMethod("put", key, value);
    }

    public boolean contains(final VpnProfile p) {
        String key = L2tpIpsecPskProfile.KEY_PREFIX_IPSEC_PSK + p.getId();
        return invokeStubMethod("contains", key);
    }

    public void unlock(final Activity ctx) {
        try {
            Intent intent = new Intent(UNLOCK_ACTION);
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w("xink", "unlock credentials failed", e);
        }
    }

    public boolean isUnlocked() {
        int err = invokeStubMethod("test");
        Log.i("xink", "KeyStore.test result is: " + err);
        return err == NO_ERROR;
    }
}
