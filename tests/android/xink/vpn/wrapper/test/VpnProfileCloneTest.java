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

package xink.vpn.wrapper.test;

import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.test.AndroidTestCase;

public class VpnProfileCloneTest extends AndroidTestCase {
    String[] genericProfile;

    @Override
    public void setUp() {
        String id = "vpn1";
        String name = "pptp";
        String server = "vpnServer";
        String user = "admin";
        String psswd = "psswd";
        String domainSuffix = "8.8.8.8";
        genericProfile = new String[] { id, name, server, user, psswd, domainSuffix };
    }

    public void testPptpClone() throws Exception {
        PptpProfile p = new PptpProfile(mContext);
        populateGenericProps(genericProfile, p);
        p.setEncryptionEnabled(false);

        assertGenericProps(genericProfile, p);
        assertFalse(p.isEncryptionEnabled());

        // test cloning default values
        PptpProfile clone = p.dulicateToConnect();
        assertGenericProps(genericProfile, clone);
        assertFalse(clone.isEncryptionEnabled());

        // test cloning encrypted pptp
        p.setEncryptionEnabled(true);
        clone = p.dulicateToConnect();
        assertGenericProps(genericProfile, clone);
        assertTrue(clone.isEncryptionEnabled());
    }

    public void testL2tpClone() throws Exception {
        L2tpProfile p = new L2tpProfile(mContext);
        populateGenericProps(genericProfile, p);
        p.setSecretEnabled(false);

        assertGenericProps(genericProfile, p);
        assertFalse(p.isSecretEnabled());
        assertNull(p.getSecretString());

        // test cloning default values
        L2tpProfile clone = p.dulicateToConnect();
        assertFalse(clone.isSecretEnabled());
        assertNull(clone.getSecretString());

        // test cloning with secret string enabled
        p.setSecretEnabled(true);

        clone = p.dulicateToConnect();
        assertGenericProps(genericProfile, clone);
        assertTrue(clone.isSecretEnabled());
        assertEquals(L2tpProfile.KEY_PREFIX_L2TP_SECRET + clone.getId(), clone.getSecretString());
    }

    public void testL2tpIpsecClone() throws Exception {
        L2tpIpsecPskProfile p = new L2tpIpsecPskProfile(mContext);
        populateGenericProps(genericProfile, p);
        p.setSecretEnabled(false);

        assertGenericProps(genericProfile, p);
        assertFalse(p.isSecretEnabled());
        assertNull(p.getSecretString());
        assertNull(p.getPresharedKey());

        // test cloning default values
        L2tpIpsecPskProfile clone = p.dulicateToConnect();
        assertFalse(clone.isSecretEnabled());
        assertNull(clone.getSecretString());
        assertEquals(L2tpIpsecPskProfile.KEY_PREFIX_IPSEC_PSK + clone.getId(), clone.getPresharedKey());

        // test cloning with secret string enabled
        p.setSecretEnabled(true);

        clone = p.dulicateToConnect();
        assertGenericProps(genericProfile, clone);
        assertTrue(clone.isSecretEnabled());
        assertEquals(L2tpProfile.KEY_PREFIX_L2TP_SECRET + clone.getId(), clone.getSecretString());
        assertEquals(L2tpIpsecPskProfile.KEY_PREFIX_IPSEC_PSK + clone.getId(), clone.getPresharedKey());
    }

    static void populateGenericProps(final String[] values, final VpnProfile target) {
        target.setId(values[0]);
        target.setName(values[1]);
        target.setServerName(values[2]);
        target.setUsername(values[3]);
        target.setPassword(values[4]);
        target.setDomainSuffices(values[5]);
    }

    static void assertGenericProps(final String[] expd, final VpnProfile actual) {
        assertEquals(expd[0], actual.getId());
        assertEquals(expd[1], actual.getName());
        assertEquals(expd[2], actual.getServerName());
        assertEquals(expd[3], actual.getUsername());
        assertEquals(expd[4], actual.getPassword());
        assertEquals(expd[5], actual.getDomainSuffices());
    }
}
