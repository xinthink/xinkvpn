package xink.vpn.wrapper;

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
        invokeStubMethod("setEncryptionEnabled", enabled);
    }

    public boolean isEncryptionEnabled() {
        return invokeStubMethod("getEncryptionEnabled");
    }
}
