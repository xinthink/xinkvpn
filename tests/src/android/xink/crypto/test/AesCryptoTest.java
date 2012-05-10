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

package xink.crypto.test;

import xink.crypto.AesCrypto;
import android.test.AndroidTestCase;

/**
 * 加解密测试
 * 
 * @author ywu
 * 
 */
public class AesCryptoTest extends AndroidTestCase {

    private String segment = "0123456789abcdef";

    /**
     * 测试加解密文本
     */
    public void testTextCrypto() throws Exception {
        textCrypto(0);
        textCrypto(1);
        textCrypto(8); // 128 bytes
        textCrypto(9);
    }

    private void textCrypto(final int copy) throws Exception {
        System.out.println("textCrypto, segment copies is " + copy);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < copy; i++) {
            buf.append(segment);
        }
        String clearText = buf.toString();

        byte[] cipherText = AesCrypto.encrypt(clearText);
        String resultText = AesCrypto.decrypt(cipherText);
        assertEquals("fails when segment copies is " + copy, clearText, resultText);
    }

}
