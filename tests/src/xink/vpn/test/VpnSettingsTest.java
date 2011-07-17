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

package xink.vpn.test;

import java.util.List;

import xink.vpn.R;
import xink.vpn.VpnProfileRepository;
import xink.vpn.VpnSettings;
import xink.vpn.test.helper.RepositoryHelper;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class VpnSettingsTest extends ActivityInstrumentationTestCase2<VpnSettings> {

    private VpnSettings settings;
    private VpnProfileRepository repository;
    private RepositoryHelper helper;

    public VpnSettingsTest() {
        super("xink.vpn", VpnSettings.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.settings = getActivity();
        repository = VpnProfileRepository.getInstance(settings);
        helper = new RepositoryHelper(settings);
        helper.clearRepository();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.clearRepository();

        if (!profileList.isEmpty()) {
            VpnProfile[] ps = profileList.toArray(new VpnProfile[0]);
            for (VpnProfile p : ps) {
                repository.deleteVpnProfile(p);
            }
        }

        super.tearDown();
    }

    /**
     * Initial vpn list should be empty
     */
    public void testEmptyVpnList() {
        ListView vpnListView = (ListView) settings.findViewById(R.id.listVpns);

        assertTrue("vpn list should be empty", repository.getAllVpnProfiles().isEmpty());
        assertNull("there should be no active vpn", repository.getActiveProfileId());
        assertTrue("vpn listview should be empty", vpnListView.getAdapter().isEmpty());
    }
}
