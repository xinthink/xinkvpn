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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import xink.vpn.Assert;
import xink.vpn.R;
import android.content.Context;

public final class StreamCrypto {

    /*
     * Buffer size when reading input.
     */
    private static final int BUF_SIZE = 128;

    private static byte[] rawKey;

    /**
     * Initialization.
     * 
     * @param ctx
     *            Context
     */
    public static void init(final Context ctx) {
        rawKey = ctx.getString(R.string.crypto_raw_key).getBytes();
        Assert.isEquals(rawKey.length, 16, "AES-128 requires a 16 bytes raw key");
    }

    /**
     * Encrypt bytes from input stream, and writes to the output stream.<br>
     * input and output stream will be closed before this method returns.
     *
     * @throws IOException
     *             when read/write file failed
     * @throws GeneralSecurityException
     *             when encrypt error occurs
     */
    public static void encrypt(final InputStream in, final OutputStream out) throws IOException, GeneralSecurityException {
        process(Cipher.ENCRYPT_MODE, in, out);
    }

    /**
     * Decrypt bytes from input stream, and writes to the output stream.<br>
     * input and output stream will be closed before this method returns.
     *
     * @throws IOException
     *             when read/write file failed
     * @throws GeneralSecurityException
     *             when decrypt error occurs
     */
    public static void decrypt(final InputStream in, final OutputStream out) throws IOException, GeneralSecurityException {
        process(Cipher.DECRYPT_MODE, in, out);
    }

    private static void process(final int mode, final InputStream in, final OutputStream out) throws IOException, GeneralSecurityException {
        CipherOutputStream co = null;

        try {
            co = doProcess(mode, in, out);
        } finally {
            in.close();

            if (co != null) {
                co.close();
            }
        }
    }

    private static CipherOutputStream doProcess(final int mode, final InputStream in, final OutputStream out) throws IOException,
            GeneralSecurityException {
        Cipher c = getCipher(mode);
        CipherOutputStream co = new CipherOutputStream(out, c);

        byte[] buf = new byte[BUF_SIZE];
        int len = -1;
        while ((len = in.read(buf)) > 0) {
            co.write(buf, 0, len);
        }
        return co;
    }

    private static Cipher getCipher(final int mode) throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        IvParameterSpec ivps = new IvParameterSpec(rawKey);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(mode, skeySpec, ivps);
        return c;
    }

    private StreamCrypto() {

    }
}
