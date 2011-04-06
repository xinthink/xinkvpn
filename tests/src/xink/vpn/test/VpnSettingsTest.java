package xink.vpn.test;

import java.util.List;

import xink.vpn.R;
import xink.vpn.VpnProfileRepository;
import xink.vpn.VpnSettings;
import xink.vpn.wrapper.VpnProfile;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class VpnSettingsTest extends ActivityInstrumentationTestCase2<VpnSettings> {

    private VpnSettings settings;
    private VpnProfileRepository repository;

    public VpnSettingsTest() {
        super("xink.vpn", VpnSettings.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.settings = getActivity();
        repository = VpnProfileRepository.getInstance(settings.getApplicationContext());
    }

    @Override
    protected void tearDown() throws Exception {
        List<VpnProfile> profileList = repository.getAllVpnProfiles();

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
