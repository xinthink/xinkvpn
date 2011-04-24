package xink.vpn.assist.test.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xink.crypto.Crypto;
import xink.vpn.AppException;
import xink.vpn.assist.bak.RepositoryBackup;
import xink.vpn.assist.test.helper.RepositoryHelper;
import xink.vpn.wrapper.ProfileUtils;
import xink.vpn.wrapper.VpnProfile;
import android.test.AndroidTestCase;

public class RepositoryBackupTest extends AndroidTestCase {

    private static final String EXP_PATH = "/sdcard/XinkVpn/t";
    private static final String ACTIVE_ID_FILE = "active_profile_id";
    private static final String PROFILES_FILE = "profiles";

    private String activeProfileId;
    private List<VpnProfile> profiles;
    RepositoryBackup bak;
    RepositoryHelper helper;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        helper = new RepositoryHelper(mContext);
        helper.cleanDir(EXP_PATH);

        bak = new RepositoryBackup(mContext);
        profiles = new ArrayList<VpnProfile>();
    }

    @Override
    public void tearDown() throws Exception {
        helper.cleanDir(EXP_PATH);
        profiles.clear();
        activeProfileId = null;
        super.tearDown();
    }

    /**
     * When the repository is empty.
     */
    public void testEmptyRepository() throws Exception {
        assertNull("activeId should be null", activeProfileId);
        assertTrue("should not has any profile", profiles.isEmpty());

        String initJson = ProfileUtils.toJson(activeProfileId, profiles);
        System.out.println(initJson);

        bak.backup(Crypto.encrypt(initJson), EXP_PATH);

        File file = new File(EXP_PATH, ACTIVE_ID_FILE);
        assertFalse("should NOT produce an active id file", file.exists());

        file = new File(EXP_PATH, PROFILES_FILE);
        assertFalse("should NOT produce profiles file", file.exists());

        // restore
        try {
            bak.restore(EXP_PATH);
            fail("ooh, u should NOT be here!");
        } catch (AppException e) {
            assertTrue("lack of error prompt", e.getMessageResourceId() > 0);
        }
    }

    /**
     * Cover pptp and l2tp profiles.
     */
    public void testRepositoryExpImp() throws Exception {
        profiles = helper.makeProfiles();
        doBackupAsserts(); // when activeProfileId is null

        activeProfileId = profiles.get(0).getId();
        doBackupAsserts();
    }

    private void doBackupAsserts() throws Exception {
        String initJson = ProfileUtils.toJson(activeProfileId, profiles);
        System.out.println("initJson ==> " + initJson);

        bak.backup(Crypto.encrypt(initJson), EXP_PATH);

        File file = new File(EXP_PATH, ACTIVE_ID_FILE);
        assertTrue("should produce an active id file", file.exists());

        file = new File(EXP_PATH, PROFILES_FILE);
        assertTrue("should produce profiles file", file.exists());

        // restore
        String restoredJson = Crypto.decrypt(bak.restore(EXP_PATH));
        System.out.println("restoredJson ==> " + restoredJson);
        assertEquals("restore incorrect", initJson, restoredJson);
    }
}
