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

package xink.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import xink.vpn.Assert;
import xink.vpn.R;
import xink.vpn.XinkVpnApp;

public final class AesCrypto {

    private static byte[] _rawkey_cache;

    private AesCrypto() {
        // no instance
    }

    /**
     * Encrypt a given string.
     * 
     * @throws GeneralSecurityException when encrypt error occurs
     */
    public static byte[] encrypt(final String in) throws GeneralSecurityException {
        return process(Cipher.ENCRYPT_MODE, in.getBytes());
    }

    /**
     * Decrypt the given bytes.
     * 
     * @throws GeneralSecurityException when decrypt error occurs
     */
    public static String decrypt(final byte[] in) throws GeneralSecurityException {
        return new String(process(Cipher.DECRYPT_MODE, in));
    }

    private static byte[] process(final int mode, final byte[] input) throws GeneralSecurityException {
        Cipher c = getCipher(mode);
        return c.doFinal(input);
    }

    private static Cipher getCipher(final int mode) throws GeneralSecurityException {
        byte[] rawkey = getRawKey();
        SecretKeySpec skeySpec = new SecretKeySpec(rawkey, "AES");
        IvParameterSpec ivps = new IvParameterSpec(rawkey);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(mode, skeySpec, ivps);
        return c;
    }

    private static byte[] getRawKey() {
        if (_rawkey_cache == null) {
            _rawkey_cache = XinkVpnApp.i().getString(R.string.crypto_raw_key).getBytes();
            Assert.isEquals(_rawkey_cache.length, 16, "AES-128 requires a 16 bytes raw key");
        }

        return _rawkey_cache;
    }
}
