package xink.vpn;

public class NoActiveVpnException extends AppException {

    private static final long serialVersionUID = 1L;

    public NoActiveVpnException(final String detailMessage) {
        super(detailMessage, R.string.err_no_active_vpn);
    }

    public NoActiveVpnException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable, R.string.err_no_active_vpn);
    }

}
