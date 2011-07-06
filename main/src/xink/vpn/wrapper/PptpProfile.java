package xink.vpn.wrapper;

import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.AppException;
import android.content.Context;

public class PptpProfile extends VpnProfile {

    public PptpProfile(final Context ctx) {
        super(ctx, "android.net.vpn.PptpProfile");
    }

    @Override
    public VpnType getType() {
        return VpnType.PPTP;
    }

    /**
     * Enables/disables the encryption for PPTP tunnel.
     */
    public void setEncryptionEnabled(final boolean enabled) {
        try {
            Method m = getStubClass().getMethod("setEncryptionEnabled", boolean.class);
            m.invoke(getStub(), enabled);
        } catch (Throwable e) {
            throw new AppException("setEncryptionEnabled failed", e);
        }
    }

    public boolean isEncryptionEnabled() {
        return invokeStubMethod("isEncryptionEnabled");
    }

    @Override
    public void toJson(final JSONObject jo) throws JSONException {
        super.toJson(jo);
        jo.put("encryptionEnabled", isEncryptionEnabled());
    }

    @Override
    public void fromJson(final JSONObject jo) throws JSONException {
        super.fromJson(jo);
        setEncryptionEnabled(jo.getBoolean("encryptionEnabled"));
    }
}
