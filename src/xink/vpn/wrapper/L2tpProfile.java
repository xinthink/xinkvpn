package xink.vpn.wrapper;

import android.content.Context;

public class L2tpProfile extends VpnProfile {

    protected L2tpProfile(final Context ctx, final String stubClass) {
        super(ctx, stubClass);
    }

    public L2tpProfile(final Context ctx) {
        super(ctx, "android.net.vpn.L2tpProfile");
    }

    @Override
    public VpnType getType() {
        return VpnType.L2TP;
    }

    /**
     * Enables/disables the secret for authenticating tunnel connection.
     */
    public void setSecretEnabled(final boolean enabled) {
        invokeStubMethod("setSecretEnabled", enabled);
    }

    public boolean isSecretEnabled() {
        return invokeStubMethod("isSecretEnabled");
    }

    public void setSecretString(final String secret) {
        invokeStubMethod("setSecretString", secret);
    }

    public String getSecretString() {
        return invokeStubMethod("getSecretString");
    }
}
