package xink.vpn.wrapper;

public enum VpnType {
    PPTP("PPTP", PptpProfile.class), L2TP("L2TP", L2tpProfile.class), L2TP_IPSEC_PSK("L2TP/IPSec PSK", L2tpIpsecPskProfile.class), L2TP_IPSEC(
            "L2TP/IPSec CRT", null);

    private String name;
    private Class<? extends VpnProfile> clazz;
    private boolean active;

    VpnType(final String name, final Class<? extends VpnProfile> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<? extends VpnProfile> getProfileClass() {
        return clazz;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean a) {
        this.active = a;
    }
}
