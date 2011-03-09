package xink.vpn;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String TAG = "xink";

    private static VpnType activeVpnType = VpnType.L2TP;

    private static VpnActor instance;

    private Context context;
    private VpnManager vpnMgr;
    private VpnService vpnSrv;

    private String activeProfileId;
    private List<VpnProfile> profiles;

    public VpnActor(final Context ctx) {
        this.context = ctx;
        vpnMgr = new VpnManager(ctx);
        vpnSrv = new VpnService(ctx);

        profiles = new ArrayList<VpnProfile>();
        instance = this;
    }

    public static VpnActor getInstance() {
        return instance;
    }

    public void save() {
        try {
            saveActiveProfileId();
            saveProfiles();
        } catch (Throwable e) {
            Log.e(TAG, "save profiles failed", e);
        }
    }

    private void saveActiveProfileId() throws IOException {
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(context.openFileOutput("active_profile_id", Context.MODE_PRIVATE));
            os.writeObject(activeProfileId);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void saveProfiles() throws IOException {
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(context.openFileOutput("profiles", Context.MODE_PRIVATE));
            for (VpnProfile p : profiles) {
                p.write(os);
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public void load() {
        try {
            loadActiveProfileId();
            loadProfiles();
        } catch (Throwable e) {
            Log.e(TAG, "load profiles failed", e);
        }
    }

    private void loadActiveProfileId() throws Exception {
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(context.openFileInput("active_profile_id"));
            activeProfileId = (String) is.readObject();
        } catch (Exception e) {
            Log.w(TAG, "loadActiveProfileId failed", e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void loadProfiles() throws Exception {
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(context.openFileInput("profiles"));
            loadProfilesFrom(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void loadProfilesFrom(final ObjectInputStream is) throws Exception {
        Object obj = null;
        Map<VpnType, VpnProfile> profileMap = new HashMap<VpnType, VpnProfile>();

        try {
            while (true) {
                obj = is.readObject();
                loadProfileObject(obj, is, profileMap);
            }
        } catch (EOFException eof) {
            Log.i(TAG, "reach the end of profiles file");
        }
    }

    private void loadProfileObject(final Object obj, final ObjectInputStream is, final Map<VpnType, VpnProfile> profileMap) throws Exception {
        if (obj == null) {
            return;
        }

        VpnType[] vpnTypes = VpnType.values();
        for (VpnType type : vpnTypes) {
            VpnProfile p = getVpnProfileInstance(profileMap, type);

            if (p.isCompatible(obj)) {
                p.read(obj, is);
                profiles.add(p);
            }
        }
    }

    private VpnProfile getVpnProfileInstance(final Map<VpnType, VpnProfile> profileMap, final VpnType type) {
        VpnProfile p = profileMap.get(type);
        if (p == null) {
            p = VpnProfile.newInstance(type, context);
            profileMap.put(type, p);
        }
        return p;
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
            // case PPTP_VPNCUP:
            // return makePptpVpncup();
            // case L2TP_IPSEC_PSK_VPNCUP:
            // return makeL2tpPskVpncup();
        default:
            return makePptpProfile();
        }
    }

    private VpnProfile makeL2tpPskVpncup() {
        L2tpIpsecPskProfile p = new L2tpIpsecPskProfile(context);
        p.setName("l2tpPsk@vpncup");
        p.setId("l2tpPsk@vpncup");
        p.setServerName("f.vpncup.net");
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setPresharedKey("vpncup.com");
        p.setUsername("xinthink");
        p.setPassword("9jh74Nx$");
        return p;
    }

    private VpnProfile makePptpVpncup() {
        PptpProfile p = new PptpProfile(context);
        p.setName("pptp@vpncup");
        p.setId("pptp@vpncup");
        p.setServerName("f.vpncup.net");
        // p.setEncryptionEnabled(true);
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setUsername("xinthink");
        p.setPassword("9jh74Nx$");
        return p;
    }

    private VpnProfile makePptpProfile() {
        PptpProfile p = new PptpProfile(context);
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
        p.setName("l2tpPsk@blockcn ddddddddd ddddd dddddddd ddddddddd");
        p.setId("l2tpPsk@blockcn");
        p.setServerName("v5.blockcn.com");
        p.setDomainSuffices("8.8.8.8,8.8.4.4");
        p.setPresharedKey("blockcn.com");
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
                    VpnProfile p = getProfile(activeProfileId);
                    boolean success = vpnSrv.connect(service, p);

                    if (!success) {
                        Log.d(TAG, "~~~~~~ connect() failed!");
                    } else {
                        Log.d(TAG, "~~~~~~ connect() succeeded!");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "connect()", e);
                    // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                Log.e(TAG, "onServiceDisconnected");
                // checkStatus();
            }
        };

        if (!vpnMgr.bindVpnService(c)) {
            Log.e(TAG, "bind service failed");
            // broadcastConnectivity(VpnState.IDLE, VpnManager.VPN_ERROR_CONNECTION_FAILED);
        }
    }

    public void disconnect() {

    }

    public void setActiveProfile(final VpnProfile profile) {
        activeProfileId = profile.getId();
    }

    public String getActiveProfileId() {
        return activeProfileId;
    }

    /**
     * @return a read-only view of the VpnProfile list.
     */
    public List<VpnProfile> getAllVpnProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public void addVpnProfile(final VpnProfile p) {
        p.setId("vpnprofile_" + (profiles.size() + 1));
        profiles.add(p);
    }

    public VpnProfile getProfile(final String id) {

        for (VpnProfile p : profiles) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
}