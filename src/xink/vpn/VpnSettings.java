package xink.vpn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xink.vpn.editor.EditAction;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class VpnSettings extends Activity {

    private static final String TAG = "xink";
    private static final String[] VPN_VIEW_KEYS = new String[] { "vpn" };
    private static final int[] VPN_VIEWS = new int[] { R.id.radioActive };

    private VpnProfileRepository vpnActor;
    private ListView vpnListView;
    private List<Map<String, ?>> vpnListViewContent;
    private VpnViewBinder vpnViewBinder = new VpnViewBinder();
    private VpnViewItem activeVpnItem;
    private SimpleAdapter vpnListAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.selectVpn);
        setContentView(R.layout.vpn_list);

        vpnListViewContent = new ArrayList<Map<String, ?>>();
        vpnActor = VpnProfileRepository.getInstance(getApplicationContext());
        vpnListView = (ListView) findViewById(R.id.listVpns);

        refreshVpnList();
    }

    private void refreshVpnList() {
        loadContent();

        vpnListAdapter = new SimpleAdapter(this, vpnListViewContent, R.layout.vpn_profile, VPN_VIEW_KEYS, VPN_VIEWS);
        vpnListAdapter.setViewBinder(vpnViewBinder);

        vpnListView.setAdapter(vpnListAdapter);
    }

    private void loadContent() {
        vpnListViewContent.clear();
        activeVpnItem = null;

        String activeProfileId = vpnActor.getActiveProfileId();
        List<VpnProfile> allVpnProfiles = vpnActor.getAllVpnProfiles();

        for (VpnProfile vpnProfile : allVpnProfiles) {
            addToProfileListView(activeProfileId, vpnProfile);
        }
    }

    private void addToProfileListView(final String activeProfileId, final VpnProfile vpnProfile) {
        if (vpnProfile == null) {
            return;
        }

        VpnViewItem item = makeVpnViewItem(activeProfileId, vpnProfile);

        Map<String, Object> row = new HashMap<String, Object>();
        row.put("vpn", item);

        vpnListViewContent.add(row);
    }

    private VpnViewItem makeVpnViewItem(final String activeProfileId, final VpnProfile vpnProfile) {
        VpnViewItem item = new VpnViewItem();
        item.profile = vpnProfile;

        if (vpnProfile.getId().equals(activeProfileId)) {
            item.isActive = true;
            activeVpnItem = item;
        }
        return item;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vpn_list_menu, menu);

        return true;
    }

    /**
     * Handles item selections
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add_vpn:
            onAddVpn();
            break;
        case R.id.menu_del_vpn:
            onDeleteVpn();
            break;
        }
        return true;
    }

    private void onAddVpn() {
        startActivityForResult(new Intent(this, VpnTypeSelection.class), Constants.REQ_SELECT_VPN_TYPE);
    }

    private void onDeleteVpn() {
        startActivityForResult(new Intent(this, DeleteVpn.class), Constants.REQ_DELETE_VPN);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (data == null) {
            return;
        }

        switch (requestCode) {
        case Constants.REQ_SELECT_VPN_TYPE:
            onVpnTypePicked(data);
            break;
        case Constants.REQ_ADD_VPN:
            onVpnProfileAdded(data);
            break;
        case Constants.REQ_DELETE_VPN:
            onVpnProfileDeleted(data);
            break;
        default:
            break;
        }
    }

    private void onVpnTypePicked(final Intent data) {
        VpnType pickedVpnType = (VpnType) data.getExtras().get(Constants.KEY_VPN_TYPE);
        addVpn(pickedVpnType);
    }

    private void addVpn(final VpnType vpnType) {
        Log.i(TAG, "add vpn " + vpnType);
        Intent intent = new Intent(this, vpnType.getEditorClass());
        intent.setAction(EditAction.CREATE.toString());
        startActivityForResult(intent, Constants.REQ_ADD_VPN);
    }

    private void onVpnProfileAdded(final Intent data) {
        Log.i(TAG, "new vpn profile created");

        String newProfileId = data.getStringExtra(Constants.KEY_VPN_PROFILE);
        VpnProfile profile = vpnActor.getProfile(newProfileId);

        addToProfileListView(vpnActor.getActiveProfileId(), profile);
        updateProfileListView();
    }

    private void onVpnProfileDeleted(final Intent data) {
        refreshVpnList();
    }

    @Override
    protected void onStop() {
        save();
        super.onStop();
    }

    private void save() {
        vpnActor.save();
    }

    private void vpnItemActivated(final VpnViewItem activatedItem) {
        if (activeVpnItem == activatedItem) {
            return;
        }

        if (activeVpnItem != null) {
            activeVpnItem.isActive = false;
        }

        activeVpnItem = activatedItem;
        vpnActor.setActiveProfile(activeVpnItem.profile);
        updateProfileListView();
    }

    private void updateProfileListView() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                vpnListAdapter.notifyDataSetChanged();
            }
        });
    }

    static final class VpnViewBinder implements ViewBinder {

        @Override
        public boolean setViewValue(final View view, final Object data, final String textRepresentation) {
            if (!(data instanceof VpnViewItem)) {
                return false;
            }

            VpnViewItem item = (VpnViewItem) data;
            boolean bound = false;

            if (view instanceof RadioButton) {
                bindVpnItem((RadioButton) view, item);
                bound = true;
            }

            return bound;
        }

        private void bindVpnItem(final RadioButton view, final VpnViewItem item) {
            view.setText(item.profile.getName());

            view.setOnCheckedChangeListener(null);
            view.setOnLongClickListener(null);

            view.setChecked(item.isActive);
            view.setOnCheckedChangeListener(item);
            view.setOnLongClickListener(item);
        }
    }

    final class VpnViewItem implements OnCheckedChangeListener, OnLongClickListener {
        VpnProfile profile;
        boolean isActive;

        @Override
        public void onCheckedChanged(final CompoundButton button, final boolean isChecked) {
            if (isActive == isChecked) {
                return;
            }

            isActive = isChecked;

            if (isActive) {
                vpnItemActivated(this);
            }
        }

        @Override
        public boolean onLongClick(final View view) {
            // isActive = true;
            // vpnItemActivated(this);
            // new VpnConnector(getApplicationContext()).connect();
            // return true;
            return false;
        }

        @Override
        public String toString() {
            return profile.getName();
        }
    }
}
