package xink.vpn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xink.vpn.wrapper.VpnProfile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DeleteVpn extends Activity {

    private VpnProfileRepository vpnActor;

    private Map<String, String> deleteCandidates = new HashMap<String, String>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.del);

        setContentView(R.layout.delete_vpn);
        ViewGroup content = (ViewGroup) findViewById(R.id.pnlDelCandidates);

        vpnActor = VpnProfileRepository.getInstance(getApplicationContext());
        List<VpnProfile> profiles = vpnActor.getAllVpnProfiles();
        for (final VpnProfile p : profiles) {
            CheckBox chkProfile = new CheckBox(this);
            chkProfile.setText(p.getName());
            chkProfile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    profileSelectionChanged(p.getId(), isChecked);
                }
            });

            content.addView(chkProfile);
        }

        Button btnDelete = (Button) findViewById(R.id.btnDel);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                doDelete();
            }
        });
    }

    private void profileSelectionChanged(final String profileId, final boolean isSelected) {
        if (isSelected) {
            deleteCandidates.put(profileId, profileId);
        } else {
            deleteCandidates.remove(profileId);
        }
    }

    private void doDelete() {
        Set<String> candidates = deleteCandidates.keySet();

        if (!candidates.isEmpty()) {
            vpnActor.deleteVpnProfiles(candidates);
        }

        Intent data = new Intent(this, VpnSettings.class);
        data.putExtra(Constants.KEY_DELETED_COUNT, candidates.size());
        setResult(Constants.REQ_DELETE_VPN, data);
        finish();
    }

}
