package xink.vpn.assist.test.helper;


import static junit.framework.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;

public class RepositoryHelper {

    private Context context;

    public RepositoryHelper(final Context context) {
        super();
        this.context = context;
    }

    public List<VpnProfile> makeProfiles() {
        ArrayList<VpnProfile> profiles = new ArrayList<VpnProfile>();

        PptpProfile pptp = new PptpProfile(context);
        pptp.setId("1");
        pptp.setName("pptp");
        pptp.setServerName("server");
        pptp.setEncryptionEnabled(true);
        pptp.setDomainSuffices("8.8.8.8 8.8.4.4");
        pptp.setUsername("ywu");
        pptp.setPassword("passwd");
        profiles.add(pptp);

        L2tpIpsecPskProfile l2tp = new L2tpIpsecPskProfile(context);
        l2tp.setId("2");
        l2tp.setName("l2tp");
        l2tp.setServerName("server");
        l2tp.setDomainSuffices("8.8.8.8 8.8.4.4");
        l2tp.setPresharedKey("psk");
        l2tp.setSecretEnabled(true);
        l2tp.setSecretString("secret");
        l2tp.setUsername("ywu");
        l2tp.setPassword("passwd");
        profiles.add(l2tp);

        return profiles;
    }

    public void cleanDir(final String dir) {
        File extDir = new File(dir);

        if (!extDir.exists()) {
            return;
        }

        File[] files = extDir.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            System.out.println(f + " delete? " + f.delete());
        }
    }

    /**
     * data should be same as original
     */
    public void verifyDataIntegrity(final String activeId, final List<VpnProfile> profiles) {
        assertEquals("check profiles count", 2, profiles.size());
        assertNull("check active id", activeId);

        PptpProfile pptp = (PptpProfile) profiles.get(0);
        assertNotNull("check id", pptp.getId());
        assertEquals("check pptp name", "pptp", pptp.getName());
        assertEquals("check pptp server", "server", pptp.getServerName());
        assertTrue("check pptp encrypt flag", pptp.isEncryptionEnabled());
        assertEquals("check pptp dns", "8.8.8.8 8.8.4.4", pptp.getDomainSuffices());
        assertEquals("check pptp user", "ywu", pptp.getUsername());
        assertEquals("check pptp password", "passwd", pptp.getPassword());

        L2tpIpsecPskProfile l2tp = (L2tpIpsecPskProfile) profiles.get(1);
        assertNotNull("check id", l2tp.getId());
        assertEquals("check l2tp name", "l2tp", l2tp.getName());
        assertEquals("check l2tp server", "server", l2tp.getServerName());
        assertEquals("check l2tp dns", "8.8.8.8 8.8.4.4", l2tp.getDomainSuffices());
        assertEquals("check l2tp psk", "psk", l2tp.getPresharedKey());
        assertTrue("check l2tp secret flag", l2tp.isSecretEnabled());
        assertEquals("check l2tp secret", "secret", l2tp.getSecretString());
        assertEquals("check l2tp user", "ywu", l2tp.getUsername());
        assertEquals("check l2tp password", "passwd", l2tp.getPassword());
    }
}
