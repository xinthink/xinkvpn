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

package xink.vpn.test.helper;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.List;

import xink.vpn.L2tpIpsecPskProfile;
import xink.vpn.PptpProfile;
import xink.vpn.VpnProfile;
import xink.vpn.VpnProfileRepository;
import android.content.Context;

public class RepositoryHelper {

    private Context context;
    private VpnProfileRepository repository;

    public RepositoryHelper(final Context ctx) {
        this.context = ctx;
        repository = VpnProfileRepository.i();
    }

    public void populateRepository() {
        PptpProfile pptp = new PptpProfile();
        pptp.name = "pptp";
        pptp.server = "server";
        pptp.encrypted = true;
        pptp.domainSuffices = "8.8.8.8 8.8.4.4";
        pptp.username = "ywu";
        pptp.password = "passwd";
        repository.addVpnProfile(pptp);

        L2tpIpsecPskProfile l2tp = new L2tpIpsecPskProfile();
        l2tp.name = "l2tp";
        l2tp.server = "server";
        l2tp.domainSuffices = "8.8.8.8 8.8.4.4";
        l2tp.psk = "psk";
        l2tp.secretEnabled = true;
        l2tp.secret = "secret";
        l2tp.username = "ywu";
        l2tp.password = "passwd";
        repository.addVpnProfile(l2tp);
    }

    public void clearRepository() {
        List<VpnProfile> profileList = repository.getAllVpnProfiles();

        if (!profileList.isEmpty()) {
            VpnProfile[] ps = profileList.toArray(new VpnProfile[0]);
            for (VpnProfile p : ps) {
                repository.deleteVpnProfile(p);
            }
        }
    }

    public void cleanDir(final String dir) {
        File extDir = new File(dir);

        if (!extDir.exists())
            return;

        File[] files = extDir.listFiles();

        if (files == null)
            return;

        for (File f : files) {
            System.out.println(f + " delete? " + f.delete());
        }
    }

    /**
     * data should be same as original
     */
    public void verifyDataIntegrity() {
        List<VpnProfile> profiles = repository.getAllVpnProfiles();
        assertEquals("check profiles count", 2, profiles.size());
        assertNull("check active id", repository.getActiveProfileId());

        PptpProfile pptp = (PptpProfile) profiles.get(0);
        assertNotNull("check id", pptp.id);
        assertEquals("check pptp name", "pptp", pptp.name);
        assertEquals("check pptp server", "server", pptp.server);
        assertTrue("check pptp encrypt flag", pptp.encrypted);
        assertEquals("check pptp dns", "8.8.8.8 8.8.4.4", pptp.domainSuffices);
        assertEquals("check pptp user", "ywu", pptp.username);
        assertEquals("check pptp password", "passwd", pptp.password);

        L2tpIpsecPskProfile l2tp = (L2tpIpsecPskProfile) profiles.get(1);
        assertNotNull("check id", l2tp.id);
        assertEquals("check l2tp name", "l2tp", l2tp.name);
        assertEquals("check l2tp server", "server", l2tp.server);
        assertEquals("check l2tp dns", "8.8.8.8 8.8.4.4", l2tp.domainSuffices);
        assertEquals("check l2tp psk", "psk", l2tp.psk);
        assertTrue("check l2tp secret flag", l2tp.secretEnabled);
        assertEquals("check l2tp secret", "secret", l2tp.secret);
        assertEquals("check l2tp user", "ywu", l2tp.username);
        assertEquals("check l2tp password", "passwd", l2tp.password);
    }
}
