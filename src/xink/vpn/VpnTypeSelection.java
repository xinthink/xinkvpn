package xink.vpn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xink.vpn.wrapper.VpnType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class VpnTypeSelection extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vpn_type_list);

        ListView listVpnTypes = (ListView) findViewById(R.id.listVpnTypes);

        List<Map<String, ?>> content = new ArrayList<Map<String, ?>>();
        VpnType[] vpnTypes = VpnType.values();
        for (VpnType vpnType : vpnTypes) {
            Map<String, String> row = new HashMap<String, String>();
            row.put("name", getString(R.string.add) + " " + getString(vpnType.getNameRid()));
            row.put("desc", getString(vpnType.getDescRid()));
            content.add(row);
        }

        listVpnTypes.setAdapter(new SimpleAdapter(this, content, R.layout.vpn_type, new String[] { "name", "desc" }, new int[] { R.id.txtVpnType,
                R.id.txtVpnTypeDesc }));

        listVpnTypes.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                VpnType vpnType = VpnType.values()[position];
                Log.i("xink", vpnType + " picked");

                Intent data = new Intent(VpnTypeSelection.this, VpnSettings.class);
                data.putExtra(Constants.KEY_VPN_TYPE, vpnType);
                setResult(Constants.REQ_SELECT_VPN_TYPE, data);
                finish();
            }
        });
    }

}
