package xink.vpn.wrapper;

import android.content.Context;

public class L2tpIpsecPskProfile extends L2tpProfile {

    public L2tpIpsecPskProfile(final Context ctx) {
        super(ctx, "android.net.vpn.L2tpIpsecPskProfile");
    }

    @Override
    public VpnType getType() {
        return VpnType.L2TP_IPSEC_PSK;
    }

    public void setPresharedKey(final String key) {
        invokeStubMethod("setPresharedKey", key);
    }

    public String getPresharedKey() {
        return invokeStubMethod("getPresharedKey");
    }
}
