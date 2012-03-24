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

package xink.vpn.wrapper;

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
}
