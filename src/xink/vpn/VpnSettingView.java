package xink.vpn;

import xink.vpn.wrapper.VpnType;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class VpnSettingView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ListView listVpns = (ListView) findViewById(R.id.listVpns);
        listVpns.setAdapter(new ArrayAdapter<VpnType>(this, R.layout.vpn_profile, VpnType.values()));

        listVpns.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                VpnActor.setActiveVpnType(VpnType.values()[position]);
                connectVpn();
            }
        });
    }

    protected void connectVpn() {
        try {
            VpnActor vpnMgrWrapper = new VpnActor(this);
            vpnMgrWrapper.connect();
        } catch (Throwable e) {
            Log.e("xink", "vpnMgrWrapper failed", e);
        }
    }
}
