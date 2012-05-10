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
import static xink.vpn.test.helper.JsonAssert.*;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.L2tpProfile;
import xink.vpn.VpnProfile;

/**
 * @author ywu
 * 
 */
public class L2tpProfileSerializationTest extends TestCase {

    protected JSONObject json;

    protected L2tpProfile p;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        json = new JSONObject();
        json.put("type", L2TP);
        json.put("id", "1");
        json.put("name", "l2tp one");
        json.put("server", "l2tp server");
        json.put("username", "usr");
        json.put("password", "passwd");
        json.put("domain", "8.8.8.8");
        json.put("secretEnabled", true);
        json.putOpt("secret", "l2tp secret");

        p = newL2tpProfile();
        p.id = "1";
        p.name = "l2tp one";
        p.server = "l2tp server";
        p.username = "usr";
        p.password = "passwd";
        p.domainSuffices = "8.8.8.8";
        p.secretEnabled = true;
        p.secret = "l2tp secret";
    }

    protected L2tpProfile newL2tpProfile() {
        return new L2tpProfile();
    }

    /**
     * deserializing l2tp profile, with all fields
     */
    public void testL2tpFromJson() throws JSONException {
        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof L2tpProfile);
        assertEquals(L2TP, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("l2tp one", p.name);
        assertEquals("l2tp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("8.8.8.8", p.domainSuffices);
        assertTrue(((L2tpProfile) p).secretEnabled);
        assertEquals("l2tp secret", ((L2tpProfile) p).secret);
    }

    /**
     * deserializing l2tp profile, lack of some fields
     */
    public void testL2tpFromPartialJson() throws JSONException {
        json.remove("domain");
        json.put("secretEnabled", false);
        json.remove("secret");

        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof L2tpProfile);
        assertEquals(L2TP, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("l2tp one", p.name);
        assertEquals("l2tp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("", p.domainSuffices);
        assertFalse(((L2tpProfile) p).secretEnabled);
        assertEquals("", ((L2tpProfile) p).secret);
    }

    /**
     * serializing l2tp profile, with all fields
     */
    public void testL2tpToJson() throws JSONException {
        JSONObject result = p.toJson();
        assertJsonEquals(json, result);
    }

    /**
     * serializing l2tp profile, lack of some fields
     */
    public void testL2tpToPartialJson() throws JSONException {
        p.domainSuffices = null;
        p.secretEnabled = false;
        p.secret = null;

        JSONObject result = p.toJson();

        // expected json
        json.remove("domain");
        json.put("secretEnabled", false);
        json.remove("secret");

        assertJsonEquals(json, result);
    }
}
