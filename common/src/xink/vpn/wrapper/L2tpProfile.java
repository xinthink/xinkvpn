package xink.vpn.wrapper;

import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.AppException;
import xink.vpn.R;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class L2tpProfile extends VpnProfile {

    /** Key prefix for L2TP VPN. */
    public static final String KEY_PREFIX_L2TP_SECRET = "VPN_l";

    private KeyStore keyStore;

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
        try {
            Method m = getStubClass().getMethod("setSecretEnabled", boolean.class);
            m.invoke(getStub(), enabled);
        } catch (Throwable e) {
            throw new AppException("setSecretEnabled failed", e);
        }
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

    @Override
    public void validate() {
        super.validate();

        if (isSecretEnabled() && TextUtils.isEmpty(getSecretString())) {
            throw new InvalidProfileException("secret is empty", R.string.err_empty_secret);
        }
    }

    @Override
    public void postConstruct() {
        super.postConstruct();

        processSecret();
    }

    @Override
    public void postUpdate() {
        super.postUpdate();

        processSecret();
    }

    @Override
    public void preConnect() {
        super.preConnect();

        processSecret();
    }

    protected void processSecret() {
        String key = makeKey();

        if (isSecretEnabled()) {
            String secret = getSecretString();

            if (!getKeyStore().put(key, secret)) {
                Log.e("xink", "keystore write failed: key=" + key);
            }
        } else {
            getKeyStore().delete(key);
        }
    }

    private String makeKey() {
        return KEY_PREFIX_L2TP_SECRET + getId();
    }

    @Override
    public boolean needKeyStoreToSave() {
        return isSecretEnabled() && !TextUtils.isEmpty(getSecretString());
    }

    @Override
    public boolean needKeyStoreToConnect() {
        return isSecretEnabled();
    }

    protected KeyStore getKeyStore() {
        if (keyStore == null) {
            keyStore = new KeyStore(getContext());
        }
        return keyStore;
    }

    @Override
    public L2tpProfile dulicateToConnect() {
        L2tpProfile p = (L2tpProfile) super.dulicateToConnect();
        boolean secretEnabled = isSecretEnabled();
        p.setSecretEnabled(secretEnabled);
        if (secretEnabled) {
            p.setSecretString(makeKey());
        }

        return p;
    }

    @Override
    public void toJson(final JSONObject jo) throws JSONException {
        super.toJson(jo);
        jo.put("secretEnabled", isSecretEnabled());
        jo.put("secretString", getSecretString());
    }

    @Override
    public void fromJson(final JSONObject jo) throws JSONException {
        super.fromJson(jo);
        setSecretEnabled(jo.getBoolean("secretEnabled"));
        setSecretString(jo.optString("secretString"));
    }
}
