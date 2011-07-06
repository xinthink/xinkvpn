package xink.vpn.wrapper;

import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.R;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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

        if (TextUtils.isEmpty(getPresharedKey())) {
            throw new InvalidProfileException("presharedKey is empty", R.string.err_empty_psk);
        }
    }

    @Override
    protected void processSecret() {
        super.processSecret();

        String psk = getPresharedKey();
        String key = makeKey();
        if (!getKeyStore().put(key, psk)) {
            Log.e("xink", "keystore write failed: key=" + key);
        }
    }

    private String makeKey() {
        return KEY_PREFIX_IPSEC_PSK + getId();
    }

    @Override
    public boolean needKeyStoreToSave() {
        return super.needKeyStoreToSave() || !TextUtils.isEmpty(getPresharedKey());
    }

    @Override
    public boolean needKeyStoreToConnect() {
        return true;
    }

    @Override
    public L2tpIpsecPskProfile dulicateToConnect() {
        L2tpIpsecPskProfile p = (L2tpIpsecPskProfile) super.dulicateToConnect();
        p.setPresharedKey(makeKey());

        return p;
    }

    @Override
    public void toJson(final JSONObject jo) throws JSONException {
        super.toJson(jo);
        jo.put("presharedKey", getPresharedKey());
    }

    @Override
    public void fromJson(final JSONObject jo) throws JSONException {
        super.fromJson(jo);
        setPresharedKey(jo.optString("presharedKey"));
    }
}
