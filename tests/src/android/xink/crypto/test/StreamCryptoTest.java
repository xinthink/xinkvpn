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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xink.crypto.StreamCrypto;
import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnType;
import android.test.AndroidTestCase;

/**
 * 字节流加解密的测试
 *
 * @author ywu
 *
 */
public class StreamCryptoTest extends AndroidTestCase {

    private String segment = "0123456789abcdef";

    // 新的密钥保存方法应能还原老版本的备份文件
    private final String EXP_BAK_ID = "ee6edc7c-0a05-49b2-9a83-5b87130209b8";

    private final String EXP_BAK_1_NAME = "pptp";
    private final String EXP_BAK_1_SERVER = "192.168.10.100";
    private final boolean EXP_BAK_1_ENCRYPT = false;
    private final String EXP_BAK_1_DNS = "8.8.8.8";
    private final String EXP_BAK_1_USR = "usr";
    private final String EXP_BAK_1_PASSWD = "psw";

    private final String EXP_BAK_2_NAME = "l2tp";
    private final String EXP_BAK_2_SERVER = "192.168.10.101";
    private final boolean EXP_BAK_2_ENCRYPT = false;
    private final String EXP_BAK_2_DNS = "8.8.8.8";
    private final String EXP_BAK_2_USR = "usr";
    private final String EXP_BAK_2_PASSWD = "psw";

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        StreamCrypto.init(mContext);
    }

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

    /**
     * 测试对象流加解密
     */
    public void testObjectStreamCrypto() throws Exception {
        Data d = new Data();
        d.id = 1;
        d.ts = new Date();
        d.value = "v1";

        byte[] cipherText = encryptObject(d);
        Data resultObj = decryptObject(cipherText);
        assertEquals(d, resultObj);
    }

    /**
     * 测试null对象加解密的情况
     */
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

    /**
     * 保证能够解密3.1之前的版本加密的数据文件
     */
    public void testDecryptLagecyActiveIdFile() throws Exception {
        String activeId = (String) loadLegacyDataFile("old_active_profile_id").readObject();
        assertEquals("should restore active-profile-id correctly", EXP_BAK_ID, activeId);
    }

    /**
     * 保证能够解密3.1之前的版本加密的数据文件
     */
    public void testDecryptLagecyProfilesFile() throws Exception {
        ObjectInputStream ois = loadLegacyDataFile("old_profiles");
        List<VpnProfile> profiles = loadProfilesFrom(ois);

        assertEquals(2, profiles.size());

        VpnProfile p1 = profiles.get(0);
        assertEquals(VpnType.PPTP, p1.getType());
        assertEquals(EXP_BAK_1_NAME, p1.getName());
        assertEquals(EXP_BAK_1_SERVER, p1.getServerName());
        assertEquals(EXP_BAK_1_ENCRYPT, ((PptpProfile) p1).isEncryptionEnabled());
        assertEquals(EXP_BAK_1_USR, p1.getUsername());
        assertEquals(EXP_BAK_1_PASSWD, p1.getPassword());
        assertEquals(EXP_BAK_1_DNS, p1.getDomainSuffices());

        VpnProfile p2 = profiles.get(1);
        assertEquals(VpnType.L2TP, p2.getType());
        assertEquals(EXP_BAK_2_NAME, p2.getName());
        assertEquals(EXP_BAK_2_SERVER, p2.getServerName());
        assertEquals(EXP_BAK_2_ENCRYPT, ((L2tpProfile) p2).isSecretEnabled());
        assertEquals(EXP_BAK_2_USR, p2.getUsername());
        assertEquals(EXP_BAK_2_PASSWD, p2.getPassword());
        assertEquals(EXP_BAK_2_DNS, p2.getDomainSuffices());
    }

    private ObjectInputStream loadLegacyDataFile(final String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);
        assertNotNull(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamCrypto.decrypt(in, out);
        byte[] data = out.toByteArray();

        return new ObjectInputStream(new ByteArrayInputStream(data));
    }

    private List<VpnProfile> loadProfilesFrom(final ObjectInputStream is) throws Exception {
        List<VpnProfile> profiles = new ArrayList<VpnProfile>();

        try {
            while (true) {
                VpnType type = (VpnType) is.readObject();
                Object obj = is.readObject();
                assertNotNull(obj);

                VpnProfile p = VpnProfile.newInstance(type, mContext);
                if (p.isCompatible(obj)) {
                    p.read(obj, is);
                    profiles.add(p);
                } else {
                    fail("saved profile '" + obj + "' is NOT compatible with " + type);
                }
            }
        } catch (EOFException eof) {
            // reach end of file
        }

        return profiles;
    }
}

/**
 * 用于测试的数据对象
 */
class Data implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    Date ts;
    String value;

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Data))
            return false;

        Data that = (Data) obj;

        return this.id == that.id && this.ts.equals(that.ts) && this.value.equals(that.value);
    }
}
