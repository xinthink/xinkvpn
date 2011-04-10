package xink.vpn.test.export.junit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import xink.vpn.VpnProfileRepository;
import android.test.mock.MockContext;

/**
 * Testing behaviors of the repository when exporting profiles.
 *
 * @author ywu
 */
@RunWith(PowerMockRunner.class)
public class RepositoryExportTest {
    private VpnProfileRepository repository;

    @Before
    protected void setUp() {
        repository = VpnProfileRepository.getInstance(new MockContext());
    }

    @Test
    public void testone() {

    }
}
