/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xink.vpn.test;

import static xink.vpn.VpnState.*;
import static xink.vpn.VpnType.*;

import org.json.JSONException;

import xink.vpn.L2tpIpsecPskProfile;
import xink.vpn.L2tpProfile;
import xink.vpn.VpnProfile;

/**
 * @author ywu
 *
 */
public class L2tpIpsecPskProfileSerializationTest extends L2tpProfileSerializationTest {

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        json.put("type", L2TP_IPSEC_PSK);
        json.put("psk", "ipsec psk");
        ((L2tpIpsecPskProfile) p).psk = "ipsec psk";
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.test.L2tpProfileSerializationTest#newL2tpProfile()
     */
    @Override
    protected L2tpProfile newL2tpProfile() {
        return new L2tpIpsecPskProfile();
    }

    /**
     * deserializing l2tp profile, with all fields
     */
    @Override
    public void testL2tpFromJson() throws JSONException {
        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof L2tpIpsecPskProfile);
        assertEquals(L2TP_IPSEC_PSK, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("l2tp one", p.name);
        assertEquals("l2tp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("8.8.8.8", p.domainSuffices);
        assertTrue(((L2tpProfile) p).secretEnabled);
        assertEquals("l2tp secret", ((L2tpProfile) p).secret);
        assertEquals("ipsec psk", ((L2tpIpsecPskProfile) p).psk);
    }

    /**
     * deserializing l2tp profile, lack of some fields
     */
    @Override
    public void testL2tpFromPartialJson() throws JSONException {
        json.remove("domain");
        json.put("secretEnabled", false);
        json.remove("secret");

        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof L2tpIpsecPskProfile);
        assertEquals(L2TP_IPSEC_PSK, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("l2tp one", p.name);
        assertEquals("l2tp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("", p.domainSuffices);
        assertFalse(((L2tpProfile) p).secretEnabled);
        assertEquals("", ((L2tpProfile) p).secret);
        assertEquals("ipsec psk", ((L2tpIpsecPskProfile) p).psk);
    }
}
