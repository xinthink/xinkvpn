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
package xink.vpn;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for VPN profile list.
 * 
 * @author ywu
 * 
 */
public class VpnListAdapter extends BaseAdapter {

    private VpnSettings activity;
    private VpnProfileRepository repo;
    private List<VpnProfile> vpns;

    public VpnListAdapter(final VpnSettings activity) {
        super();
        this.activity = activity;
        this.repo = VpnProfileRepository.i();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        this.vpns = repo.getAllVpnProfiles();
        return vpns.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public VpnProfile getItem(final int position) {
        return vpns.get(position);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(final int position) {
        return position;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final VpnProfile p = getItem(position);
        View v = LayoutInflater.from(activity).inflate(R.layout.vpn_list_item, null);

        TextView txtName = (TextView) v.findViewById(R.id.txt_vpn_name);
        txtName.setText(p.name);

        TextView txtState = (TextView) v.findViewById(R.id.txt_vpn_state);
        txtState.setVisibility(p.state.isTransitive() ? View.VISIBLE : View.GONE);
        txtState.setText(getStateText(p.state));

        View rdbActive = v.findViewById(R.id.rdb_active_vpn);
        rdbActive.setSelected(p.id.equals(repo.getActiveProfileId()));

        TextView tgbConn = (TextView) v.findViewById(R.id.tgb_conn_vpn);
        tgbConn.setText(p.state == VpnState.CONNECTED ? R.string.on : R.string.off);
        tgbConn.setSelected(p.state == VpnState.CONNECTED);
        tgbConn.setEnabled(p.state.isStable());

        // handle events
        rdbActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                activity.onActiveVpnChanged(p.id);
            }
        });

        tgbConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                activity.toggleVpn(p);
            }
        });
        return v;
    }

    private String getStateText(final VpnState state) {
        String txt = "";
        switch (state) {
        case CONNECTING:
            txt = activity.getString(R.string.connecting);
            break;
        case DISCONNECTING:
            txt = activity.getString(R.string.disconnecting);
            break;
        }

        return txt;
    }
}
