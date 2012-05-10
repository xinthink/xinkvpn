/*
 * Copyright 2011 yingxinwu.g@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.vpn;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class L2tpIpsecPskProfile extends L2tpProfile {

    /** Key prefix for VPN. */
    public static final String KEY_PREFIX_IPSEC_PSK = "VPN_i";

    public String psk;

    public L2tpIpsecPskProfile() {
        type = VpnType.L2TP_IPSEC_PSK;
    }

    @Override
    public void validate() {
        super.validate();

        if (TextUtils.isEmpty(psk))
            throw new InvalidProfileException("presharedKey is empty", R.string.err_empty_psk);
    }

    @Override
    protected void processSecret() {
        super.processSecret();

        String key = makeKey();
        if (!getKeyStore().put(key, psk)) {
            Log.e("xink", "keystore write failed: key=" + key);
        }
    }

    private String makeKey() {
        return KEY_PREFIX_IPSEC_PSK + id;
    }

    @Override
    public boolean needKeyStoreToSave() {
        return super.needKeyStoreToSave() || !TextUtils.isEmpty(psk);
    }

    @Override
    public boolean needKeyStoreToConnect() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.L2tpProfile#clone()
     */
    @Override
    public L2tpIpsecPskProfile clone() {
        return (L2tpIpsecPskProfile) super.clone();
    }

    @Override
    public L2tpIpsecPskProfile clone4Connect() {
        L2tpIpsecPskProfile p = (L2tpIpsecPskProfile) super.clone4Connect();
        p.psk = makeKey(); // use key instead of psk itself (stored in KeyStore, indexed by a key)

        return p;
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.L2tpProfile#saveTo(org.json.JSONObject)
     */
    @Override
    protected void saveTo(final JSONObject jo) throws JSONException {
        super.saveTo(jo);
        jo.put("psk", psk);
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.L2tpProfile#restoreFrom(org.json.JSONObject)
     */
    @Override
    protected void restoreFrom(final JSONObject jo) throws JSONException {
        super.restoreFrom(jo);
        psk = jo.getString("psk");
    }
}
