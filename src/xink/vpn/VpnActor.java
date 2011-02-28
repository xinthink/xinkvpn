package xink.vpn;

import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnManager;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnService;
import xink.vpn.wrapper.VpnType;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class VpnActor {

    private static VpnType activeVpnType = VpnType.L2TP;

    private Context context;
    private VpnManager vpnMgr;
    private VpnService vpnSrv;


    public VpnActor(final Context ctx) {
        this.context = ctx;
        vpnMgr = new VpnManager(ctx);
        vpnSrv = new VpnService(ctx);
    }

    public static void setActiveVpnType(final VpnType type) {
        VpnActor.activeVpnType = type;
    }

    private VpnProfile makeVpnProfile() {
        switch (activeVpnType) {
        case L2TP:
            return makeL2tpProfile();
        case L2TP_IPSEC_PSK:
            return makeL2tpPskProfile();
        default:
            return makePptpProfile();
        }
    }

    private VpnProfile makePptpProfile() {
        VpnProfile p = new PptpProfile(context);
        p.setName("pptp@blockcn");
        p.setId("pptp@blockcn");
        p.setServerName("v5.blockcn.com");
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setUsername("xinthink");
        p.setPassword("9jh74Nx$");
        return p;
    }

    private VpnProfile makeL2tpProfile() {
        L2tpProfile p = new L2tpProfile(context);
        p.setName("l2tp@blockcn");
        p.setId("l2tp@blockcn");
        p.setServerName("v5.blockcn.com");
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setUsername("xinthink");
        p.setPassword("9jh74Nx$");
        return p;
    }

    private VpnProfile makeL2tpPskProfile() {
        L2tpIpsecPskProfile p = new L2tpIpsecPskProfile(context);
        p.setName("l2tpPsk@blockcn");
        p.setId("l2tpPsk@blockcn");
        p.setServerName("v5.blockcn.com");
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setPresharedKey("9jh74Nx$");
        p.setUsername("xinthink");
        p.setPassword("9jh74Nx$");
        return p;
    }

    public void connect() {
        vpnMgr.startVpnService();

        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service) {
                try {
                    VpnProfile p = makeVpnProfile();
                    boolean success = vpnSrv.connect(service, p);

                    if (!success) {
                        Log.d("xink", "~~~~~~ connect() failed!");
                    } else {
                        Log.d("xink", "~~~~~~ connect() succeeded!");
                    }
                } catch (Throwable e) {
                    Log.e("xink", "connect()", e);
                    // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                Log.e("xink", "onServiceDisconnected");
                // checkStatus();
            }
        };

        if (!vpnMgr.bindVpnService(c)) {
            Log.e("xink", "bind service failed");
            // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
        }
    }

    public void disconnect() {

    }
}