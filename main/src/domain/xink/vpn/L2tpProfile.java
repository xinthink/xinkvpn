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

import xink.vpn.wrapper.KeyStore;
import android.text.TextUtils;
import android.util.Log;

public class L2tpProfile extends VpnProfile {

    /** Key prefix for L2TP VPN. */
    public static final String KEY_PREFIX_L2TP_SECRET = "VPN_l";

    public boolean secretEnabled;

    public String secret;

    public L2tpProfile() {
        type = VpnType.L2TP;
    }

    @Override
    public void validate() {
        super.validate();

        if (secretEnabled && TextUtils.isEmpty(secret))
            throw new InvalidProfileException("secret is empty", R.string.err_empty_secret);
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

        if (secretEnabled) {
            if (!getKeyStore().put(key, secret)) {
                Log.e("xink", "keystore write failed: key=" + key);
            }
        } else {
            getKeyStore().delete(key);
        }
    }

    private String makeKey() {
        return KEY_PREFIX_L2TP_SECRET + id;
    }

    @Override
    public boolean needKeyStoreToSave() {
        return secretEnabled;
    }

    @Override
    public boolean needKeyStoreToConnect() {
        return secretEnabled;
    }

    protected KeyStore getKeyStore() {
        return XinkVpnApp.i().getKeyStoreService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.VpnProfile#clone()
     */
    @Override
    public L2tpProfile clone() {
        return (L2tpProfile) super.clone();
    }

    @Override
    public L2tpProfile clone4Connect() {
        L2tpProfile p = (L2tpProfile) super.clone4Connect();
        if (secretEnabled) {
            p.secret = makeKey(); // use key instead of secret string
        }

        return p;
    }
}
