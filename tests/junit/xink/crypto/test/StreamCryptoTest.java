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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.junit.Test;

import xink.crypto.StreamCrypto;

public class StreamCryptoTest {

    private String segment = "0123456789abcdef";

    @Test
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

        byte[] cipherText = encrpt(clearText);
        String resultText = decrpt(cipherText);
        assertEquals("fails when segment copies is " + copy, clearText, resultText);
    }

    private byte[] encrpt(final String clearText) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(clearText.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamCrypto.encrypt(in, out);
        byte[] cipherText = out.toByteArray();
        System.out.println('\'' + clearText + "' --> " + printBytes(cipherText));
        return cipherText;
    }

    private String decrpt(final byte[] cipherText) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(cipherText);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamCrypto.decrypt(in, out);
        return new String(out.toByteArray());
    }

    private static String printBytes(final byte[] bytes) {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(Integer.toHexString(b));
        }
        return buf.toString();
    }

    @Test
    public void testObjectStreamCrypto() throws Exception {
        Data d = new Data();
        d.id = 1;
        d.ts = new Date();
        d.value = "v1";

        byte[] cipherText = encryptObject(d);
        Data resultObj = decryptObject(cipherText);
        assertEquals(d, resultObj);
    }

    @Test
    public void testNullObjectCrypto() throws Exception {
        byte[] cipherText = encryptObject(null);
        Data resultObj = decryptObject(cipherText);
        assertNull(resultObj);
    }

    private byte[] encryptObject(final Serializable obj) throws Exception {
        byte[] objBytes = obj2bytes(obj);

        ByteArrayInputStream in = new ByteArrayInputStream(objBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamCrypto.encrypt(in, out);
        byte[] cipherText = out.toByteArray();
        System.out.println(obj + " --> " + printBytes(cipherText));
        return cipherText;
    }

    private Data decryptObject(final byte[] cipherText) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(cipherText);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamCrypto.decrypt(in, out);
        Data d = (Data) bytes2obj(out.toByteArray());
        return d;
    }

    private static byte[] obj2bytes(final Serializable d) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream producer = new ObjectOutputStream(bytes);
        producer.writeObject(d);
        return bytes.toByteArray();
    }

    private static Serializable bytes2obj(final byte[] bytes) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream consumer = new ObjectInputStream(in);
        Serializable d = (Serializable) consumer.readObject();
        return d;
    }
}

class Data implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    Date ts;
    String value;

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Data)) {
            return false;
        }

        Data that = (Data) obj;

        return this.id == that.id && this.ts.equals(that.ts) && this.value.equals(that.value);
    }
}
