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

import xink.vpn.PptpProfile;
import xink.vpn.VpnProfile;

/**
 * @author ywu
 * 
 */
public class PptpProfileSerializationTest extends TestCase {

    private JSONObject json;
    private PptpProfile p;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        json = new JSONObject();
        json.put("type", PPTP);
        json.put("id", "1");
        json.put("name", "pptp one");
        json.put("server", "pptp server");
        json.put("username", "usr");
        json.put("password", "passwd");
        json.put("domain", "8.8.8.8");
        json.put("encrypt", true);

        p = new PptpProfile();
        p.id = "1";
        p.name = "pptp one";
        p.server = "pptp server";
        p.username = "usr";
        p.password = "passwd";
        p.domainSuffices = "8.8.8.8";
        p.encrypted = true;
    }

    /**
     * deserializing pptp profile, with all fields
     */
    public void testPptpFromJson() throws JSONException {
        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof PptpProfile);
        assertEquals(PPTP, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("pptp one", p.name);
        assertEquals("pptp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("8.8.8.8", p.domainSuffices);
        assertTrue(((PptpProfile) p).encrypted);
    }

    /**
     * deserializing pptp profile, lack of some fields
     */
    public void testPptpFromPartialJson() throws JSONException {
        json.remove("domain");
        json.put("encrypt", false);

        VpnProfile p = VpnProfile.fromJson(json);

        assertTrue(p instanceof PptpProfile);
        assertEquals(PPTP, p.type);
        assertEquals(IDLE, p.state);

        assertEquals("1", p.id);
        assertEquals("pptp one", p.name);
        assertEquals("pptp server", p.server);
        assertEquals("usr", p.username);
        assertEquals("passwd", p.password);
        assertEquals("", p.domainSuffices);
        assertFalse(((PptpProfile) p).encrypted);
    }

    /**
     * serializing pptp profile, with all fields
     */
    public void testPptpToJson() throws JSONException {
        JSONObject result = p.toJson();
        assertJsonEquals(json, result);
    }

    /**
     * serializing pptp profile, lack of some fields
     */
    public void testPptpToPartialJson() throws JSONException {
        p.domainSuffices = null;
        p.encrypted = false;

        JSONObject result = p.toJson();

        // expected json
        json.remove("domain");
        json.put("encrypt", false);

        assertJsonEquals(json, result);
    }


}
