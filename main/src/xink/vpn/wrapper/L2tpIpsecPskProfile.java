package xink.vpn.wrapper;

import xink.vpn.R;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

public class L2tpIpsecPskProfile extends L2tpProfile {

    /** Key prefix for VPN. */
    public static final String KEY_PREFIX_IPSEC_PSK = "VPN_i";

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

    @Override
    public void validate() {
        super.validate();

        String psk = getPresharedKey();
        if (TextUtils.isEmpty(psk)) {
            throw new InvalidProfileException("presharedKey is empty", R.string.err_empty_psk);
        }
    }

    @Override
    public void postConstruct() {
        super.postConstruct();

        String psk = getPresharedKey();
        new KeyStore(getContext()).put(KEY_PREFIX_IPSEC_PSK + getId(), psk);
    }

    @Override
    public void preConnect(final Activity activity) {
        new KeyStore(activity).unlock();
    }
}
