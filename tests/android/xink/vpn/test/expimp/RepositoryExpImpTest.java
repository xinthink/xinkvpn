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

package xink.vpn.test.expimp;

import java.io.File;

import xink.vpn.AppException;
import xink.vpn.VpnProfileRepository;
import xink.vpn.test.helper.RepositoryHelper;
import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.test.AndroidTestCase;

/**
 * Testing behaviors of the repository when exporting/importing profiles.
 *
 * @author ywu
 */
public class RepositoryExpImpTest extends AndroidTestCase {

    private static final String EXP_PATH = "/sdcard/XinkVpn/test";
    private static final String ACTIVE_ID_FILE = "active_profile_id";
    private static final String PROFILES_FILE = "profiles";

    private VpnProfileRepository repository;
    private RepositoryHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = new RepositoryHelper(getContext());
        repository = VpnProfileRepository.getInstance(getContext());
        clean();
    }

    @Override
    protected void tearDown() throws Exception {
        clean();
        super.tearDown();
    }

    /**
     * When repo is empty.
     */
    public void testEmptyRepositoryExpImp() {
        // export
        repository.backup(EXP_PATH);

        File file = new File(EXP_PATH, ACTIVE_ID_FILE);
        assertFalse("should NOT produce an active id file", file.exists());

        file = new File(EXP_PATH, PROFILES_FILE);
        assertFalse("should NOT produce profiles file", file.exists());

        // import
        try {
            repository.restore(EXP_PATH);
            fail("ooh, u should NOT be here!");
        }
        catch (AppException e) {
            assertTrue("lack of error prompt", e.getMessageCode() > 0);
        }
    }

    /**
     * Cover pptp and l2tp profiles.
     */
    public void testRepositoryExpImp() {
        helper.populateRepository();

        repository.backup(EXP_PATH);

        File file = new File(EXP_PATH, ACTIVE_ID_FILE);
        assertTrue("should produce an active id file", file.exists());

        file = new File(EXP_PATH, PROFILES_FILE);
        assertTrue("should produce profiles file", file.exists());

        // import
        // make some changes
        VpnProfile p = repository.getAllVpnProfiles().get(0);
        repository.setActiveProfile(p);
        p = repository.getAllVpnProfiles().get(1);
        repository.deleteVpnProfile(p);
        p = makeL2tp();
        repository.addVpnProfile(p);

        // restore
        repository.restore(EXP_PATH);

        // should restore to the status before changes
        helper.verifyDataIntegrity();
    }

    private VpnProfile makeL2tp() {
        L2tpProfile p = new L2tpProfile(mContext);
        p.setName("l2tp test");
        p.setServerName("10.1.1.5");
        return p;
    }

    private void clean() {
        helper.clearRepository();
        helper.cleanDir(EXP_PATH);
    }
}
